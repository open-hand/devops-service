package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Set;

import com.google.common.base.Joiner;
import org.hzero.core.base.BaseConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.CiTemplateJobGroupService;
import io.choerodon.devops.infra.dto.CiTemplateJobGroupDTO;
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
}

