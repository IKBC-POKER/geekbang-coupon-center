package com.geekbang.coupon.customer.rabbitm;

import com.geekbang.coupon.customer.api.beans.RequestCoupon;
import com.geekbang.coupon.customer.service.CouponCustomerService;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.pqc.crypto.newhope.NHOtherInfoGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 优惠券消费者
 */
@Slf4j
@Service
public class CouponConsumer {

    @Autowired
    private CouponCustomerService couponCustomerService;

    @Bean
    public Consumer<RequestCoupon> addCoupon(){
        return request ->{
          log.info("received: {}", request);
          couponCustomerService.requestCoupon(request);
        };
    }

    @Bean
    public Consumer<String> deleteCoupon(){
        return request -> {
            log.info("received: {}", request);
            List<Long> params = Arrays.stream(request.split(","))
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
            couponCustomerService.deleteCoupon(params.get(0),params.get(1));
        };
    }

    /**
     * 延时消息队列消费者消费消息
     * @return
     */
    @Bean
    public Consumer<RequestCoupon> addCouponDelay(){
        return request ->{
          log.info("received: {}", request);
          couponCustomerService.requestCoupon(request);
        };
    }
}
