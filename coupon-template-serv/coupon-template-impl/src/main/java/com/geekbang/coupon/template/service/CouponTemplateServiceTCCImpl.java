package com.geekbang.coupon.template.service;

import com.geekbang.coupon.template.api.beans.CouponTemplateInfo;
import com.geekbang.coupon.template.api.beans.PagedCouponTemplateInfo;
import com.geekbang.coupon.template.api.beans.TemplateSearchParams;
import com.geekbang.coupon.template.dao.entity.CouponTemplate;
import com.geekbang.coupon.template.service.intf.CouponTemplateServiceTCC;
import io.seata.rm.tcc.api.BusinessActionContext;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.Map;

public class CouponTemplateServiceTCCImpl implements CouponTemplateServiceTCC {
    @Override
    public CouponTemplateInfo createTeplate(CouponTemplateInfo templateInfo) {
        return null;
    }

    @Override
    public CouponTemplateInfo cloneTemplate(Long templateId) {
        return null;
    }

    @Override
    public PagedCouponTemplateInfo search(TemplateSearchParams params) {
        return null;
    }

    @Override
    public CouponTemplateInfo loadTemplateInfo(Long id) {
        return null;
    }

    @Override
    public void deleteTemplate(Long id) {

    }

    @Override
    public Map<Long, CouponTemplateInfo> getTemplateInfoMap(Collection<Long> ids) {
        return null;
    }

    @Override
    @Transactional
    public void deleteTemplateTCC(Long id) {
        CouponTemplate filter = CouponTemplate.builder()
                .available(true)
                .locked(false)
                .id(id)
                .build();
//        CouponTemplate template = t

    }

    @Override
    public void deleteTemplateCommit(BusinessActionContext context) {

    }

    @Override
    public void deleteTemplateCancel(BusinessActionContext context) {

    }
}
