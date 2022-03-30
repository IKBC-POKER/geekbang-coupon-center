package com.geekbang.coupon.calculation.template;

import com.geekbang.coupon.calculations.api.beans.ShoppingCart;

public interface RuleTemplate {

    ShoppingCart calculate (ShoppingCart settlement);
}
