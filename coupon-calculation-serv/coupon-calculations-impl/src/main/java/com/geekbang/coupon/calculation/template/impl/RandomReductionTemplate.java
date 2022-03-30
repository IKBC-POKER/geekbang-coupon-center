package com.geekbang.coupon.calculation.template.impl;

import com.geekbang.coupon.calculation.template.AbstractRuleTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Random;

// 随机减钱
@Slf4j
@Component
public class RandomReductionTemplate extends AbstractRuleTemplate {
    @Override
    protected Long calculateNewPrice(Long orderTotalAmount, Long shopTotalAmount, Long quota) {
        Long benifit = Math.min(shopTotalAmount,quota);
        int reductionAmount = new Random().nextInt(benifit.intValue());
        Long newCost = orderTotalAmount-reductionAmount;
        log.debug("original price={}, new price={}", orderTotalAmount, newCost );
        return newCost;
    }
}
