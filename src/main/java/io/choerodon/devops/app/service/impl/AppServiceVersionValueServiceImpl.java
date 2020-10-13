package io.choerodon.devops.app.service.impl;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.AppServiceVersionValueService;
import io.choerodon.devops.infra.constant.ResourceCheckConstant;
import io.choerodon.devops.infra.dto.AppServiceVersionValueDTO;
import io.choerodon.devops.infra.mapper.AppServiceVersionValueMapper;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * Created by Sheep on 2019/7/12.
 */

@Service
public class AppServiceVersionValueServiceImpl implements AppServiceVersionValueService {


    @Autowired
    private AppServiceVersionValueMapper appServiceVersionValueMapper;

    @Override
    public AppServiceVersionValueDTO baseCreate(AppServiceVersionValueDTO appServiceVersionValueDTO) {
        if (appServiceVersionValueMapper.insert(appServiceVersionValueDTO) != 1) {
            throw new CommonException("error.version.value.insert");
        }
        return appServiceVersionValueDTO;
    }

    @Override
    public AppServiceVersionValueDTO baseUpdate(AppServiceVersionValueDTO appServiceVersionValueDTO) {
        MapperUtil.resultJudgedUpdateByPrimaryKey(appServiceVersionValueMapper, appServiceVersionValueDTO, "error.version.value.update");
        return appServiceVersionValueDTO;
    }


    @Override
    public AppServiceVersionValueDTO baseQuery(Long appServiceServiceValueId) {
        return appServiceVersionValueMapper.selectByPrimaryKey(appServiceServiceValueId);
    }

    @Override
    @Transactional
    public void baseDeleteById(Long appServiceServiceValueId) {
        Assert.notNull(appServiceServiceValueId, ResourceCheckConstant.ERROR_SERVICE_VERSION_VALUE_ID_IS_NULL);
        appServiceVersionValueMapper.deleteByPrimaryKey(appServiceServiceValueId);
    }

    @Override
    public void deleteByIds(Set<Long> valueIds) {

        appServiceVersionValueMapper.deleteByIds(valueIds);

    }


}
