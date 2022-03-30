package com.geekbang.coupon.customer.config;

import com.alibaba.druid.pool.DruidDataSource;
import io.seata.rm.datasource.DataSourceProxy;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class SeataConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DruidDataSource druidDataSource(){
        return new DruidDataSource();
    }

    @Bean("datasource")
    @Primary //作为 javax.sql.DataSource 的默认代理类
    public DataSource dataSourceDelegation(DruidDataSource druidDataSource){
        return new DataSourceProxy(druidDataSource);
    }
}
