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
 * @since 2021/6/10 21:37
 */
public class DaemonSetInfoVO {
    @Encrypt
    private Long id;
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("目标pod数")
    private Long desiredScheduled;
    @ApiModelProperty("当前pod数")
    private Long currentScheduled;
    @ApiModelProperty("可用pod数")
    private Long numberAvailable;
    @ApiModelProperty("就绪pod数")
    private Long numberReady;
    @ApiModelProperty("创建时间")
    private String age;
    @ApiModelProperty("端口号")
    private List<Integer> ports;
    @ApiModelProperty("标签")
    private Map<String, String> labels;
    @Encrypt
    @ApiModelProperty("所属实例id")
    private Long instanceId;
    @ApiModelProperty("commandType")
    private String commandType;
    @ApiModelProperty("commandStatus")
    private String commandStatus;
    @ApiModelProperty("错误信息")
    private String error;

    @ApiModelProperty("来源类型 chart/工作负载")
    private String sourceType;

    public Long getNumberReady() {
        return numberReady;
    }

    public void setNumberReady(Long numberReady) {
        this.numberReady = numberReady;
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

    public Long getDesiredScheduled() {
        return desiredScheduled;
    }

    public void setDesiredScheduled(Long desiredScheduled) {
        this.desiredScheduled = desiredScheduled;
    }

    public Long getCurrentScheduled() {
        return currentScheduled;
    }

    public void setCurrentScheduled(Long currentScheduled) {
        this.currentScheduled = currentScheduled;
    }

    public Long getNumberAvailable() {
        return numberAvailable;
    }

    public void setNumberAvailable(Long numberAvailable) {
        this.numberAvailable = numberAvailable;
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
