package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DevopsIssueRelService {
    void addRelation(String object, Long objectId, List<Long> issueIds);

    void addRelation(String object, Long objectId, Long issueIds);

    void deleteRelation(String object, Long objectId);

    void deleteRelation(Long issueId);

    Map<Long, List<Long>> listMappedIssueIdsByObjectTypeAndObjectId(String object, Set<Long> objectIds);
}
