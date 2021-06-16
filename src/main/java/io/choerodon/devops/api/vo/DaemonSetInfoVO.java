package io.choerodon.devops.api.vo;

import java.util.List;
import java.util.Map;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/6/10 21:37
 */
public class DaemonSetInfoVO {

    private Long id;
    private String name;
    private Long desiredScheduled;
    private Long currentScheduled;
    private Long numberAvailable;
    private Long numberReady;
    private String age;
    private List<Integer> ports;
    private Map<String, String> labels;
    private Long instanceId;

    private String commandType;

    private String commandStatus;

    private String error;

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
}
