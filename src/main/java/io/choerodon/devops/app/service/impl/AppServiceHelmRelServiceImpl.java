package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import io.choerodon.devops.app.service.AppServiceHelmRelService;
import io.choerodon.devops.infra.constant.ResourceCheckConstant;
import io.choerodon.devops.infra.dto.AppServiceHelmRelDTO;
import io.choerodon.devops.infra.mapper.AppServiceHelmRelMapper;

/**
 * 应用服务和helm配置的关联关系表(AppServiceHelmRel)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2022-07-15 10:55:52
 */
@Service
public class AppServiceHelmRelServiceImpl implements AppServiceHelmRelService {
    @Autowired
    private AppServiceHelmRelMapper appServiceHelmRelMapper;

    @Override
    public AppServiceHelmRelDTO queryByAppServiceId(Long appServiceId) {
        Assert.notNull(appServiceId, ResourceCheckConstant.DEVOPS_APP_SERVICE_ID_IS_NULL);

        AppServiceHelmRelDTO appServiceHelmRelDTO = new AppServiceHelmRelDTO();
        appServiceHelmRelDTO.setAppServiceId(appServiceId);
        return appServiceHelmRelMapper.selectOne(appServiceHelmRelDTO);
    }

    @Override
    public void batchInsertInNewTrans(List<AppServiceHelmRelDTO> appServiceHelmRelDTOToInsert) {
        appServiceHelmRelMapper.batchInsert(appServiceHelmRelDTOToInsert);
    }
}

