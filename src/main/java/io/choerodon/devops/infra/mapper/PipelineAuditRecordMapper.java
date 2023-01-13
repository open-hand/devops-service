package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.api.vo.ApprovalVO;
import io.choerodon.devops.infra.dto.PipelineAuditRecordDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 人工卡点审核记录表(PipelineAuditRecord)应用服务
 *
 * @author
 * @since 2022-11-23 16:42:03
 */
public interface PipelineAuditRecordMapper extends BaseMapper<PipelineAuditRecordDTO> {

    PipelineAuditRecordDTO queryByJobRecordIdForUpdate(@Param("jobRecordId") Long jobRecordId);

    List<ApprovalVO> listApprovalInfoByProjectIdsAndUserId(@Param("userId") Long userId,
                                                           @Param("projectIds") List<Long> projectIds);
}

