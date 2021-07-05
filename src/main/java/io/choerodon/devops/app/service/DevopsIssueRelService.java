package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import io.choerodon.devops.api.vo.IssueIdAndBranchIdsVO;

public interface DevopsIssueRelService {
    void addRelation(String object, Long objectId, Long branchId, List<Long> issueIds);

    void addRelation(String object, Long objectId, Long branchId, Long issueId);

    void deleteRelationByObjectAndObjectId(String object, Long objectId);

    void deleteRelationByObjectAndObjectIdAndIssueId(String object, Long objectId, Long issueId);

    /**
     * 列举出关联对象id和issueIds的map关系图
     *
     * @param object    对象类型 commit/branch
     * @param objectIds 对象id
     * @return key: objectId value: issueIds
     */
    Map<Long, List<Long>> listMappedIssueIdsByObjectTypeAndObjectId(String object, Set<Long> objectIds);

    /**
     * 列出关联了敏捷问题的commitId或branchId
     *
     * @param object
     * @param issueId
     * @return
     */
    Set<Long> listObjectIdsByIssueIdAndObjectType(String object, Long issueId);

    /**
     * 列出关联了敏捷问题的commitId或branchId
     *
     * @param object   关联对象
     * @param issueIds 敏捷问题ids
     */
    List<IssueIdAndBranchIdsVO> listObjectIdsByIssueIdsAndObjectType(String object, Set<Long> issueIds);

    void fixBranchId();
}
