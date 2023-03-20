package io.choerodon.devops.infra.dto;

import com.cdancy.jenkins.rest.JenkinsClient;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2023/3/3 10:03
 */
public class JenkinsClientWrapper {
    private Long versionId;
    private JenkinsClient jenkinsClient;

    public JenkinsClientWrapper() {
    }

    public JenkinsClientWrapper(Long versionId, JenkinsClient jenkinsClient) {
        this.versionId = versionId;
        this.jenkinsClient = jenkinsClient;
    }

    public Long getVersionId() {
        return versionId;
    }

    public void setVersionId(Long versionId) {
        this.versionId = versionId;
    }

    public JenkinsClient getJenkinsClient() {
        return jenkinsClient;
    }

    public void setJenkinsClient(JenkinsClient jenkinsClient) {
        this.jenkinsClient = jenkinsClient;
    }
}
