package io.choerodon.devops.app.eventhandler.payload;

/**
 * @author zmf
 */
public class EnvGitlabProjectPayload extends GitlabProjectPayload {
    private Boolean skipCheckPermission;

    public Boolean getSkipCheckPermission() {
        return skipCheckPermission;
    }

    public void setSkipCheckPermission(Boolean skipCheckPermission) {
        this.skipCheckPermission = skipCheckPermission;
    }
}
