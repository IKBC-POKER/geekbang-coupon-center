package com.geekbang.coupon.template.service.intf;

import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.BusinessActionContextParameter;
import io.seata.rm.tcc.api.LocalTCC;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;

@LocalTCC  //用来修饰实现了二阶段提交的本地 TCC 接口
public interface CouponTemplateServiceTCC extends CouponTemplateService {

    //解标识当前方法使用 TCC 模式管理事务提交
    @TwoPhaseBusinessAction(name = "deleteTemplateTCC", commitMethod = "deleteTemplateCommit", rollbackMethod = "deleteTemplateCancel")
    void deleteTemplateTCC(@BusinessActionContextParameter(paramName = "id") Long id);

    void deleteTemplateCommit(BusinessActionContext context);

    void deleteTemplateCancel(BusinessActionContext context);
}
