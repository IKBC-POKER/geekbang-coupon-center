package com.geekbang.coupon.template.service.intf;

import com.geekbang.coupon.template.api.beans.CouponTemplateInfo;
import com.geekbang.coupon.template.api.beans.PagedCouponTemplateInfo;
import com.geekbang.coupon.template.api.beans.TemplateSearchParams;

import java.util.Collection;
import java.util.Map;

public interface CouponTemplateService {

    //创建优惠模版
    CouponTemplateInfo createTeplate(CouponTemplateInfo templateInfo);
    CouponTemplateInfo cloneTemplate(Long templateId);

    //模版查询
    PagedCouponTemplateInfo search(TemplateSearchParams params);

    CouponTemplateInfo loadTemplateInfo(Long id);
    //设置优惠券模版失效
    void deleteTemplate(Long id);
    // 批量查询
    // Map是模板ID，key是模板详情
    Map<Long, CouponTemplateInfo> getTemplateInfoMap(Collection<Long> ids);
}
