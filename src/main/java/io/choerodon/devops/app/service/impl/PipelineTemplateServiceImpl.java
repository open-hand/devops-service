package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.pipeline.PipelineTemplateCompositeVO;
import io.choerodon.devops.api.vo.template.CiTemplateJobVO;
import io.choerodon.devops.api.vo.template.CiTemplateStepVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.enums.CiTriggerType;
import io.choerodon.devops.infra.enums.StageType;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.PipelineTemplateMapper;
import io.choerodon.devops.infra.util.ConvertUtils;

/**
 * 流水线模板表(PipelineTemplate)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 16:54:19
 */
@Service
public class PipelineTemplateServiceImpl implements PipelineTemplateService {

    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;

    @Autowired
    private CiTemplateStageService ciTemplateStageService;

    @Autowired
    private CiTemplateJobService ciTemplateJobService;
    @Autowired
    private CiTemplateCategoryService ciTemplateCategoryService;
    @Autowired
    private CiTemplateStepService ciTemplateStepService;
    @Autowired
    private CiTemplateJobStepRelService ciTemplateJobStepRelService;
    @Autowired
    private CiTemplateJobGroupService ciTemplateJobGroupService;
    @Autowired
    private CiTemplateVariableService ciTemplateVariableService;

    @Autowired
    private PipelineTemplateMapper pipelineTemplatemapper;
    @Autowired
    private DevopsCiStepOperator devopsCiStepOperator;


    @Override
    public PipelineTemplateCompositeVO listTemplateWithLanguage(Long projectId) {
        List<PipelineTemplateVO> pipelineTemplateVOS = listTemplateForProject(projectId);

        Set<Long> categoryIds = pipelineTemplateVOS.stream().map(PipelineTemplateVO::getCiTemplateCategoryId).collect(Collectors.toSet());
        List<CiTemplateCategoryDTO> ciTemplateCategoryDTOS = ciTemplateCategoryService.listByIds(categoryIds);
        Map<Long, CiTemplateCategoryDTO> templateCategoryDTOMap = ciTemplateCategoryDTOS.stream().collect(Collectors.toMap(CiTemplateCategoryDTO::getId, Function.identity()));

        pipelineTemplateVOS.forEach(pipelineTemplateVO -> {
            CiTemplateCategoryDTO ciTemplateCategoryDTO = templateCategoryDTOMap.get(pipelineTemplateVO.getCiTemplateCategoryId());
            pipelineTemplateVO.setCiTemplateCategoryDTO(ciTemplateCategoryDTO);
        });

        return new PipelineTemplateCompositeVO(ciTemplateCategoryDTOS, pipelineTemplateVOS);
    }

    @Override
    public PipelineTemplateDTO baseQuery(Long id) {
        return pipelineTemplatemapper.selectByPrimaryKey(id);
    }


    @Override
    public List<PipelineTemplateVO> listTemplateForProject(Long projectId) {
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);
        return pipelineTemplatemapper.listTemplateForProject(projectDTO.getOrganizationId());
    }

    @Override
    public CiCdPipelineVO queryPipelineInfoByTemplateId(Long projectId, Long templateId) {

        PipelineTemplateDTO pipelineTemplateDTO = baseQuery(templateId);

        // 查询模板下的阶段
        List<CiTemplateStageDTO> ciTemplateStageDTOS = ciTemplateStageService.listByPipelineTemplateId(templateId);
        Set<Long> stageIds = ciTemplateStageDTOS.stream().map(CiTemplateStageDTO::getId).collect(Collectors.toSet());

        // 查询阶段下的任务
        List<CiTemplateJobVO> ciTemplateJobVOList = ciTemplateJobService.listByStageIds(stageIds);
        Map<Long, List<CiTemplateJobVO>> stageJobListMap = ciTemplateJobVOList.stream().collect(Collectors.groupingBy(CiTemplateJobVO::getRelateStageId));

        // 查询任务所属的分组
        Set<Long> groupIds = ciTemplateJobVOList.stream().map(CiTemplateJobVO::getGroupId).collect(Collectors.toSet());
        List<CiTemplateJobGroupDTO> ciTemplateJobGroupDTOS = ciTemplateJobGroupService.listByIds(groupIds);
        Map<Long, CiTemplateJobGroupDTO> groupMap = ciTemplateJobGroupDTOS.stream().collect(Collectors.toMap(CiTemplateJobGroupDTO::getId, Function.identity()));

        // 查询任务下的步骤
        Set<Long> jobIds = ciTemplateJobVOList.stream().map(CiTemplateJobVO::getId).collect(Collectors.toSet());
        List<CiTemplateStepVO> ciTemplateStepVOList = ciTemplateJobStepRelService.listByJobIds(jobIds);
        Map<Long, List<CiTemplateStepVO>> jobStepMap = ciTemplateStepVOList.stream().collect(Collectors
                .groupingBy(CiTemplateStepVO::getCiTemplateJobId));

        // 组装返回给前端的流水线对象
        List<DevopsCiStageVO> devopsCiStageVOList = new ArrayList<>();

        ciTemplateStageDTOS
                .stream()
                .sorted(Comparator.comparing(CiTemplateStageDTO::getSequence))
                .forEach(ciTemplateStageDTO -> {
                    DevopsCiStageVO devopsCiStageVO = new DevopsCiStageVO();
                    devopsCiStageVO.setName(ciTemplateStageDTO.getName());
                    devopsCiStageVO.setSequence(ciTemplateStageDTO.getSequence());
                    devopsCiStageVO.setType(StageType.CI.getType());
                    List<CiTemplateJobVO> stageTemplateJobVOList = stageJobListMap.get(ciTemplateStageDTO.getId());

                    List<DevopsCiJobVO> devopsCiJobVOList = new ArrayList<>();
                    stageTemplateJobVOList.forEach(stageTemplateJobVO -> {
                        DevopsCiJobVO devopsCiJobVO = ConvertUtils.convertObject(stageTemplateJobVO, DevopsCiJobVO.class);

                        CiTemplateJobGroupDTO ciTemplateJobGroupDTO = groupMap.get(stageTemplateJobVO.getGroupId());
                        devopsCiJobVO.setCiTemplateJobGroupDTO(ciTemplateJobGroupDTO);
                        devopsCiJobVO.setGroupType(ciTemplateJobGroupDTO.getType());
                        devopsCiJobVO.setTriggerType(CiTriggerType.REFS.value());

                        // 组装步骤信息
                        List<CiTemplateStepVO> ciTemplateStepVOS = jobStepMap.get(stageTemplateJobVO.getId());
                        List<DevopsCiStepVO> devopsCiStepVOList = new ArrayList<>();
                        ciTemplateStepVOS.forEach(ciTemplateStepVO -> {
                            // 添加步骤关联的配置信息
                            AbstractDevopsCiStepHandler ciTemplateStepHandler = devopsCiStepOperator.getHandlerOrThrowE(ciTemplateStepVO.getType());
                            ciTemplateStepHandler.fillTemplateStepConfigInfo(ciTemplateStepVO);
                            if (Boolean.FALSE.equals(ciTemplateStepHandler.isComplete(ciTemplateStepVO))) {
                                devopsCiJobVO.setCompleted(false);
                            }

                            devopsCiStepVOList.add(ConvertUtils.convertObject(ciTemplateStepVO, DevopsCiStepVO.class));
                        });
                        devopsCiJobVO.setDevopsCiStepVOList(devopsCiStepVOList);
                        devopsCiJobVOList.add(devopsCiJobVO);
                    });

                    devopsCiStageVO.setJobList(devopsCiJobVOList);
                    devopsCiStageVOList.add(devopsCiStageVO);
                });

        CiCdPipelineVO ciCdPipelineVO = new CiCdPipelineVO();
        ciCdPipelineVO.setImage(pipelineTemplateDTO.getImage());
        ciCdPipelineVO.setName(pipelineTemplateDTO.getName());
        ciCdPipelineVO.setVersionName(pipelineTemplateDTO.getVersionName());
        ciCdPipelineVO.setDevopsCiStageVOS(devopsCiStageVOList);

        // 设置流水线变量
        List<CiTemplateVariableDTO> ciTemplateVariableDTOS = ciTemplateVariableService.listByTemplateId(templateId);
        List<DevopsCiPipelineVariableDTO> devopsCiPipelineVariableDTOS = ConvertUtils.convertList(ciTemplateVariableDTOS, DevopsCiPipelineVariableDTO.class);
        ciCdPipelineVO.setDevopsCiPipelineVariableDTOList(devopsCiPipelineVariableDTOS);
        return ciCdPipelineVO;
    }


}

