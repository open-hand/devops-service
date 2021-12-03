package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import io.choerodon.devops.api.vo.template.CiTemplateJobVO;
import io.choerodon.devops.app.service.CiTemplateJobGroupService;
import io.choerodon.devops.app.service.CiTemplateJobService;
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
}

