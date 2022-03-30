package com.geekbang.coupon.customer.service;

import com.geekbang.coupon.calculations.api.beans.ShoppingCart;
import com.geekbang.coupon.calculations.api.beans.SimulationOrder;
import com.geekbang.coupon.calculations.api.beans.SimulationResponse;
import com.geekbang.coupon.customer.api.beans.RequestCoupon;
import com.geekbang.coupon.customer.api.beans.SearchCoupon;
import com.geekbang.coupon.customer.dao.entity.Coupon;
import com.geekbang.coupon.template.api.beans.CouponInfo;

import java.util.List;

/**
 * 用户对接服务
 */
public interface CouponCustomerService {
    //领券接口
    Coupon requestCoupon(RequestCoupon requestCoupon);

    void deleteCoupon(Long userId,Long couponId);
    //核销优惠券
    ShoppingCart placeOrder(ShoppingCart cart);

    //优惠券金额式算
    SimulationResponse simulateOrderPrice(SimulationOrder order);

    //用户优惠券查询
    List<CouponInfo> findUserCoupon(SearchCoupon request);

    void deleteCouponTemplate(Long temlpalteId);

}
