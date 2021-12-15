package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Set;

import io.choerodon.devops.infra.dto.CiTemplateJobGroupDTO;

/**
 * 流水线任务模板分组(CiTemplateJobGroup)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:16
 */
public interface CiTemplateJobGroupService {

    CiTemplateJobGroupDTO baseQuery(Long groupId);

    List<CiTemplateJobGroupDTO> listByIds(Set<Long> groupIds);

    List<CiTemplateJobGroupDTO> listGroups(Long projectId);
}

