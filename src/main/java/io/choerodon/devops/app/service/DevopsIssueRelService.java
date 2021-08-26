package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import io.choerodon.devops.api.vo.IssueIdAndBranchIdsVO;
import io.choerodon.devops.infra.dto.DevopsIssueRelDTO;

public interface DevopsIssueRelService {
    /**
     * 添加关联关系
     *
     * @param object         对象
     * @param objectId       对象id
     * @param branchId       和该对象有关的分支id
     * @param projectId      所属项目
     * @param appServiceCode 和该对象有关的应用服务code
     * @param issueIds       issueIds
     */
    void addRelation(String object, Long objectId, Long branchId, Long projectId, String appServiceCode, List<Long> issueIds);

    /**
     * 添加关联关系
     *
     * @param object         对象
     * @param objectId       对象id
     * @param branchId       和该对象有关的分支id
     * @param projectId      所属项目
     * @param appServiceCode 和该对象有关的应用服务code
     * @param issueId        issueId
     */
    void addRelation(String object, Long objectId, Long branchId, Long projectId, String appServiceCode, Long issueId);

    /**
     * 根据对象以及对象id删除关联关系
     *
     * @param object   对象 branch/commit
     * @param objectId 对象id
     */
    void deleteRelationByObjectAndObjectId(String object, Long objectId);

    /**
     * 根据对象、对象id以及issueId删除关联关系
     *
     * @param object   对象
     * @param objectId 对象id
     * @param issueId  issueId
     */
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
    Set<DevopsIssueRelDTO> listRelationByIssueIdAndObjectType(String object, Long issueId);

    /**
     * 列出关联了敏捷问题的commitId或branchId
     *
     * @param object   关联对象
     * @param issueIds 敏捷问题ids
     */
    List<IssueIdAndBranchIdsVO> listObjectIdsByIssueIdsAndObjectType(String object, Set<Long> issueIds);

    List<Long> listRelatedBranchIds(Set<Long> commitRelatedBranchIds);

    void fixBranchInfo();

    /**
     * 根据branchId删除commit的关联
     *
     * @param branchId 分支id
     * @param issueId  问题id
     */
    void deleteCommitRelationByBranchId(Long branchId, Long issueId);
}
