package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.pipeline.PipelineTemplateCompositeVO;
import io.choerodon.devops.api.vo.template.CiTemplateJobStepRelVO;
import io.choerodon.devops.api.vo.template.CiTemplateJobVO;
import io.choerodon.devops.api.vo.template.CiTemplateStepVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.CiTemplateJobGroupDTO;
import io.choerodon.devops.infra.dto.CiTemplateLanguageDTO;
import io.choerodon.devops.infra.dto.CiTemplateStageDTO;
import io.choerodon.devops.infra.dto.PipelineTemplateDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
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
    private CiTemplateStageJobRelService ciTemplateStageJobRelService;
    @Autowired
    private CiTemplateLanguageService ciTemplateLanguageService;
    @Autowired
    private CiTemplateStepService ciTemplateStepService;
    @Autowired
    private CiTemplateJobStepRelService ciTemplateJobStepRelService;
    @Autowired
    private CiTemplateJobGroupService ciTemplateJobGroupService;

    @Autowired
    private DevopsCiStepOperator devopsCiStepOperator;

    @Autowired
    private PipelineTemplateMapper pipelineTemplatemapper;


    @Override
    public PipelineTemplateCompositeVO listTemplateWithLanguage(Long projectId) {
        List<PipelineTemplateVO> pipelineTemplateVOS = listTemplateForProject(projectId);

        Set<Long> languageIds = pipelineTemplateVOS.stream().map(PipelineTemplateVO::getCiTemplateLanguageId).collect(Collectors.toSet());
        List<CiTemplateLanguageDTO> ciTemplateLanguageDTOS = ciTemplateLanguageService.listByIds(languageIds);

        return new PipelineTemplateCompositeVO(ciTemplateLanguageDTOS, pipelineTemplateVOS);
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
        List<CiTemplateJobStepRelVO> ciTemplateJobStepRelVOS = ciTemplateJobStepRelService.listByJobIds(jobIds);
        Map<Long, List<CiTemplateStepVO>> jobStepMap = ciTemplateJobStepRelVOS.stream().collect(Collectors
                .groupingBy(CiTemplateJobStepRelVO::getCiTemplateJobId,
                        Collectors.mapping(CiTemplateJobStepRelVO::getCiTemplateStepVO, Collectors.toList())));

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

                        // 组装步骤信息
                        List<CiTemplateStepVO> ciTemplateStepVOS = jobStepMap.get(stageTemplateJobVO.getId());
                        ciTemplateStepVOS.forEach(ciTemplateStepVO -> {
                            DevopsCiStepVO devopsCiStepVO = ConvertUtils.convertObject(ciTemplateStepVO, DevopsCiStepVO.class);
                            AbstractDevopsCiStepHandler stepHandler = devopsCiStepOperator.getHandlerOrThrowE(devopsCiStepVO.getType());
                        });


                        devopsCiJobVOList.add(devopsCiJobVO);
                    });
                    devopsCiStageVO.setJobList(devopsCiJobVOList);

                    List<DevopsCiJobVO> devopsCiJobVOS = ConvertUtils.convertList(stageTemplateJobVOList, DevopsCiJobVO.class);

                    devopsCiStageVOList.add(devopsCiStageVO);
                });


        ciTemplateStageDTOS.forEach(ciTemplateStageDTO -> {

            // 查询阶段下的任务
            List<CiTemplateJobVO> ciTemplateJobVOS = ciTemplateJobService.listByStageIdWithGroupInfo(ciTemplateStageDTO.getId());
            ciTemplateJobVOS.forEach(ciTemplateJobVO -> {

            });

        });

        return null;
    }


}

