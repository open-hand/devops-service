package io.choerodon.devops.api.vo;

import java.util.List;

/**
 * @author zmf
 */
public class DaemonSetDTO {
    private String name;
    private Long desiredScheduled;
    private Long currentScheduled;
    private Long numberAvailable;
    private String age;
    private List<DevopsEnvPodDTO> devopsEnvPodDTOS;

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

    public List<DevopsEnvPodDTO> getDevopsEnvPodDTOS() {
        return devopsEnvPodDTOS;
    }

    public void setDevopsEnvPodDTOS(List<DevopsEnvPodDTO> devopsEnvPodDTOS) {
        this.devopsEnvPodDTOS = devopsEnvPodDTOS;
    }
}
