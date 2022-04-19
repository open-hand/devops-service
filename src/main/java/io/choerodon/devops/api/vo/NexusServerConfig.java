package io.choerodon.devops.api.vo;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;


/**
 * 制品库_nexus服务信息配置表
 *
 * @author weisen.yang@hand-china.com 2020-03-27 11:42:59
 */

public class NexusServerConfig{





    @Encrypt
    @ApiModelProperty("表ID，主键，供其他表做外键")
    @Id
    @GeneratedValue
    private Long configId;
    @ApiModelProperty(value = "服务名称",required = true)
    @NotBlank
    private String serverName;
    @ApiModelProperty(value = "访问地址",required = true)
    @NotBlank
    private String serverUrl;
    @ApiModelProperty(value = "管理用户",required = true)
    @NotBlank
    private String userName;
    @ApiModelProperty(value = "管理用户密码",required = true)
    @NotBlank
    private String password;
    @ApiModelProperty(value = "匿名访问，用户")
    private String anonymous;
    @ApiModelProperty(value = "匿名访问，用户对应角色")
    private String anonymousRole;
    @ApiModelProperty(value = "是否是Choerodon默认服务")
    private Integer defaultFlag;
    @ApiModelProperty(value = "租户Id")
    private Long tenantId;
    @ApiModelProperty(value = "是否启用匿名访问控制")
    @NotNull
    private Integer enableAnonymousFlag;


        public Long getConfigId() {
            return configId;
        }

        public void setConfigId(Long configId) {
            this.configId = configId;
        }

        public String getServerName() {
            return serverName;
        }

        public void setServerName(String serverName) {
            this.serverName = serverName;
        }

        public String getServerUrl() {
            return serverUrl;
        }

        public void setServerUrl(String serverUrl) {
            this.serverUrl = serverUrl;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getAnonymous() {
            return anonymous;
        }

        public void setAnonymous(String anonymous) {
            this.anonymous = anonymous;
        }

        public String getAnonymousRole() {
            return anonymousRole;
        }

        public void setAnonymousRole(String anonymousRole) {
            this.anonymousRole = anonymousRole;
        }

        public Integer getDefaultFlag() {
            return defaultFlag;
        }

        public void setDefaultFlag(Integer defaultFlag) {
            this.defaultFlag = defaultFlag;
        }

        public Long getTenantId() {
            return tenantId;
        }

        public void setTenantId(Long tenantId) {
            this.tenantId = tenantId;
        }

        public Integer getEnableAnonymousFlag() {
            return enableAnonymousFlag;
        }

        public void setEnableAnonymousFlag(Integer enableAnonymousFlag) {
            this.enableAnonymousFlag = enableAnonymousFlag;
        }
}
