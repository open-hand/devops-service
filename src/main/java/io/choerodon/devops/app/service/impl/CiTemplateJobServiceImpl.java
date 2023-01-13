package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import io.choerodon.devops.api.vo.DevopsCiJobVO;
import io.choerodon.devops.api.vo.DevopsCiStepVO;
import io.choerodon.devops.api.vo.template.CiTemplateJobVO;
import io.choerodon.devops.api.vo.template.CiTemplateStepVO;
import io.choerodon.devops.app.eventhandler.pipeline.job.AbstractJobHandler;
import io.choerodon.devops.app.eventhandler.pipeline.job.JobOperator;
import io.choerodon.devops.app.eventhandler.pipeline.step.AbstractDevopsCiStepHandler;
import io.choerodon.devops.app.service.CiTemplateJobGroupService;
import io.choerodon.devops.app.service.CiTemplateJobService;
import io.choerodon.devops.app.service.CiTemplateStepService;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.CiTemplateJobDTO;
import io.choerodon.devops.infra.dto.CiTemplateJobGroupDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.enums.CiTriggerType;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.CiTemplateJobMapper;
import io.choerodon.devops.infra.util.ConvertUtils;

/**
 * 流水线任务模板表(CiTemplateJob)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:16
 */
@Service
public class CiTemplateJobServiceImpl implements CiTemplateJobService {
    @Autowired
    private CiTemplateJobMapper ciTemplateJobmapper;
    @Autowired
    private CiTemplateJobGroupService ciTemplateJobGroupService;
    @Autowired
    private DevopsCiStepOperator devopsCiStepOperator;
    @Autowired
    private CiTemplateStepService ciTemplateStepService;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private JobOperator jobOperator;


    @Override
    public List<CiTemplateJobVO> listByStageIds(Set<Long> stageIds) {
        return ciTemplateJobmapper.listByStageIds(stageIds);
    }

    @Override
    public List<CiTemplateJobVO> listByStageIdWithGroupInfo(Long stageId) {
        Assert.notNull(stageId, PipelineCheckConstant.DEVOPS_STAGE_ID_IS_NULL);

        List<CiTemplateJobDTO> ciTemplateJobDTOList = ciTemplateJobmapper.listByStageId(stageId);

        List<CiTemplateJobVO> ciTemplateJobVOS = ConvertUtils.convertList(ciTemplateJobDTOList, CiTemplateJobVO.class);
        ciTemplateJobVOS.forEach(ciTemplateJobVO -> {
            CiTemplateJobGroupDTO ciTemplateJobGroupDTO = ciTemplateJobGroupService.baseQuery(ciTemplateJobVO.getGroupId());
            ciTemplateJobVO.setCiTemplateJobGroupDTO(ciTemplateJobGroupDTO);
        });

        return ciTemplateJobVOS;
    }

    @Override
    public List<DevopsCiJobVO> listJobsByGroupId(Long projectId, Long groupId) {
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);
        List<CiTemplateJobDTO> templateJobDTOS = ciTemplateJobmapper.listByTenantIdAndGroupId(projectId, projectDTO.getOrganizationId(), groupId);

        if (CollectionUtils.isEmpty(templateJobDTOS)) {
            return new ArrayList<>();
        }

        List<DevopsCiJobVO> devopsCiJobVOList = new ArrayList<>();

        List<CiTemplateJobVO> ciTemplateJobVOList = ConvertUtils.convertList(templateJobDTOS, CiTemplateJobVO.class);
        // 填充任务中的步骤信息
        Set<Long> jobIds = templateJobDTOS.stream().map(CiTemplateJobDTO::getId).collect(Collectors.toSet());
        List<CiTemplateStepVO> ciTemplateStepVOS = ciTemplateStepService.listByJobIds(jobIds);
        Map<Long, List<CiTemplateStepVO>> jobStepsMap = ciTemplateStepVOS.stream().collect(Collectors.groupingBy(CiTemplateStepVO::getCiTemplateJobId));
        ciTemplateJobVOList.forEach(templateJobVO -> {
            DevopsCiJobVO devopsCiJobVO = ConvertUtils.convertObject(templateJobVO, DevopsCiJobVO.class);
            devopsCiJobVO.setTriggerType(CiTriggerType.REFS.value());
            // 填充任务配置
            AbstractJobHandler handler = jobOperator.getHandler(templateJobVO.getType());
            if (handler != null) {
                handler.fillJobTemplateConfigInfo(devopsCiJobVO);
            }
            // 填充步骤信息
            List<CiTemplateStepVO> ciTemplateStepVOList = jobStepsMap.get(templateJobVO.getId());
            if (!CollectionUtils.isEmpty(ciTemplateStepVOList)) {
                List<DevopsCiStepVO> devopsCiStepVOList = new ArrayList<>();
                ciTemplateStepVOList
                        .stream()
                        .sorted(Comparator.comparing(CiTemplateStepVO::getSequence))
                        .forEach(ciTemplateStepVO -> {
                            // 添加步骤关联的配置信息
                            DevopsCiStepVO devopsCiStepVO = ConvertUtils.convertObject(ciTemplateStepVO, DevopsCiStepVO.class);
                            AbstractDevopsCiStepHandler stepHandler = devopsCiStepOperator.getHandlerOrThrowE(devopsCiStepVO.getType());
                            stepHandler.fillTemplateStepConfigInfo(devopsCiStepVO);
                            devopsCiStepVOList.add(devopsCiStepVO);
                        });
                devopsCiJobVO.setDevopsCiStepVOList(devopsCiStepVOList);
            }
            devopsCiJobVOList.add(devopsCiJobVO);
        });
        return devopsCiJobVOList;
    }
}

