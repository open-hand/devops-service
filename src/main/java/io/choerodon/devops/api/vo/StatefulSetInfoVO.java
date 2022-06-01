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
 * @since 2021/6/10 22:28
 */
public class StatefulSetInfoVO {
    @Encrypt
    private Long id;
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("目标副本数")
    private Long desiredReplicas;
    @ApiModelProperty("就绪副本数")
    private Long readyReplicas;
    @ApiModelProperty("当前副本数")
    private Long currentReplicas;
    @ApiModelProperty("存活时间")
    private String age;
    @ApiModelProperty("端口号")
    private List<Integer> ports;
    @ApiModelProperty("标签")
    private Map<String, String> labels;
    @Encrypt
    @ApiModelProperty("所属实例id")
    private Long instanceId;
    @ApiModelProperty("command类型")
    private String commandType;
    @ApiModelProperty("command状态")
    private String commandStatus;
    @ApiModelProperty("错误信息")
    private String error;

    @ApiModelProperty("来源类型 chart/工作负载")
    private String sourceType;

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

    public Long getDesiredReplicas() {
        return desiredReplicas;
    }

    public void setDesiredReplicas(Long desiredReplicas) {
        this.desiredReplicas = desiredReplicas;
    }

    public Long getReadyReplicas() {
        return readyReplicas;
    }

    public void setReadyReplicas(Long readyReplicas) {
        this.readyReplicas = readyReplicas;
    }

    public Long getCurrentReplicas() {
        return currentReplicas;
    }

    public void setCurrentReplicas(Long currentReplicas) {
        this.currentReplicas = currentReplicas;
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

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

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

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }
}
