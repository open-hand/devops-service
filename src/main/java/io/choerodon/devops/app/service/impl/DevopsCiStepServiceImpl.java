package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.devops.app.eventhandler.pipeline.step.AbstractDevopsCiStepHandler;
import io.choerodon.devops.app.service.DevopsCiStepService;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.DevopsCiStepDTO;
import io.choerodon.devops.infra.mapper.DevopsCiStepMapper;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/11/29 14:22
 */
@Service
public class DevopsCiStepServiceImpl implements DevopsCiStepService {

    private static final String DEVOPS_SAVE_CI_STEP_FAILED = "devops.save.ci.step.failed";

    // dependency service
    @Autowired
    private DevopsCiStepOperator devopsCiStepOperator;
    // dependency mapper

    @Autowired
    private DevopsCiStepMapper devopsCiStepMapper;

    @Override
    @Transactional
    public void baseCreate(DevopsCiStepDTO devopsCiStepDTO) {
        MapperUtil.resultJudgedInsertSelective(devopsCiStepMapper,
                devopsCiStepDTO,
                DEVOPS_SAVE_CI_STEP_FAILED);
    }

    @Override
    public List<DevopsCiStepDTO> listByJobIds(List<Long> jobIds) {
        return devopsCiStepMapper.listByJobIds(jobIds);
    }

    @Override
    @Transactional
    public void deleteByJobIds(List<Long> jobIds) {
        List<DevopsCiStepDTO> devopsCiStepDTOS = devopsCiStepMapper.listByJobIds(jobIds);
        Map<String, List<DevopsCiStepDTO>> stepMap = devopsCiStepDTOS.stream().collect(Collectors.groupingBy(DevopsCiStepDTO::getType));
        // 按类型级联删除
        stepMap.forEach((k, v) -> {
            AbstractDevopsCiStepHandler handler = devopsCiStepOperator.getHandler(k);
            handler.batchDeleteCascade(v);
        });
    }

    @Override
    @Transactional
    public void batchDeleteByIds(Set<Long> ids) {
        devopsCiStepMapper.batchDeleteByIds(ids);
    }

    @Override
    public List<DevopsCiStepDTO> listByJobId(Long jobId) {
        Assert.notNull(jobId, PipelineCheckConstant.DEVOPS_JOB_ID_IS_NULL);
        DevopsCiStepDTO devopsCiStepDTO = new DevopsCiStepDTO();
        devopsCiStepDTO.setDevopsCiJobId(jobId);
        return devopsCiStepMapper.select(devopsCiStepDTO);
    }

    @Override
    public Long queryAppServiceIdByStepId(Long id) {
        return devopsCiStepMapper.queryAppServiceIdByStepId(id);
    }
}
