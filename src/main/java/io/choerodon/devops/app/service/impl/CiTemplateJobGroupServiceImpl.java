package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Joiner;
import org.apache.commons.lang3.StringUtils;
import org.hzero.core.base.BaseConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.CiTemplateJobGroupService;
import io.choerodon.devops.infra.dto.CiTemplateJobGroupDTO;
import io.choerodon.devops.infra.enums.CiTemplateJobGroupTypeEnum;
import io.choerodon.devops.infra.mapper.CiTemplateJobGroupMapper;

/**
 * 流水线任务模板分组(CiTemplateJobGroup)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:16
 */
@Service
public class CiTemplateJobGroupServiceImpl implements CiTemplateJobGroupService {
    @Autowired
    private CiTemplateJobGroupMapper ciTemplateJobGroupMapper;


    @Override
    public CiTemplateJobGroupDTO baseQuery(Long groupId) {
        return ciTemplateJobGroupMapper.selectByPrimaryKey(groupId);
    }

    @Override
    public List<CiTemplateJobGroupDTO> listByIds(Set<Long> groupIds) {
        return ciTemplateJobGroupMapper.selectByIds(Joiner.on(BaseConstants.Symbol.COMMA).join(groupIds));
    }

    @Override
    public List<CiTemplateJobGroupDTO> listAllGroups() {
        return ciTemplateJobGroupMapper.selectAll();
    }

    @Override
    public List<CiTemplateJobGroupDTO> listNonEmptyGroups() {
        List<CiTemplateJobGroupDTO> ciTemplateJobGroupDTOS = ciTemplateJobGroupMapper.listNonEmptyGroups();
        return sortedTemplateJob(ciTemplateJobGroupDTOS);
    }

    private List<CiTemplateJobGroupDTO> sortedTemplateJob(List<CiTemplateJobGroupDTO> ciTemplateJobGroupDTOS) {
        List<CiTemplateJobGroupDTO> templateJobGroupDTOS = new ArrayList<>();
        List<CiTemplateJobGroupDTO> jobGroupDTOS = ciTemplateJobGroupDTOS.stream().sorted(Comparator.comparing(CiTemplateJobGroupDTO::getId).reversed()).collect(Collectors.toList());
        //构建放在第一位 自定义的放在最后
        List<CiTemplateJobGroupDTO> customJobGroupVOS = jobGroupDTOS.stream().filter(ciTemplateJobGroupVO -> !ciTemplateJobGroupVO.getBuiltIn()).collect(Collectors.toList());
        List<CiTemplateJobGroupDTO> otherVos = jobGroupDTOS.stream()
                .filter(CiTemplateJobGroupDTO::getBuiltIn)
                .filter(ciTemplateJobGroupVO -> StringUtils.equalsIgnoreCase(ciTemplateJobGroupVO.getType(), CiTemplateJobGroupTypeEnum.OTHER.value()))
                .collect(Collectors.toList());


        List<CiTemplateJobGroupDTO> firstVos = jobGroupDTOS
                .stream()
                .filter(ciTemplateJobGroupVO -> StringUtils.equalsIgnoreCase(ciTemplateJobGroupVO.getType(), CiTemplateJobGroupTypeEnum.BUILD.value()))
                .collect(Collectors.toList());

        List<CiTemplateJobGroupDTO> groupVOS = jobGroupDTOS.stream().filter(CiTemplateJobGroupDTO::getBuiltIn)
                .filter(ciTemplateJobGroupVO -> !StringUtils.equalsIgnoreCase(ciTemplateJobGroupVO.getType(), CiTemplateJobGroupTypeEnum.OTHER.value()))
                .filter(ciTemplateJobGroupVO -> !StringUtils.equalsIgnoreCase(ciTemplateJobGroupVO.getType(), CiTemplateJobGroupTypeEnum.BUILD.value()))
                .sorted(Comparator.comparing(CiTemplateJobGroupDTO::getId))
                .collect(Collectors.toList());

        templateJobGroupDTOS.addAll(firstVos);
        templateJobGroupDTOS.addAll(groupVOS);
        templateJobGroupDTOS.addAll(otherVos);
        templateJobGroupDTOS.addAll(customJobGroupVOS);
        return templateJobGroupDTOS;
    }
}

