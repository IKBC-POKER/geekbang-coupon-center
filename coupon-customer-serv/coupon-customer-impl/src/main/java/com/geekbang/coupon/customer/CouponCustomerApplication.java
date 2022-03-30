package com.geekbang.coupon.customer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@ComponentScan(basePackages = {"com.geekbang"})
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {"com.geekbang"})
@EntityScan(basePackages = {"com.geekbang"})
@EnableFeignClients(basePackages = {"com.geekbang"})
public class CouponCustomerApplication {
    public static void main(String[] args) {
        SpringApplication.run(CouponCustomerApplication.class,args);
    }
}
