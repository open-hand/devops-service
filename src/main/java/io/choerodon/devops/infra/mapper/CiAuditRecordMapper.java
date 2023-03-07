package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.api.vo.ApprovalVO;
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

    List<ApprovalVO> listApprovalInfoByProjectIdsAndUserId(@Param("userId") Long userId,
                                                           @Param("projectIds") List<Long> projectIds);
}

