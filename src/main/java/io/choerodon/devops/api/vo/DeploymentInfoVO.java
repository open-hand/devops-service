package io.choerodon.devops.api.vo;

import java.util.List;
import java.util.Map;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/6/10 16:25
 */
public class DeploymentInfoVO {
    @Encrypt
    private Long id;
    private String name;
    private Long desired;
    private Long current;
    private Long upToDate;
    private Long available;
    private String age;
    private List<Integer> ports;
    private Map<String, String> labels;
    private List<DevopsEnvPodVO> devopsEnvPodVOS;
    @Encrypt
    private Long instanceId;

    private String commandType;

    private String commandStatus;

    private String error;

    @ApiModelProperty("来源类型 chart/工作负载/部署组")
    private String sourceType;

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public String getCommandStatus() {
        return commandStatus;
    }

    public void setCommandStatus(String commandStatus) {
        this.commandStatus = commandStatus;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getDesired() {
        return desired;
    }

    public void setDesired(Long desired) {
        this.desired = desired;
    }

    public Long getCurrent() {
        return current;
    }

    public void setCurrent(Long current) {
        this.current = current;
    }

    public Long getUpToDate() {
        return upToDate;
    }

    public void setUpToDate(Long upToDate) {
        this.upToDate = upToDate;
    }

    public Long getAvailable() {
        return available;
    }

    public void setAvailable(Long available) {
        this.available = available;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public List<Integer> getPorts() {
        return ports;
    }

    public void setPorts(List<Integer> ports) {
        this.ports = ports;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    public List<DevopsEnvPodVO> getDevopsEnvPodVOS() {
        return devopsEnvPodVOS;
    }

    public void setDevopsEnvPodVOS(List<DevopsEnvPodVO> devopsEnvPodVOS) {
        this.devopsEnvPodVOS = devopsEnvPodVOS;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }
}
