package io.choerodon.devops.infra.feign.operator;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.ServiceUnavailableException;
import io.choerodon.core.utils.FeignClientUtils;
import io.choerodon.devops.infra.dto.agile.IssueDTO;
import io.choerodon.devops.infra.dto.agile.SprintDTO;
import io.choerodon.devops.infra.feign.AgileServiceClient;

/**
 * Created by Sheep on 2019/7/11.
 */

@Component
public class AgileServiceClientOperator {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgileServiceClientOperator.class);

    @Autowired
    private AgileServiceClient agileServiceClient;


    public IssueDTO queryIssue(Long projectId, Long issueId, Long organizationId) {
        try {
            return FeignClientUtils.doRequest(() -> agileServiceClient.queryIssue(projectId, issueId, organizationId), IssueDTO.class, "error.issue.get");
        } catch (ServiceUnavailableException e) {
            return null;
        }
    }

    public List<IssueDTO> listIssueByIds(Long projectId, List<Long> ids) {
        return FeignClientUtils.doRequest(() -> agileServiceClient.queryIssues(projectId, ids), new TypeReference<List<IssueDTO>>() {
        }, "error.issues.list.by.id");
    }

    public List<IssueDTO> listIssueByIdsWithProjectId(List<Long> ids) {
        return FeignClientUtils.doRequest(() -> agileServiceClient.queryIssuesByIds(ids), new TypeReference<List<IssueDTO>>() {
        }, "error.issues.list.by.ids");
    }


    public SprintDTO getActiveSprint(Long projectId, Long organizationId) {
        try {
            return FeignClientUtils.doRequest(() -> agileServiceClient.getActiveSprint(projectId, organizationId), SprintDTO.class, "error.active.sprint.get");
        } catch (ServiceUnavailableException e) {
            LOGGER.warn(e.getMessage());
            return null;
        }
    }
}
