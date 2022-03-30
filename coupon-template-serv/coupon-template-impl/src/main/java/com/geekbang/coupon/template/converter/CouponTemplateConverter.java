package com.geekbang.coupon.template.converter;

import com.geekbang.coupon.template.api.beans.CouponTemplateInfo;
import com.geekbang.coupon.template.dao.entity.CouponTemplate;

public class CouponTemplateConverter {
    public static CouponTemplateInfo couponTemplateInfo(CouponTemplate template){
        return CouponTemplateInfo.builder()
                .id(template.getId())
                .name(template.getName())
                .desc(template.getDescription())
                .type(template.getCategory().getCode())
                .shopId(template.getShopId())
                .available(template.getAvailable())
                .rule(template.getRule())
                .build();
    }
}
