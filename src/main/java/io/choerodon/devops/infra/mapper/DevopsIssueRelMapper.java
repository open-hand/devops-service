package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsIssueRelDTO;
import io.choerodon.mybatis.common.BaseMapper;

public interface DevopsIssueRelMapper extends BaseMapper<DevopsIssueRelDTO> {
    void batchDeleteByBranchIdAndIssueIds(@Param("branchId") Long branchId, @Param("issueIds") List<Long> issueIds);

    Set<Long> listIssueIdsByObjectTypeAndObjectId(@Param("objectIds") Set<Long> objectIds, @Param("object") String object);

    List<DevopsIssueRelDTO> listIssueIdsByObjectTypeAndObjectIds(Set<Long> objectIds, String object);

    Set<DevopsIssueRelDTO> listRelationByIssueIdAndProjectIdAndObjectType(@Param("projectId") Long projectId, @Param("object") String object, @Param("issueId") Long issueId);

    List<DevopsIssueRelDTO> listRelationByIssueIdsAndObjectType(@Param("object") String object, @Param("issueIds") Set<Long> issueIds);

    Integer count();

    void batchUpdate(@Param("dtosToUpdate") List<DevopsIssueRelDTO> dtosToUpdate);

    /**
     * 查出还存在关联关系的分支id
     *
     * @param commitRelatedBranchIds 待查询的分支id
     * @return 仍存在关系的分支id
     */
    List<Long> listExistRelationBranchIds(@Param("commitRelatedBranchIds") Set<Long> commitRelatedBranchIds);

    void deleteCommitRelationByBranchIdAndIssueId(@Param("branchId") Long branchId, @Param("issueId") Long issueId);

    void batchDeleteCommitRelationByBranchIdAndIssueIds(@Param("branchId") Long branchId, @Param("issueIdsToDelete") List<Long> issueIdsToDelete);

    List<Long> listBranchIdsByCommitIds(@Param("commitIds") Set<Long> commitIds);

    List<Long> listCommitRelationByBranchId(@Param("branchId") Long branchId);

    Set<DevopsIssueRelDTO> listRelationByIssueIdAndObjectType(@Param("object") String object, @Param("issueId") Long issueId);

    Boolean checkIssueBranchRelExist(@Param("value") String value,
                                     @Param("projectId") Long projectId,
                                     @Param("issueId") Long issueId);
}
