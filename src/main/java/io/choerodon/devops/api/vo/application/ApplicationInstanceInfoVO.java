package io.choerodon.devops.api.vo.application;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/6/29 10:33
 */
public class ApplicationInstanceInfoVO {
    @Encrypt
    private Long id;
    @ApiModelProperty("实例编码")
    private String code;
    @ApiModelProperty("实例当前部署的版本")
    private String version;
    @ApiModelProperty("实例的pod总数")
    private Integer podCount;
    @ApiModelProperty("实例运行中的pod总数")
    private Integer podRunningCount;
    @ApiModelProperty("实例的状态")
    private String status;

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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Integer getPodCount() {
        return podCount;
    }

    public void setPodCount(Integer podCount) {
        this.podCount = podCount;
    }

    public Integer getPodRunningCount() {
        return podRunningCount;
    }

    public void setPodRunningCount(Integer podRunningCount) {
        this.podRunningCount = podRunningCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
