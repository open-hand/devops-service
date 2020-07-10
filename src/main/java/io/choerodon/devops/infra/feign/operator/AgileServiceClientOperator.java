package io.choerodon.devops.infra.feign.operator;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.infra.dto.agile.IssueDTO;
import io.choerodon.devops.infra.dto.agile.ProjectInfoDTO;
import io.choerodon.devops.infra.dto.agile.SprintDTO;
import io.choerodon.devops.infra.feign.AgileServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by Sheep on 2019/7/11.
 */

@Component
public class AgileServiceClientOperator {


    @Autowired
    private AgileServiceClient agileServiceClient;


    public IssueDTO queryIssue(Long projectId, Long issueId, Long organizationId) {
        try {
            return agileServiceClient.queryIssue(projectId, issueId, organizationId).getBody();
        } catch (Exception e) {
            throw new CommonException("error.issue.get", e);
        }
    }

    public ProjectInfoDTO queryProjectInfo(Long projectId) {
        return agileServiceClient.queryProjectInfo(projectId).getBody();
    }

    public List<IssueDTO> listIssueByIds(Long projectId, List<Long> ids) {
        try {
            return agileServiceClient.queryIssues(projectId, ids).getBody();
        } catch (Exception e) {
            return null;
        }
    }

    public SprintDTO getActiveSprint(Long projectId, Long organizationId) {
        try {
            return agileServiceClient.getActiveSprint(projectId, organizationId).getBody();
        } catch (Exception e) {
            throw new CommonException(e);
        }
    }
}
