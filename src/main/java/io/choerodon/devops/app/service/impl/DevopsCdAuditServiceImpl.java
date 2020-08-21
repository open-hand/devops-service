package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsCdAuditService;
import io.choerodon.devops.infra.dto.DevopsCdAuditDTO;
import io.choerodon.devops.infra.dto.DevopsCdJobDTO;
import io.choerodon.devops.infra.dto.DevopsCdStageDTO;
import io.choerodon.devops.infra.mapper.DevopsCdAuditMapper;
import io.choerodon.devops.infra.mapper.DevopsCdJobMapper;
import io.choerodon.devops.infra.mapper.DevopsCdStageMapper;
import io.choerodon.devops.infra.util.TypeUtil;

@Service
public class DevopsCdAuditServiceImpl implements DevopsCdAuditService {

    @Autowired
    private DevopsCdAuditMapper devopsCdAuditMapper;
    @Autowired
    private DevopsCdStageMapper devopsCdStageMapper;
    @Autowired
    private DevopsCdJobMapper devopsCdJobMapper;

    @Override
    @Transactional
    public void baseCreate(DevopsCdAuditDTO devopsCdAuditDTO) {
        if (devopsCdAuditMapper.insertSelective(devopsCdAuditDTO) != 1) {
            throw new CommonException("error.insert.pipeline.user");
        }
    }


    @Override
    public List<DevopsCdAuditDTO> baseListByOptions(Long pipelineId, Long stageId, Long taskId) {
        DevopsCdAuditDTO devopsCdAuditDTO = new DevopsCdAuditDTO(pipelineId, stageId, taskId);
        return devopsCdAuditMapper.select(devopsCdAuditDTO);
    }

    @Override
    public void baseDelete(DevopsCdAuditDTO devopsCdAuditDTO) {
        if (TypeUtil.checkObjAllFieldsIsNull(devopsCdAuditDTO) ||
                (devopsCdAuditDTO.getId() == null
                        && devopsCdAuditDTO.getCdStageId() == null
                        && devopsCdAuditDTO.getCdJobId() == null
                        && devopsCdAuditDTO.getUserId() == null
                        && devopsCdAuditDTO.getPipelineId() == null
                )) {
            throw new CommonException("error.delete.pipeline.user.relation");
        }
        devopsCdAuditMapper.delete(devopsCdAuditDTO);
    }

    @Override
    public void fixProjectId() {
        List<DevopsCdAuditDTO> devopsCdAuditDTOS = devopsCdAuditMapper.selectAll();
        Set<Long> cdStageIds = devopsCdAuditDTOS.stream().filter(i -> i.getCdStageId() != null).map(DevopsCdAuditDTO::getCdStageId).collect(Collectors.toSet());
        Set<Long> cdJobIds = devopsCdAuditDTOS.stream().filter(i -> i.getCdJobId() != null).map(DevopsCdAuditDTO::getCdJobId).collect(Collectors.toSet());

        List<DevopsCdStageDTO> devopsCdStageDTOS = devopsCdStageMapper.selectByIds(StringUtils.join(cdStageIds, ","));
        List<DevopsCdJobDTO> devopsCdJobDTOS = devopsCdJobMapper.selectByIds(StringUtils.join(cdJobIds, ","));

        devopsCdStageDTOS.forEach(i -> {
            devopsCdAuditMapper.updateProjectIdByStageId(i.getProjectId(), i.getId());
        });

        devopsCdJobDTOS.forEach(i -> {
            devopsCdAuditMapper.updateProjectIdByJobId(i.getProjectId(), i.getId());
        });
    }
}