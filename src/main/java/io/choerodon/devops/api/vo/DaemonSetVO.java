package io.choerodon.devops.api.vo;

import java.util.List;

/**
 * @author zmf
 */
public class DaemonSetVO {
    private String name;
    private Long desiredScheduled;
    private Long currentScheduled;
    private Long numberAvailable;
    private String age;
    private List<DevopsEnvPodVO> devopsEnvPodDTOS;

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

    public List<DevopsEnvironmentPodVO> getDevopsEnvPodDTOS() {
        return devopsEnvPodDTOS;
    }

    public void setDevopsEnvPodDTOS(List<DevopsEnvironmentPodVO> devopsEnvPodDTOS) {
        this.devopsEnvPodDTOS = devopsEnvPodDTOS;
    }
}
