package io.choerodon.devops.api.dto;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:29 2019/7/3
 * Description:
 */
public class ApplicationRemoteDeployDTO {
    private Long environmentId;
    private String type;
    private Long appInstanceId;
    private Long commandId;
    private String instanceName;
    private boolean isNotChange;
    private ApplicationRemoteDTO appRemoteDTO;
    private ApplicationVersionRemoteDTO versionRemoteDTO;
    private ProjectConfigDTO harbor;
    private ProjectConfigDTO chart;

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

    public ApplicationRemoteDTO getAppRemoteDTO() {
        return appRemoteDTO;
    }

    public void setAppRemoteDTO(ApplicationRemoteDTO appRemoteDTO) {
        this.appRemoteDTO = appRemoteDTO;
    }

    public ApplicationVersionRemoteDTO getVersionRemoteDTO() {
        return versionRemoteDTO;
    }

    public void setVersionRemoteDTO(ApplicationVersionRemoteDTO versionRemoteDTO) {
        this.versionRemoteDTO = versionRemoteDTO;
    }

    public ProjectConfigDTO getHarbor() {
        return harbor;
    }

    public void setHarbor(ProjectConfigDTO harbor) {
        this.harbor = harbor;
    }

    public ProjectConfigDTO getChart() {
        return chart;
    }

    public void setChart(ProjectConfigDTO chart) {
        this.chart = chart;
    }
}
