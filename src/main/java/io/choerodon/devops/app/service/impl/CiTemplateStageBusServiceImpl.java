package io.choerodon.devops.app.service.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.devops.app.service.CiTemplateJobBusService;
import io.choerodon.devops.app.service.CiTemplateStageBusService;
import io.choerodon.devops.app.service.CiTemplateStepBusService;
import io.choerodon.devops.infra.dto.CiTemplateJobStepRelDTO;
import io.choerodon.devops.infra.dto.CiTemplateStageJobRelDTO;
import io.choerodon.devops.infra.mapper.CiTemplateJobStepRelBusMapper;
import io.choerodon.devops.infra.mapper.CiTemplateStageBusMapper;
import io.choerodon.devops.infra.mapper.CiTemplateStageJobRelBusMapper;
import io.choerodon.devops.infra.mapper.CiTemplateStageJobRelMapper;

@Service
public class CiTemplateStageBusServiceImpl implements CiTemplateStageBusService {

    @Autowired
    private CiTemplateStageJobRelMapper ciTemplateStageJobRelMapper;

    @Autowired
    private CiTemplateJobStepRelBusMapper ciTemplateJobStepRelBusMapper;

    @Autowired
    private CiTemplateStageBusMapper ciTemplateStageBusMapper;

    @Autowired
    private CiTemplateStepBusService ciTemplateStepBusService;

    @Autowired
    private CiTemplateJobBusService ciTemplateJobBusService;

    @Autowired
    private CiTemplateStageJobRelBusMapper ciTemplateStageJobRelBusMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStageById(Long projectId, Long ciTemplateStageId) {

        Set<Long> stageJobRelIds = new HashSet<>();
        Set<Long> ciTemplatejobIds = new HashSet<>();
        Set<Long> ciStepTemplateIds = new HashSet<>();
        Set<Long> jobStepRelIds = new HashSet<>();

        CiTemplateStageJobRelDTO ciTemplateStageJobRelDTO = new CiTemplateStageJobRelDTO();
        ciTemplateStageJobRelDTO.setCiTemplateStageId(ciTemplateStageId);
        List<CiTemplateStageJobRelDTO> stageJobRelDTOS = ciTemplateStageJobRelMapper.select(ciTemplateStageJobRelDTO);

        if (!CollectionUtils.isEmpty(stageJobRelDTOS)) {
            stageJobRelIds.addAll(stageJobRelDTOS.stream().map(CiTemplateStageJobRelDTO::getId).collect(Collectors.toSet()));
            ciTemplatejobIds.addAll(stageJobRelDTOS.stream().map(CiTemplateStageJobRelDTO::getCiTemplateJobId).collect(Collectors.toList()));
        }
        List<CiTemplateJobStepRelDTO> ciTemplateJobStepRelDTOS = Collections.EMPTY_LIST;
        if (!CollectionUtils.isEmpty(ciTemplatejobIds)) {
            ciTemplateJobStepRelDTOS = ciTemplateJobStepRelBusMapper.selectNonVisibilityByJobIds(ciTemplatejobIds);
        }
        if (!CollectionUtils.isEmpty(ciTemplateJobStepRelDTOS)) {
            jobStepRelIds.addAll(ciTemplateJobStepRelDTOS.stream().map(CiTemplateJobStepRelDTO::getId).collect(Collectors.toSet()));
            // 这个步骤id集合里面可能包含了可见的步骤id 删除的时候要做判断
            ciStepTemplateIds.addAll(ciTemplateJobStepRelDTOS.stream().map(CiTemplateJobStepRelDTO::getCiTemplateStepId).collect(Collectors.toSet()));
        }
        // 删除不可见的 step模板  包含配置的删除
        ciTemplateStepBusService.deleteTemplateStepByIds(projectId, ciStepTemplateIds);
        // 删除不可见的 Job模板 包含配置的删除
        ciTemplateJobBusService.deleteTemplateJobByIds(ciTemplatejobIds);

        // 删除不可见任务和步骤的关联关系
        if (!CollectionUtils.isEmpty(jobStepRelIds)) {
            ciTemplateJobStepRelBusMapper.deleteByIds(jobStepRelIds);
        }
        //删除stage_job_rel
        if (!CollectionUtils.isEmpty(stageJobRelIds)) {
            ciTemplateStageJobRelBusMapper.deleteByIds(stageJobRelIds);
        }
        ciTemplateStageBusMapper.deleteByPrimaryKey(ciTemplateStageId);
    }
}
