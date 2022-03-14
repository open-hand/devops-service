package io.choerodon.devops.infra.mapper;

import java.util.List;

import io.choerodon.devops.infra.dto.CiTemplateJobGroupDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 流水线任务模板分组(CiTemplateJobGroup)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:16
 */
public interface CiTemplateJobGroupMapper extends BaseMapper<CiTemplateJobGroupDTO> {

    List<CiTemplateJobGroupDTO> listNonEmptyGroups();

}

