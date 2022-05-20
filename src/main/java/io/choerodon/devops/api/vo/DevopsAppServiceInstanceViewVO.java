package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * @author zmf
 */
public class DevopsAppServiceInstanceViewVO {
    @Encrypt
    private Long id;
    @ApiModelProperty("实例名称")
    private String name;
    @Encrypt
    @ApiModelProperty("应用中心应用id")
    private Long appId;
    @ApiModelProperty("实例编码")
    private String code;
    @ApiModelProperty("实例pod总数")
    private Long podCount;
    @ApiModelProperty("实例运行pod名称")
    private Long podRunningCount;
    @ApiModelProperty("实例状态")
    private String status;


    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

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

    public Long getPodCount() {
        return podCount;
    }

    public void setPodCount(Long podCount) {
        this.podCount = podCount;
    }

    public Long getPodRunningCount() {
        return podRunningCount;
    }

    public void setPodRunningCount(Long podRunningCount) {
        this.podRunningCount = podRunningCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
