package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.PipelineAuditUserDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 人工卡点审核人员表(PipelineAuditUser)应用服务
 *
 * @author
 * @since 2022-11-24 15:56:48
 */
public interface PipelineAuditUserMapper extends BaseMapper<PipelineAuditUserDTO> {

    void batchDeleteByConfigIds(@Param("configIds") List<Long> configIds);
}

