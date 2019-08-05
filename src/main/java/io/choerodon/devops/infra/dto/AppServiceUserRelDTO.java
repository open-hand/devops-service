package io.choerodon.devops.infra.dto;

import javax.persistence.Table;

import io.choerodon.mybatis.entity.BaseDTO;

/**
 * Created by n!Ck
 * Date: 2018/11/21
 * Time: 11:08
 * Description:
 */

@Table(name = "devops_app_service_user_rel")
public class AppServiceUserRelDTO extends BaseDTO {
    private Long iamUserId;
    private Long appServiceId;

    public AppServiceUserRelDTO() {
    }

    public AppServiceUserRelDTO(Long iamUserId, Long appServiceId) {
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

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }
}
