package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DevopsIssueRelService {
    void addRelation(String object, Long objectId, Long branchId, Long projectId, String appServiceCode, List<Long> issueIds);

    void addRelation(String object, Long objectId, Long branchId, Long projectId, String appServiceCode, Long issueIds);

    void deleteRelationByObjectAndObjectId(String object, Long objectId);

    void deleteRelationByObjectAndObjectIdAndIssueId(String object, Long objectId, Long issueId);

    Map<Long, List<Long>> listMappedIssueIdsByObjectTypeAndObjectId(String object, Set<Long> objectIds);

    void fixBranchInfo();
}
