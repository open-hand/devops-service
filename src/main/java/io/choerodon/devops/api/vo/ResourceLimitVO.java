package io.choerodon.devops.api.vo;

/**
 * 〈功能简述〉
 * 〈组织资源限制VO〉
 *
 * @author wanghao
 * @Date 2020/3/26 21:32
 */
public class ResourceLimitVO {
    private Integer userMaxNumber;
    private Integer projectMaxNumber;
    private Integer appSvcMaxNumber;
    private Integer clusterMaxNumber;
    private Integer envMaxNumber;

    public Integer getUserMaxNumber() {
        return userMaxNumber;
    }

    public void setUserMaxNumber(Integer userMaxNumber) {
        this.userMaxNumber = userMaxNumber;
    }

    public Integer getProjectMaxNumber() {
        return projectMaxNumber;
    }

    public void setProjectMaxNumber(Integer projectMaxNumber) {
        this.projectMaxNumber = projectMaxNumber;
    }

    public Integer getAppSvcMaxNumber() {
        return appSvcMaxNumber;
    }

    public void setAppSvcMaxNumber(Integer appSvcMaxNumber) {
        this.appSvcMaxNumber = appSvcMaxNumber;
    }

    public Integer getClusterMaxNumber() {
        return clusterMaxNumber;
    }

    public void setClusterMaxNumber(Integer clusterMaxNumber) {
        this.clusterMaxNumber = clusterMaxNumber;
    }

    public Integer getEnvMaxNumber() {
        return envMaxNumber;
    }

    public void setEnvMaxNumber(Integer envMaxNumber) {
        this.envMaxNumber = envMaxNumber;
    }
}
