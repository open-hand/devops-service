package io.choerodon.devops.infra.feign.operator;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
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


    @Autowired
    private AgileServiceClient agileServiceClient;


    public IssueDTO queryIssue(Long projectId, Long issueId, Long organizationId) {
        try {
            return FeignClientUtils.doRequest(() -> agileServiceClient.queryIssue(projectId, issueId, organizationId), IssueDTO.class, "error.issue.get");
        } catch (ServiceUnavailableException e) {
            return new IssueDTO();
        }
    }

    public List<IssueDTO> listIssueByIds(Long projectId, List<Long> ids) {
        return FeignClientUtils.doRequest(() -> agileServiceClient.queryIssues(projectId, ids), new TypeReference<List<IssueDTO>>() {
        }, "error.issues.list.by.id");
    }

    public SprintDTO getActiveSprint(Long projectId, Long organizationId) {
        try {
            return FeignClientUtils.doRequest(() -> agileServiceClient.getActiveSprint(projectId, organizationId), SprintDTO.class, "error.active.sprint.get");
        } catch (ServiceUnavailableException e) {
            return new SprintDTO();
        }
    }
}
