package io.choerodon.devops.infra.dto;

import javax.persistence.Table;

/**
 * Created by n!Ck
 * Date: 2018/11/21
 * Time: 11:08
 * Description:
 */

@Table(name = "devops_app_user_rel")
public class ApplicationUserPermissionDTO {
    private Long iamUserId;
    private Long appServiceId;

    public ApplicationUserPermissionDTO() {
    }

    public ApplicationUserPermissionDTO(Long iamUserId, Long appServiceId) {
        this.iamUserId = iamUserId;
        this.appServiceId = appServiceId;
    }

    public Long getIamUserId() {
        return iamUserId;
    }

    public void setIamUserId(Long iamUserId) {
        this.iamUserId = iamUserId;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void getAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }
}
