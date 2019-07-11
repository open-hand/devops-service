package io.choerodon.devops.api.vo.iam.entity;

/**
 * Created by younger on 2018/3/29.
 */
public class ProjectHookE {
    private boolean enableSslVerification;
    private Integer projectId;
    private String token;
    private String url;


    public boolean getEnableSslVerification() {
        return enableSslVerification;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public String getToken() {
        return token;
    }

    public String getUrl() {
        return url;
    }

    public void initUrl(String apiGateway, Long projectId, Long applicationId) {
        this.url = apiGateway + "/devops/v1/public/project/" + projectId + "/service/" + applicationId + "/webhook";
    }
}
