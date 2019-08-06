package io.choerodon.devops.app.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.AppServiceVersionValueService;
import io.choerodon.devops.infra.dto.AppServiceVersionValueDTO;
import io.choerodon.devops.infra.mapper.AppServiceVersionValueMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public AppServiceVersionValueDTO baseQuery(Long appServiceServiceValueId) {
        return appServiceVersionValueMapper.selectByPrimaryKey(appServiceServiceValueId);
    }



}
