package com.geekbang.coupon.customer.api.beans;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestCoupon {

    //用户领券
    @NotNull
    private Long userId;

    //券模板id
    @NotNull
    private Long couponTemplateId;
}
