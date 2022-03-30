package com.geekbang.coupon.calculation.service;

import com.geekbang.coupon.calculations.api.beans.ShoppingCart;
import com.geekbang.coupon.calculations.api.beans.SimulationOrder;
import com.geekbang.coupon.calculations.api.beans.SimulationResponse;
import org.springframework.web.bind.annotation.RequestBody;

public interface CouponCalculationService {

    ShoppingCart calculateOrderPrice(@RequestBody ShoppingCart cart);
    SimulationResponse simulateOrder(@RequestBody SimulationOrder order);
}
