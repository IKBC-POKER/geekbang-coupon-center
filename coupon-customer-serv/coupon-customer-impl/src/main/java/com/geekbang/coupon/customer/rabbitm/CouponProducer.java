package com.geekbang.coupon.customer.rabbitm;

import com.geekbang.coupon.customer.api.beans.RequestCoupon;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

/**
 * 优惠券消息生产者
 */
@Slf4j
@Service
public class CouponProducer {

    @Autowired
    private StreamBridge streamBridge;

    /**
     *
     * @param coupon
     */
    public void sendCoupon(RequestCoupon coupon){
        log.info("sent: {}", coupon);
        streamBridge.send(EventConstant.ADD_COUPON_EVENT, coupon);
    }

    /**
     * 删除用户优惠券信息
     * @param userId
     * @param couponId
     */
    public void deleteCoupon(Long userId, Long couponId){
        log.info("sent delete coupon event: userId={}, couponId={}", userId, couponId);
        streamBridge.send(EventConstant.DELETE_COUPON_EVENT,userId + "," + couponId);
    }

    // 使用延迟消息发送
    public void sendCouponInDelay(RequestCoupon coupon) {
        log.info("sent: {}", coupon);
        streamBridge.send(EventConstant.ADD_COUPON_DELAY_EVENT, MessageBuilder.withPayload(coupon).setHeader("x-delay",10*100).build());
    }
}
