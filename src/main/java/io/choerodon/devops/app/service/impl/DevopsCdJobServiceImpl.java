package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsCdJobService;
import io.choerodon.devops.infra.dto.DevopsCdJobDTO;
import io.choerodon.devops.infra.mapper.DevopsCdJobMapper;

import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.DevopsCdJobDTO;
import io.choerodon.devops.infra.mapper.DevopsCdJobMapper;

@Service
public class DevopsCdJobServiceImpl implements DevopsCdJobService {
    @Autowired
    private DevopsCdJobMapper devopsCdJobMapper;

    @Override
    public DevopsCdJobDTO create(DevopsCdJobDTO devopsCdJobDTO) {
        if (devopsCdJobMapper.insert(devopsCdJobDTO) != 1) {
            throw new CommonException("error.insert.cd.job");
        }
        return devopsCdJobMapper.selectByPrimaryKey(devopsCdJobDTO.getId());
    }
    @Override
    public List<DevopsCdJobDTO> listByPipelineId(Long pipelineId) {
        Assert.notNull(pipelineId, PipelineCheckConstant.ERROR_PIPELINE_IS_NULL);
        DevopsCdJobDTO devopsCdJobDTO = new DevopsCdJobDTO();
        devopsCdJobDTO.setPipelineIid(pipelineId);
        return  devopsCdJobMapper.select(devopsCdJobDTO);
    }
}
