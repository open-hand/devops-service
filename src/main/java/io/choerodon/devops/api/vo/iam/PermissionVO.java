package io.choerodon.devops.api.vo.iam;

import io.swagger.annotations.ApiModelProperty;

/**
 * Created by n!Ck
 * Date: 2018/10/26
 * Time: 13:23
 * Description:
 */
public class PermissionVO {
    @ApiModelProperty(value = "主键ID")
    private Long id;
    @ApiModelProperty(value = "权限编码")
    private String code;
    @ApiModelProperty(value = "权限路径")
    private String path;
    @ApiModelProperty(value = "接口方法")
    private String method;
    @ApiModelProperty(value = "权限层级")
    private String level;
    @ApiModelProperty(value = "权限描述")
    private String description;
    @ApiModelProperty(value = "方法action")
    private String action;
    @ApiModelProperty(value = "权限资源")
    private String resource;
    @ApiModelProperty(value = "是否公开权限")
    private Boolean publicAccess;
    @ApiModelProperty(value = "是否登录可访问")
    private Boolean loginAccess;
    @ApiModelProperty(value = "服务名")
    private String serviceName;
    @ApiModelProperty(value = "乐观锁版本号")
    private Long objectVersionNumber;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public Boolean getPublicAccess() {
        return publicAccess;
    }

    public void setPublicAccess(Boolean publicAccess) {
        this.publicAccess = publicAccess;
    }

    public Boolean getLoginAccess() {
        return loginAccess;
    }

    public void setLoginAccess(Boolean loginAccess) {
        this.loginAccess = loginAccess;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }
}
