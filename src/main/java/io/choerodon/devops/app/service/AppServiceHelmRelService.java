package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.infra.dto.AppServiceHelmRelDTO;

/**
 * 应用服务和helm配置的关联关系表(AppServiceHelmRel)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2022-07-15 10:55:52
 */
public interface AppServiceHelmRelService {

    AppServiceHelmRelDTO queryByAppServiceId(Long appServiceId);

    /**
     * 批量插入
     * @param appServiceHelmRelDTOToInsert
     */
    void batchInsertInNewTrans(List<AppServiceHelmRelDTO> appServiceHelmRelDTOToInsert);
}

