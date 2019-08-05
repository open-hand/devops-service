package io.choerodon.devops.app.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.ApplicationVersionValueService;
import io.choerodon.devops.infra.dto.ApplicationVersionValueDTO;
import io.choerodon.devops.infra.mapper.ApplicationVersionValueMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Sheep on 2019/7/12.
 */

@Service
public class ApplicationVersionValueServiceImpl implements ApplicationVersionValueService {


    @Autowired
    private ApplicationVersionValueMapper applicationVersionValueMapper;

    @Override
    public ApplicationVersionValueDTO baseCreate(ApplicationVersionValueDTO applicationVersionValueDTO) {
        if (applicationVersionValueMapper.insert(applicationVersionValueDTO) != 1) {
            throw new CommonException("error.version.value.insert");
        }
        return applicationVersionValueDTO;
    }


    @Override
    public ApplicationVersionValueDTO baseQuery(Long appVersionValueId) {
        return applicationVersionValueMapper.selectByPrimaryKey(appVersionValueId);
    }



}
