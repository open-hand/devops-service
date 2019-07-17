package io.choerodon.devops.api.vo;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:29 2019/7/3
 * Description:
 */
public class ApplicationRemoteDeployVO {
    private Long environmentId;
    private String type;
    private Long appInstanceId;
    private Long commandId;
    private String instanceName;
    private boolean isNotChange;
    private ApplicationRemoteVO applicationRemoteVO;
    private ApplicationVersionRemoteVO applicationVersionRemoteVO;
    private ProjectConfigVO harbor;
    private ProjectConfigVO chart;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getAppInstanceId() {
        return appInstanceId;
    }

    public void setAppInstanceId(Long appInstanceId) {
        this.appInstanceId = appInstanceId;
    }

    public Long getCommandId() {
        return commandId;
    }

    public void setCommandId(Long commandId) {
        this.commandId = commandId;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public boolean isNotChange() {
        return isNotChange;
    }

    public void setNotChange(boolean notChange) {
        isNotChange = notChange;
    }

    public Long getEnvironmentId() {
        return environmentId;
    }

    public void setEnvironmentId(Long environmentId) {
        this.environmentId = environmentId;
    }

    public ApplicationRemoteVO getApplicationRemoteVO() {
        return applicationRemoteVO;
    }

    public void setApplicationRemoteVO(ApplicationRemoteVO applicationRemoteVO) {
        this.applicationRemoteVO = applicationRemoteVO;
    }

    public ApplicationVersionRemoteVO getApplicationVersionRemoteVO() {
        return applicationVersionRemoteVO;
    }

    public void setApplicationVersionRemoteVO(ApplicationVersionRemoteVO applicationVersionRemoteVO) {
        this.applicationVersionRemoteVO = applicationVersionRemoteVO;
    }

    public ProjectConfigVO getHarbor() {
        return harbor;
    }

    public void setHarbor(ProjectConfigVO harbor) {
        this.harbor = harbor;
    }

    public ProjectConfigVO getChart() {
        return chart;
    }

    public void setChart(ProjectConfigVO chart) {
        this.chart = chart;
    }
}
