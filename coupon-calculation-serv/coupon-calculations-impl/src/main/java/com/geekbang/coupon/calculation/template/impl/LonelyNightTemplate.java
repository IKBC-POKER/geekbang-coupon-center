package com.geekbang.coupon.calculation.template.impl;

import com.geekbang.coupon.calculation.template.AbstractRuleTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Calendar;

@Slf4j
@Component
public class LonelyNightTemplate extends AbstractRuleTemplate {
    @Override
    protected Long calculateNewPrice(Long orderTotalAmount, Long shopTotalAmount, Long quota) {
        Calendar calendar = Calendar.getInstance();
        int hourofday = calendar.get(Calendar.HOUR_OF_DAY);
        if (hourofday>=23 || hourofday<2){
            quota *=2;
        }
        Long benefitamount = shopTotalAmount<quota?shopTotalAmount:quota;
        return orderTotalAmount-benefitamount;
    }
}
