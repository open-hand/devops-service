package io.choerodon.devops.api.vo;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:29 2019/7/3
 * Description:
 */
public class AppServiceRemoteDeployVO {

    private Long environmentId;
    private String type;
    private Long instanceId;
    private Long commandId;
    private String instanceName;
    private boolean isNotChange;
    private AppServiceRemoteVO appServiceRemoteVO;
    private AppServiceVersionRemoteVO appServiceVersionRemoteVO;
    private ConfigVO harbor;
    private ConfigVO chart;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
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

    public AppServiceRemoteVO getAppServiceRemoteVO() {
        return appServiceRemoteVO;
    }

    public void setAppServiceRemoteVO(AppServiceRemoteVO appServiceRemoteVO) {
        this.appServiceRemoteVO = appServiceRemoteVO;
    }

    public AppServiceVersionRemoteVO getAppServiceVersionRemoteVO() {
        return appServiceVersionRemoteVO;
    }

    public void setAppServiceVersionRemoteVO(AppServiceVersionRemoteVO appServiceVersionRemoteVO) {
        this.appServiceVersionRemoteVO = appServiceVersionRemoteVO;
    }

    public ConfigVO getHarbor() {
        return harbor;
    }

    public void setHarbor(ConfigVO harbor) {
        this.harbor = harbor;
    }

    public ConfigVO getChart() {
        return chart;
    }

    public void setChart(ConfigVO chart) {
        this.chart = chart;
    }
}
