package io.choerodon.devops.infra.feign.operator;

import io.choerodon.devops.infra.dto.agile.IssueDTO;
import io.choerodon.devops.infra.dto.agile.ProjectInfoDTO;
import io.choerodon.devops.infra.feign.AgileServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
            return null;
        }
    }

    public ProjectInfoDTO queryProjectInfo(Long projectId) {
        return agileServiceClient.queryProjectInfo(projectId).getBody();
    }


}
