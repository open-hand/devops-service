//package io.choerodon.devops.infra.mapper;
//
//import java.util.List;
//
//import org.apache.ibatis.annotations.Param;
//
//import io.choerodon.devops.infra.dto.DevopsCdAuditRecordDTO;
//import io.choerodon.mybatis.common.BaseMapper;
//
///**
// * @author scp
// * @date 2020/7/3
// * @description
// */
//public interface DevopsCdAuditRecordMapper extends BaseMapper<DevopsCdAuditRecordDTO> {
//    List<DevopsCdAuditRecordDTO> listByProjectIdsAndUserId(@Param("userId") Long userId, @Param("projectIds") List<Long> projectIds);
//
//    Integer updateProjectIdByJobRecordId(@Param("projectId") Long projectId, @Param("jobRecordId") Long jobRecordId);
//}