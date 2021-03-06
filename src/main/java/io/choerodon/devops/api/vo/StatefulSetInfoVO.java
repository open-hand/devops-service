package io.choerodon.devops.api.vo;

import java.util.List;
import java.util.Map;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/6/10 22:28
 */
public class StatefulSetInfoVO {
    private Long id;
    private String name;
    private Long desiredReplicas;
    private Long readyReplicas;
    private Long currentReplicas;
    private String age;
    private List<Integer> ports;
    private Map<String, String> labels;
    private Long instanceId;

    private String commandType;

    private String commandStatus;

    private String error;

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
}
