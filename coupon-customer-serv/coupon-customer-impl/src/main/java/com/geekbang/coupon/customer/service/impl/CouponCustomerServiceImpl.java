package com.geekbang.coupon.customer.service.impl;

import com.geekbang.coupon.calculations.api.beans.ShoppingCart;
import com.geekbang.coupon.calculations.api.beans.SimulationOrder;
import com.geekbang.coupon.calculations.api.beans.SimulationResponse;
import com.geekbang.coupon.customer.api.beans.RequestCoupon;
import com.geekbang.coupon.customer.api.beans.SearchCoupon;
import com.geekbang.coupon.customer.api.enums.CouponStatus;
import com.geekbang.coupon.customer.dao.CouponDao;
import com.geekbang.coupon.customer.dao.entity.Coupon;
import com.geekbang.coupon.customer.feign.CalculationService;
import com.geekbang.coupon.customer.feign.TemplateService;
import com.geekbang.coupon.customer.service.CouponConverter;
import com.geekbang.coupon.customer.service.CouponCustomerService;
import com.geekbang.coupon.template.api.beans.CouponInfo;
import com.geekbang.coupon.template.api.beans.CouponTemplateInfo;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClient;

import javax.transaction.Transactional;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CouponCustomerServiceImpl implements CouponCustomerService {
    @Autowired
    private CouponDao couponDao;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private CalculationService calculationService;
//    @Autowired
//    private CouponTemplateService couponTemplateService;
//
//    @Autowired
//    private CouponCalculationService couponCalculationService;

    /**
     * 用户领取优惠券
     */
    @Override
    public Coupon requestCoupon(RequestCoupon requestCoupon) {
        CouponTemplateInfo templateInfo = templateService.getTemplate(requestCoupon.getCouponTemplateId());
//        CouponTemplateInfo templateInfo = webClientBuilder.build()
//                .get()
//                .uri("http://coupon-template-serv/template/getTemplate?id="+requestCoupon.getCouponTemplateId())
//                .retrieve()
//                .bodyToMono(CouponTemplateInfo.class)
//                .block();
        if (templateInfo == null){
            log.error("invalid template id={}", requestCoupon.getCouponTemplateId());
            throw new IllegalArgumentException("Invalid template id");
        }
        //校验模版是否过期
        long nowTime = Calendar.getInstance().getTimeInMillis(); //获取当前时间
        long examTime = templateInfo.getRule().getDeadline();
        if ( nowTime>examTime || BooleanUtils.isFalse(templateInfo.getAvailable())){
            log.error("template is not available id={}", requestCoupon.getCouponTemplateId());
            throw new IllegalArgumentException("template is unavailable");
        }
        //查询用户的优惠券领取数量，超过上限报错
        long count = couponDao.countByUserIdAndTemplateId(requestCoupon.getUserId(),requestCoupon.getCouponTemplateId());
        if (count > templateInfo.getRule().getLimitation()){
            log.error("exceeds maximum number");
            throw new IllegalArgumentException("exceeds maximum number");
        }
        Coupon coupon  = Coupon.builder()
                .userId(requestCoupon.getUserId())
                .templateId(requestCoupon.getCouponTemplateId())
                .shopId(templateInfo.getShopId())
                .status(CouponStatus.AVAILABLE)
                .build();
        couponDao.save(coupon);
        return coupon;
    }

    @Override
    @Transactional
    public ShoppingCart placeOrder(ShoppingCart order) {
        if (CollectionUtils.isEmpty(order.getProducts())){
            log.error("invalid check out request, order={}", order);
            throw new IllegalArgumentException("cart is empty");
        }
        Coupon coupon=null;
        if (order.getCouponId() != null) {
            // 如果有优惠券，验证是否可用，并且是当前客户的
            Coupon example = Coupon.builder().
                    userId(order.getUserId()).
                    id(order.getCouponId()).
                    status(CouponStatus.AVAILABLE)
                    .build();
            coupon = couponDao.findAll(Example.of(example))
                    .stream()
                    .findFirst()
                    //查询不到优惠券报错
                    .orElseThrow(() -> new RuntimeException("Coupon not found"));
            CouponInfo couponInfo = CouponConverter.converToCoupon(coupon);
            couponInfo.setTemplate(templateService.getTemplate(couponInfo.getTemplateId()));
//            couponInfo.setTemplate(webClientBuilder.build()
//                    .get()
//                    .uri("http://coupon-template-serv/template/getTemplate?id="+couponInfo.getTemplateId())
//                    .retrieve()
//                    .bodyToMono(CouponTemplateInfo.class)
//                    .block());
            order.setCouponInfos(Lists.newArrayList(couponInfo));
        }
        //清算
        ShoppingCart shoppingCart = calculationService.checkout(order);
//                ShoppingCart shoppingCart = webClientBuilder.build()build
//                        .post().uri("http://coupon-calculation-serv/calculator/checkout")
//                        .bodyValue(order)
//                        .retrieve().bodyToMono(ShoppingCart.class).block();
        if (coupon != null) {
            // 如果优惠券没有被结算掉，而用户传递了优惠券，报错提示该订单满足不了优惠条件
            if (CollectionUtils.isEmpty(shoppingCart.getCouponInfos())){
                log.error("cannot apply coupon to order, couponId={}", coupon.getId());
                throw new IllegalArgumentException("coupon is not applicable to this order");
            }
            log.info("update coupon status to used, couponId={}", coupon.getId());
            coupon.setStatus(CouponStatus.USED);
            couponDao.save(coupon);
        }
        return shoppingCart;
    }

    @Override
    public SimulationResponse simulateOrderPrice(SimulationOrder order) {
        List<CouponInfo> couponInfos = Lists.newArrayList();
        // 挨个循环，把优惠券信息加载出来
        // 高并发场景下不能这么一个个循环，更好的做法是批量查询
        // 而且券模板一旦创建不会改内容，所以在创建端做数据异构放到缓存里，使用端从缓存捞template信息
        for (Long couponId : order.getCouponIDs()) {
            Coupon example = Coupon.builder()
                    .userId(order.getUserId())
                    .id(couponId)
                    .status(CouponStatus.AVAILABLE)
                    .build();
            Optional<Coupon> couponOptional = couponDao.findAll(Example.of(example))
                    .stream()
                    .findFirst();
            // 加载优惠券模板信息
            if (((Optional<?>) couponOptional).isPresent()) {
                Coupon coupon = couponOptional.get();
                CouponInfo couponInfo = CouponConverter.converToCoupon(coupon);

                couponInfo.setTemplate(loadTemplateInfo(coupon.getTemplateId()));
                couponInfos.add(couponInfo);
            }
        }
        order.setCouponInfos(couponInfos);
        return calculationService.simulate(order);
//        return webClientBuilder.build().post()
//                .uri("http://coupon-calculation-serv/calculator/simulate")
//                .bodyValue(order)
//                .retrieve()
//                .bodyToMono(SimulationResponse.class)
//                .block();
    }

    /**
     * 用户查询优惠券的接口
     */
    @Override
    public List<CouponInfo> findUserCoupon(SearchCoupon request) {
        // 在真正的生产环境，这个接口需要做分页查询，并且查询条件要封装成一个类
        Coupon example = Coupon.builder()
                .userId(request.getUserId())
                .status(CouponStatus.convert(request.getCouponStatus()))
                .shopId(request.getShopId())
                .build();

        // 这里你可以尝试实现分页查询
        List<Coupon> coupons = couponDao.findAll(Example.of(example));
        if (coupons.isEmpty()) {
            return Lists.newArrayList();
        }

        List<Long> templateIds = coupons.stream()
                .map(Coupon::getTemplateId)
                .collect(Collectors.toList());
        // 获取这些优惠券的模板ID
//        String templateIds = coupons.stream()
//                .map(Coupon::getTemplateId)
//                .map(String::valueOf)
//                .distinct()
//                .collect(Collectors.joining(","));
        Map<Long, CouponTemplateInfo> templateMap = templateService.getTemplateInBatch(templateIds);
//        Map<Long, CouponTemplateInfo> templateMap = webClientBuilder.build()
//                .get()
//                .uri("http://coupon-template-serv/template/getTemplateInfoMap?id="+templateIds)
//                .retrieve()
//                .bodyToMono(new ParameterizedTypeReference<Map<Long, CouponTemplateInfo>>() {})
//                .block();
//        coupons.stream().forEach(e -> e.setTemplateInfo(templateMap.get(e.getTemplateId())));

        return coupons.stream()
                .map(CouponConverter::converToCoupon)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteCouponTemplate(Long templateId) {
//        templateService.deleteTemplate(templateId);
//        couponDao.deleteInBatch(templateId, CouponStatus.INACTIVE);
        // 模拟分布式异常
        throw new RuntimeException("AT分布式事务挂球了");
    }

    // 逻辑删除优惠券
    @Override
    public void deleteCoupon(Long userId, Long couponId) {
        Coupon example = Coupon.builder()
                .userId(userId)
                .id(couponId)
                .status(CouponStatus.AVAILABLE)
                .build();
        Coupon coupon = couponDao.findAll(Example.of(example))
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Could not find available coupon"));
        coupon.setStatus(CouponStatus.INACTIVE);
        couponDao.save(coupon);
    }

    private CouponTemplateInfo loadTemplateInfo(Long templateId) {
        return templateService.getTemplate(templateId);
//        return webClientBuilder.build().get()
//                .uri("http://coupon-template-serv/template/getTemplate?id=" + templateId)
//                .retrieve()
//                .bodyToMono(CouponTemplateInfo.class)
//                .block();
    }
}
