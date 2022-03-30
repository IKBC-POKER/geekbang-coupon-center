package com.geekbang.coupon.calculation.controller;

import com.geekbang.coupon.calculation.service.CouponCalculationService;
import com.geekbang.coupon.calculations.api.beans.ShoppingCart;
import com.geekbang.coupon.calculations.api.beans.SimulationOrder;
import com.geekbang.coupon.calculations.api.beans.SimulationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("calculator")
public class CouponCalculationController {

    @Autowired
    public CouponCalculationService couponCalculationService;

    //优惠结算
    @PostMapping("/checkout")
    @ResponseBody
    public ShoppingCart calculateOrderPrice(ShoppingCart settlement){
        return couponCalculationService.calculateOrderPrice(settlement);
    }
    // 优惠券列表挨个试算
    // 给客户提示每个可用券的优惠额度，帮助挑选
    @PostMapping("/simulate")
    @ResponseBody
    public SimulationResponse simulate(SimulationOrder order){
        return couponCalculationService.simulateOrder(order);
    }
}
