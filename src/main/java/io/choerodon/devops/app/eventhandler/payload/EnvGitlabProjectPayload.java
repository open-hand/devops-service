package io.choerodon.devops.app.eventhandler.payload;

/**
 * @author zmf
 */
public class EnvGitlabProjectPayload extends GitlabProjectPayload {
    private Boolean isSkipCheckPermission;

    public Boolean getSkipCheckPermission() {
        return isSkipCheckPermission;
    }

    public void setSkipCheckPermission(Boolean skipCheckPermission) {
        isSkipCheckPermission = skipCheckPermission;
    }
}
