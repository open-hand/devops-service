package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.infra.constant.ExceptionConstants.CiApiTestCode.DEVOPS_CI_API_TEST_INFO_SAVE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.devops.app.service.DevopsCiApiTestInfoService;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.DevopsCiApiTestInfoDTO;
import io.choerodon.devops.infra.mapper.DevopsCiApiTestInfoMapper;
import io.choerodon.devops.infra.util.MapperUtil;

@Service
public class DevopsCiApiTestInfoServiceImpl implements DevopsCiApiTestInfoService {
    @Autowired
    private DevopsCiApiTestInfoMapper devopsCiApiTestInfoMapper;

    @Override
    public void insert(DevopsCiApiTestInfoDTO devopsCiApiTestInfoDTO) {
        MapperUtil.resultJudgedInsert(devopsCiApiTestInfoMapper, devopsCiApiTestInfoDTO, DEVOPS_CI_API_TEST_INFO_SAVE);
    }

    @Override
    public DevopsCiApiTestInfoDTO selectByPrimaryKey(Long id) {
        return devopsCiApiTestInfoMapper.selectByPrimaryKey(id);
    }

    @Override
    public DevopsCiApiTestInfoDTO selectById(Long id) {
        return devopsCiApiTestInfoMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConfigByPipelineId(Long ciPipelineId) {
        Assert.notNull(ciPipelineId, PipelineCheckConstant.DEVOPS_PIPELINE_ID_IS_NULL);

        DevopsCiApiTestInfoDTO devopsCiApiTestInfoDTO = new DevopsCiApiTestInfoDTO();
        devopsCiApiTestInfoDTO.setCiPipelineId(ciPipelineId);
        devopsCiApiTestInfoMapper.delete(devopsCiApiTestInfoDTO);
    }
}
