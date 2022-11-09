package io.choerodon.devops.infra.mapper;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.CiAuditRecordDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * ci 人工卡点审核记录表(CiAuditRecord)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2022-11-03 10:16:51
 */
public interface CiAuditRecordMapper extends BaseMapper<CiAuditRecordDTO> {

    CiAuditRecordDTO queryByUniqueOptionForUpdate(@Param("appServiceId") Long appServiceId,
                                                  @Param("gitlabPipelineId") Long gitlabPipelineId,
                                                  @Param("name") String name);
}

