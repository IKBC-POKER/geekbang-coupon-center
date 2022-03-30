package com.geekbang.coupon.customer.contorller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.geekbang.coupon.calculations.api.beans.ShoppingCart;
import com.geekbang.coupon.calculations.api.beans.SimulationOrder;
import com.geekbang.coupon.calculations.api.beans.SimulationResponse;
import com.geekbang.coupon.customer.api.beans.RequestCoupon;
import com.geekbang.coupon.customer.api.beans.SearchCoupon;
import com.geekbang.coupon.customer.api.enums.CouponStatus;
import com.geekbang.coupon.customer.dao.entity.Coupon;
import com.geekbang.coupon.customer.rabbitm.CouponProducer;
import com.geekbang.coupon.customer.service.CouponCustomerService;
import com.geekbang.coupon.template.api.beans.CouponInfo;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RefreshScope
@RequestMapping("coupon-customer")
public class CouponCustomerController {

    @Autowired
    private CouponCustomerService couponCustomerService;

    @Autowired
    private CouponProducer couponProducer;

    @Value("${disableCouponRequest:false}")
    private Boolean disableCoupon;

    @GetMapping("requestCoupon")
    @SentinelResource(value = "requestCoupon",fallback = "getNothing")
    public Coupon requestCoupon(@Valid @RequestBody RequestCoupon requestCoupon){
        if (disableCoupon){
            log.info("暂停领取优惠券");
            return null;
        }
        return couponCustomerService.requestCoupon(requestCoupon);
    }

    //获取优惠券熔断机制调用
    public Coupon getNothing(RequestCoupon request) {
        return Coupon.builder()
                .status(CouponStatus.INACTIVE)
                .build();
    }

    // 用户删除优惠券
    @DeleteMapping("deleteCooupon")
    public void deleteCoupon(@RequestParam("userId")Long userId,@RequestParam("couponId")Long couponId){
        couponCustomerService.deleteCoupon(userId,couponId);
    }

    //用户订单优惠金额试算
    @PostMapping("simulateOrder")
    public SimulationResponse simulate(@Valid@RequestBody SimulationOrder order){
        return couponCustomerService.simulateOrderPrice(order);
    }

    // ResponseEntity - 指定返回状态码 - 可以作为一个课后思考题
    @PostMapping("placeOrder")
    // 可以使用同一个资源，这样控制就会对两个资源生效
    @SentinelResource(value = "checkout")
    public ShoppingCart checkout(@Valid @RequestBody ShoppingCart info){
        return couponCustomerService.placeOrder(info);
    }

    // 实现的时候最好封装一个search object类
    @PostMapping("findCoupon")
    @SentinelResource(value = "customer-findCoupon")
    public List<CouponInfo> findCoupon(@Valid @RequestBody SearchCoupon request) {
        return couponCustomerService.findUserCoupon(request);
    }

    @PostMapping("requestCouponEvent")
    public void requestCouponEvent(@Valid@RequestBody RequestCoupon coupon){
        couponProducer.sendCoupon(coupon);
    }

    /**
     * 删除用户优惠券，基于消息队列，rabbitmq
     * @param userId
     * @param couponId
     */
    @PostMapping("deleteCouponEvent")
    public void deleteCouponEvent(@RequestParam("userId")Long userId,@RequestParam("couponId")Long couponId){
        couponProducer.deleteCoupon(userId,couponId);
    }

    /**
     * 使用创建消息到延时队列
     * @param requestCoupon
     */
    @PostMapping("requestCouponDelayedEvent")
    public void requestCouponDelayedEvent(@Valid@RequestBody RequestCoupon requestCoupon){
        couponProducer.sendCouponInDelay(requestCoupon);
    }


    @DeleteMapping("template")
    @GlobalTransactional(name = "coupon-customer-serv", rollbackFor = Exception.class)
    public void deleteCoupon(@RequestParam("templateId") Long templateId) {
        couponCustomerService.deleteCouponTemplate(templateId);
    }
}
