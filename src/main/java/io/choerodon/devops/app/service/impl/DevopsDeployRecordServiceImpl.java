package io.choerodon.devops.app.service.impl;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.app.service.DevopsDeployRecordService;
import io.choerodon.devops.infra.dto.DevopsDeployRecordDTO;
import io.choerodon.devops.infra.mapper.DevopsDeployRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Sheep on 2019/7/29.
 */

@Service
public class DevopsDeployRecordServiceImpl implements DevopsDeployRecordService {


    @Autowired
    private DevopsDeployRecordMapper devopsDeployRecordMapper;

    @Override
    public DevopsDeployRecordDTO basePageByProjectId(Long projectId, PageRequest pageRequest) {


        return null;
    }
}
