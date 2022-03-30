package com.geekbang.coupon.template.service;

import com.geekbang.coupon.template.api.beans.CouponTemplateInfo;
import com.geekbang.coupon.template.api.beans.PagedCouponTemplateInfo;
import com.geekbang.coupon.template.api.beans.TemplateSearchParams;
import com.geekbang.coupon.template.api.enums.CouponType;
import com.geekbang.coupon.template.converter.CouponTemplateConverter;
import com.geekbang.coupon.template.dao.CouponTemplateDao;
import com.geekbang.coupon.template.dao.entity.CouponTemplate;
import com.geekbang.coupon.template.service.intf.CouponTemplateService;
import com.geekbang.coupon.template.service.intf.CouponTemplateServiceTCC;
import io.seata.rm.tcc.api.BusinessActionContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 优惠券模板类相关操作服务
 */
@Slf4j
@Service
public class CouponTemplateServiceImpl implements CouponTemplateServiceTCC {

    @Autowired
    private CouponTemplateDao couponTemplateDao;


    //创建优惠券
    @Override
    public CouponTemplateInfo createTeplate(CouponTemplateInfo templateInfo) {
        // 单个门店最多可以创建100张优惠券模板,超出100抛出异常报错处理
        if (templateInfo.getShopId()!=null){
            Integer count = couponTemplateDao.countByShopIdAndAvailable(templateInfo.getShopId(),true);
            if (count >=100){
                log.error("the totals of coupon template exceeds maximum number");
                throw new UnsupportedOperationException("exceeded the maximum of coupon templates that you can create");
            }
        }
        CouponTemplate template = CouponTemplate.builder().name(templateInfo.getName())
                .description(templateInfo.getDesc())
                .category(CouponType.convert(templateInfo.getType()))
                .available(true)
                .shopId(templateInfo.getShopId())
                .rule(templateInfo.getRule())
                .build();
        template = couponTemplateDao.save(template);
        return CouponTemplateConverter.couponTemplateInfo(template);
    }

    // 克隆优惠券
    @Override
    public CouponTemplateInfo cloneTemplate(Long templateId) {
        log.info("cloning template id {}", templateId);
        CouponTemplate template = couponTemplateDao.findById(templateId).orElseThrow(() -> new IllegalArgumentException("invalid template id"));
        CouponTemplate target = new CouponTemplate();
        BeanUtils.copyProperties(template,target);
        target.setAvailable(true);
        target.setId(null);
        couponTemplateDao.save(target);
        return CouponTemplateConverter.couponTemplateInfo(target);
    }

    @Override
    public PagedCouponTemplateInfo search(TemplateSearchParams params) {
        CouponTemplate template = CouponTemplate.builder().shopId(params.getShopId()).category(CouponType.convert(params.getType())).available(params.getAvailable()).name(params.getName()).build();
        Pageable page = (Pageable) PageRequest.of(params.getPage(),params.getPageSize());
        couponTemplateDao.findAll(Example.of(template), page);
        Page<CouponTemplate> result = couponTemplateDao.findAll(Example.of(template), page);
        List<CouponTemplateInfo> couponTemplateInfos = result.stream().map(CouponTemplateConverter::couponTemplateInfo).collect(Collectors.toList());
        PagedCouponTemplateInfo response = PagedCouponTemplateInfo.builder().templates(couponTemplateInfos).page(params.getPage()).total(result.getTotalElements()).build();
        return response ;
    }

    /**
     * 通过ID查询优惠券模板
     */
    @Override
    public CouponTemplateInfo loadTemplateInfo(Long id) {
        Optional<CouponTemplate> templat= couponTemplateDao.findById(id);
        return templat.isPresent()?CouponTemplateConverter.couponTemplateInfo(templat.get()):null;
    }

    @Override
    public void deleteTemplate(Long id) {
        int rows = couponTemplateDao.makeCouponUnavailable(id);
        if (rows == 0) {
            throw new IllegalArgumentException("Template Not Found: " + id);
        }
    }

    @Override
    public Map<Long, CouponTemplateInfo> getTemplateInfoMap(Collection<Long> ids) {
        List<com.geekbang.coupon.template.dao.entity.CouponTemplate> templates = couponTemplateDao.findAllById(ids);
        return templates.stream()
                .map(CouponTemplateConverter::couponTemplateInfo)
                .collect(Collectors.toMap(CouponTemplateInfo::getId, Function.identity()));
    }

    @Transactional
    public void deleteTemplateTCC(Long id) {
        CouponTemplate filter = CouponTemplate.builder()
                .available(true)
                .locked(false)
                .id(id)
                .build();
        CouponTemplate template = couponTemplateDao.findAll(Example.of(filter)).stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Template Not Found"));
        template.setLocked(true);
        couponTemplateDao.save(template);
    }

    @Override
    public void deleteTemplateCommit(BusinessActionContext context) {
        Long id = Long.parseLong(context.getActionContext("id").toString());
        CouponTemplate template = couponTemplateDao.findById(id).get();
        template.setLocked(false);
        template.setAvailable(false);
        couponTemplateDao.save(template);
        log.info("TCC committed");
    }

    @Override
    public void deleteTemplateCancel(BusinessActionContext context) {
        Long id = Long.parseLong(context.getActionContext("id").toString());
        Optional<CouponTemplate> templateOption = couponTemplateDao.findById(id);
        if (templateOption.isPresent()){
            CouponTemplate template = templateOption.get();
            template.setLocked(false);
            couponTemplateDao.save(template);
        }
        log.info("TCC cancel");
    }
}
