package io.choerodon.devops.api.vo;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  9:55 2019/7/2
 * Description:
 */
public class AppServiceVersionAndValueVO {
    private AppServiceVersionRemoteVO versionRemoteDTO;
    private ProjectConfigVO harbor;
    private ProjectConfigVO chart;

    public AppServiceVersionRemoteVO getVersionRemoteDTO() {
        return versionRemoteDTO;
    }

    public void setVersionRemoteDTO(AppServiceVersionRemoteVO versionRemoteDTO) {
        this.versionRemoteDTO = versionRemoteDTO;
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
