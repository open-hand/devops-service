package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.api.vo.template.CiTemplateStepCategoryVO;
import io.choerodon.devops.api.vo.template.CiTemplateStepVO;
import io.choerodon.devops.app.eventhandler.pipeline.step.AbstractDevopsCiStepHandler;
import io.choerodon.devops.app.service.CiTemplateStepCategoryService;
import io.choerodon.devops.app.service.CiTemplateStepService;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.CiTemplateStepMapper;

/**
 * 流水线步骤模板(CiTemplateStep)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:21
 */
@Service
public class CiTemplateStepServiceImpl implements CiTemplateStepService {

    @Autowired
    private CiTemplateStepCategoryService ciTemplateStepCategoryService;
    @Autowired
    private CiTemplateStepMapper ciTemplateStepMapper;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private DevopsCiStepOperator devopsCiStepOperator;


    @Override
    public List<CiTemplateStepVO> listByJobIds(Set<Long> jobIds) {
        return ciTemplateStepMapper.listByJobIds(jobIds);
    }

    @Override
    public List<CiTemplateStepVO> listStepsByTemplateJobId(Long projectId, Long templateJobId) {
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);
        List<CiTemplateStepVO> ciTemplateStepVOS = ciTemplateStepMapper.listStepsByTemplateJobId(projectDTO.getOrganizationId(), templateJobId);
        ciTemplateStepVOS.forEach(ciTemplateStepVO -> {
            AbstractDevopsCiStepHandler devopsCiStepHandler = devopsCiStepOperator.getHandlerOrThrowE(ciTemplateStepVO.getType());
            devopsCiStepHandler.fillTemplateStepConfigInfo(ciTemplateStepVO);
        });

        return ciTemplateStepVOS;
    }

    @Override
    public List<CiTemplateStepCategoryVO> listStepsByProjectId(Long projectId) {
        // 先查询所有步骤
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);
        List<CiTemplateStepVO> ciTemplateStepVOS = ciTemplateStepMapper.listStepsByOrganizationIdId(projectId, projectDTO.getOrganizationId());
        ciTemplateStepVOS.forEach(ciTemplateStepVO -> {
            AbstractDevopsCiStepHandler devopsCiStepHandler = devopsCiStepOperator.getHandlerOrThrowE(ciTemplateStepVO.getType());
            devopsCiStepHandler.fillTemplateStepConfigInfo(ciTemplateStepVO);
        });
        Map<Long, List<CiTemplateStepVO>> categoryStepMap = ciTemplateStepVOS.stream().collect(Collectors.groupingBy(CiTemplateStepVO::getCategoryId));

        Set<Long> cids = ciTemplateStepVOS.stream().map(CiTemplateStepVO::getCategoryId).collect(Collectors.toSet());
        List<CiTemplateStepCategoryVO> ciTemplateStepCategoryVOS = ciTemplateStepCategoryService.listByIds(cids);


        //将步骤分组排序
        List<CiTemplateStepCategoryVO> customJobGroupVOS = ciTemplateStepCategoryVOS.stream().filter(ciTemplateStepCategoryVO -> !ciTemplateStepCategoryVO.getBuiltIn()).collect(Collectors.toList());

        List<CiTemplateStepCategoryVO> otherVos = ciTemplateStepCategoryVOS.stream()
                .filter(CiTemplateStepCategoryVO::getBuiltIn)
                .filter(ciTemplateStepCategoryVO -> StringUtils.equalsIgnoreCase(ciTemplateStepCategoryVO.getName(), "其他"))
                .collect(Collectors.toList());
        List<CiTemplateStepCategoryVO> firstVos = ciTemplateStepCategoryVOS
                .stream()
                .filter(ciTemplateStepCategoryVO -> StringUtils.equalsIgnoreCase(ciTemplateStepCategoryVO.getName(), "构建"))
                .collect(Collectors.toList());

        List<CiTemplateStepCategoryVO> groupVOS = ciTemplateStepCategoryVOS.stream().filter(CiTemplateStepCategoryVO::getBuiltIn)
                .filter(ciTemplateStepCategoryVO -> !StringUtils.equalsIgnoreCase(ciTemplateStepCategoryVO.getName(), "其他"))
                .filter(ciTemplateStepCategoryVO -> !StringUtils.equalsIgnoreCase(ciTemplateStepCategoryVO.getName(), "构建"))
                .sorted(Comparator.comparing(CiTemplateStepCategoryVO::getId))
                .collect(Collectors.toList());
        List<CiTemplateStepCategoryVO> templateStepCategoryVOS = new ArrayList<>();
        templateStepCategoryVOS.addAll(firstVos);
        templateStepCategoryVOS.addAll(groupVOS);
        templateStepCategoryVOS.addAll(otherVos);
        templateStepCategoryVOS.addAll(customJobGroupVOS);
        // 将步骤分组
        templateStepCategoryVOS.forEach(ciTemplateStepCategoryVO -> {
            List<CiTemplateStepVO> ciTemplateStepVOList = categoryStepMap.get(ciTemplateStepCategoryVO.getId());
            ciTemplateStepCategoryVO.setCiTemplateStepVOList(ciTemplateStepVOList);
        });

        return templateStepCategoryVOS;
    }
}

