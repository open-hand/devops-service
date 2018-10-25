package io.choerodon.devops.infra.dataobject;

/**
 * Created by n!Ck
 * Date: 2018/10/25
 * Time: 16:52
 * Description:
 */
public class EnvUserPermissionDO {
    private String userName;
    private Long envId;
    private Boolean hasPermission;

    public EnvUserPermissionDO() {
    }

    public EnvUserPermissionDO(String userName, Long envId, Boolean hasPermission) {
        this.userName = userName;
        this.envId = envId;
        this.hasPermission = hasPermission;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public Boolean getHasPermission() {
        return hasPermission;
    }

    public void setHasPermission(Boolean hasPermission) {
        this.hasPermission = hasPermission;
    }
}
