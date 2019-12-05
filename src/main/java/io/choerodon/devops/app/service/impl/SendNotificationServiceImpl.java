package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.SendNotificationService;
import io.choerodon.devops.infra.feign.NotifyClient;

/**
 * @author zmf
 * @since 12/5/19
 */
@Service
public class SendNotificationServiceImpl implements SendNotificationService {
    @Autowired
    private NotifyClient notifyClient;

    @Override
    public void sendWhenAppServiceFailure(Long organizationId, Long projectId, String projectName, String projectCategory, String appServiceName) {
        // TODO by zmf
    }

    @Override
    public void sendWhenAppServiceEnabled(Long organizationId, Long projectId, String projectName, String projectCategory, String appServiceName) {
        // TODO by zmf
    }

    @Override
    public void sendWhenAppServiceDisabled(Long organizationId, Long projectId, String projectName, String projectCategory, String appServiceName) {
        // TODO by zmf
    }

    @Override
    public void sendWhenCDFailure(String gitlabUrl, String organizationCode, String projectCode, String projectName, String appServiceCode, String appServiceName) {
        // TODO by zmf
    }

    @Override
    public void sendWhenMergeRequestAuditEvent(String gitlabUrl, String organizationCode, String projectCode, String projectName, String appServiceCode, String appServiceName, String realName, Long mergeRequestId) {
        // TODO by zmf
    }

    @Override
    public void sendWhenMergeRequestClosed(String gitlabUrl, String organizationCode, String projectCode, String projectName, String appServiceCode, String appServiceName, String realName, Long mergeRequestId) {
        // TODO by zmf
    }

    @Override
    public void sendWhenMergeRequestPassed(String gitlabUrl, String organizationCode, String projectCode, String projectName, String appServiceCode, String appServiceName, String realName, Long mergeRequestId) {
        // TODO by zmf
    }

    @Override
    public void sendWhenInstanceCreationFailure(String projectName, String envName, String resourceName) {
        // TODO by zmf
    }

    @Override
    public void sendWhenServiceCreationFailure(String projectName, String envName, String resourceName) {
        // TODO by zmf
    }

    @Override
    public void sendWhenIngressCreationFailure(String projectName, String envName, String resourceName) {
        // TODO by zmf
    }

    @Override
    public void sendWhenCertificationCreationFailure(String projectName, String envName, String resourceName) {
        // TODO by zmf
    }
}
