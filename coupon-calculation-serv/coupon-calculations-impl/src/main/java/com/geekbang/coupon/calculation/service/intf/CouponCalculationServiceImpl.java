package com.geekbang.coupon.calculation.service.intf;

import com.alibaba.fastjson.JSON;
import com.geekbang.coupon.calculation.service.CouponCalculationService;
import com.geekbang.coupon.calculation.template.CouponTemplateFactory;
import com.geekbang.coupon.calculations.api.beans.ShoppingCart;
import com.geekbang.coupon.calculations.api.beans.SimulationOrder;
import com.geekbang.coupon.calculations.api.beans.SimulationResponse;
import com.geekbang.coupon.template.api.beans.CouponInfo;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CouponCalculationServiceImpl implements CouponCalculationService {

    @Autowired
    private CouponTemplateFactory couponTemplateFactory;
    // 优惠券结算
    // 这里通过Factory类决定使用哪个底层Rule，底层规则对上层透明
    @Override
    public ShoppingCart calculateOrderPrice(ShoppingCart cart) {
        log.info("calculate order price: {}", JSON.toJSONString(cart));
        return couponTemplateFactory.getTemplate(cart).calculate(cart);
    }

    @Override
    public SimulationResponse simulateOrder(SimulationOrder order) {
        SimulationResponse response = new SimulationResponse();
        Long minOrderPrice = Long.MAX_VALUE;
        // 计算每一个优惠券的订单价格
        for (CouponInfo coupon:order.getCouponInfos()) {
            ShoppingCart cart = new ShoppingCart();
            cart.setProducts(order.getProducts());
            cart.setCouponInfos(Lists.newArrayList(coupon));

            Long couponId = coupon.getId();
            Long orderPrice = cart.getCost();
            // 设置当前优惠券对应的订单价格
            response.getCouponToOrderPrice().put(couponId,orderPrice);
            // 比较订单价格，设置当前最优优惠券的ID
            if (minOrderPrice>orderPrice){
                response.setBestCouponId(couponId);
                minOrderPrice = orderPrice;
            }
        }
        return response;
    }
}
