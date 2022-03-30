package com.geekbang.coupon.template.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.geekbang.coupon.template.api.beans.CouponTemplateInfo;
import com.geekbang.coupon.template.api.beans.PagedCouponTemplateInfo;
import com.geekbang.coupon.template.api.beans.TemplateSearchParams;
import com.geekbang.coupon.template.service.intf.CouponTemplateService;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/template")
public class CouponTemplateController {

    @Autowired
    public CouponTemplateService couponTemplateService;

    //创建优惠券
    @PostMapping("/createTemplate")
    public CouponTemplateInfo createTemplate(@Valid @RequestBody CouponTemplateInfo request){
        log.info("Create coupon template: data={}", request);
        return couponTemplateService.createTeplate(request);
    }
    @PostMapping("/cloneTemplate")
    public CouponTemplateInfo cloneTemplate(@RequestParam("id") Long templateId) {
        log.info("Clone coupon template: data={}", templateId);
        return couponTemplateService.cloneTemplate(templateId);
    }

    // 读取优惠券
    @GetMapping("/getTemplate")
    @SentinelResource(value = "getTemplate")
    public CouponTemplateInfo getTemplate(@RequestParam("id") Long id){
        log.info("Load template, id={}", id);
        return couponTemplateService.loadTemplateInfo(id);
    }

    // 搜索模板
    @PostMapping("/search")
    public PagedCouponTemplateInfo search(@Valid @RequestBody TemplateSearchParams request) {
        log.info("search templates, payload={}", request);
        return couponTemplateService.search(request);
    }

    // 优惠券无效化
    @DeleteMapping("/deleteTemplate")
    @SentinelResource(value = "deleteTemplate")
    public void deleteTemplate(@RequestParam("id") Long id){
        log.info("Load template, id={}", id);
        couponTemplateService.deleteTemplate(id);
    }

    //
    @PostMapping("/getTemplateInfoMap")
    @SentinelResource(value = "getTemplateMap",fallback = "getTemplateMap_fallback", blockHandler = "getTemplateMap_block")
    public Map<Long, CouponTemplateInfo> getTemplateMap(@RequestParam("ids") Collection<Long> ids){
        log.info("getTemplateInBatch: {}", JSON.toJSONString(ids));
        return couponTemplateService.getTemplateInfoMap(ids);
    }

    //如果服务抛出的是BlackException 是不回调用fallback方法的，只会调用black进行降级操作
    //接口被降级后会调用的方法
    public Map<Long, CouponTemplateInfo> getTemplateMap_block(@RequestParam("ids") Collection<Long> ids){
        log.info("this api getTemplateInfoMap is blocked!");
        return Maps.newHashMap();
    }

    public Map<Long, CouponTemplateInfo> getTemplateMap_fallback(@RequestParam("ids") Collection<Long> ids){
        log.info("this api is fallbacked!被降级");
        return Maps.newHashMap();
    }
}
