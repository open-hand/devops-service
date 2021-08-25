package io.choerodon.devops.api.vo;

import java.util.List;
import java.util.Map;

import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * Created by younger on 2018/4/25.
 */
public class DeploymentVO {
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
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
}
