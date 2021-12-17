package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import io.choerodon.devops.api.vo.DevopsCiJobVO;
import io.choerodon.devops.api.vo.DevopsCiStepVO;
import io.choerodon.devops.api.vo.template.CiTemplateJobVO;
import io.choerodon.devops.api.vo.template.CiTemplateStepVO;
import io.choerodon.devops.app.service.AbstractDevopsCiStepHandler;
import io.choerodon.devops.app.service.CiTemplateJobGroupService;
import io.choerodon.devops.app.service.CiTemplateJobService;
import io.choerodon.devops.app.service.CiTemplateStepService;
import io.choerodon.devops.infra.dto.CiTemplateJobDTO;
import io.choerodon.devops.infra.dto.CiTemplateJobGroupDTO;
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


    @Override
    public List<CiTemplateJobVO> listByStageIds(Set<Long> stageIds) {
        return ciTemplateJobmapper.listByStageIds(stageIds);
    }

    @Override
    public List<CiTemplateJobVO> listByStageIdWithGroupInfo(Long stageId) {
        Assert.notNull(stageId, "error.stage.id.is.null");

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
        CiTemplateJobDTO ciTemplateJobDTO = new CiTemplateJobDTO();
        ciTemplateJobDTO.setGroupId(groupId);
        List<CiTemplateJobDTO> templateJobDTOS = ciTemplateJobmapper.select(ciTemplateJobDTO);

        List<DevopsCiJobVO> devopsCiJobVOList = new ArrayList<>();

        if (CollectionUtils.isEmpty(templateJobDTOS)) {
            return new ArrayList<>();
        }
        // 填充任务中的步骤信息
        Set<Long> jobIds = templateJobDTOS.stream().map(CiTemplateJobDTO::getId).collect(Collectors.toSet());
        List<CiTemplateStepVO> ciTemplateStepVOS = ciTemplateStepService.listByJobIds(jobIds);
        Map<Long, List<CiTemplateStepVO>> jobStepsMap = ciTemplateStepVOS.stream().collect(Collectors.groupingBy(CiTemplateStepVO::getCiTemplateJobId));
        templateJobDTOS.forEach(templateJobDTO -> {
            List<CiTemplateStepVO> ciTemplateStepVOList = jobStepsMap.get(templateJobDTO.getId());

            List<DevopsCiStepVO> devopsCiStepVOList = new ArrayList<>();
            ciTemplateStepVOList.forEach(ciTemplateStepVO -> {
                DevopsCiStepVO devopsCiStepVO = ConvertUtils.convertObject(ciTemplateStepVO, DevopsCiStepVO.class);
                // 添加步骤关联的配置信息
                AbstractDevopsCiStepHandler ciTemplateStepHandler = devopsCiStepOperator.getHandlerOrThrowE(devopsCiStepVO.getType());
                ciTemplateStepHandler.fillStepConfigInfo(devopsCiStepVO);
                devopsCiStepVOList.add(devopsCiStepVO);
            });

            DevopsCiJobVO devopsCiJobVO = ConvertUtils.convertObject(templateJobDTO, DevopsCiJobVO.class);
            devopsCiJobVO.setDevopsCiStepVOList(devopsCiStepVOList);
            devopsCiJobVOList.add(devopsCiJobVO);
        });
        return devopsCiJobVOList;
    }
}

