package io.choerodon.devops.api.vo;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  9:55 2019/7/2
 * Description:
 */
public class AppVersionAndValueDTO {
    private ApplicationVersionRemoteDTO versionRemoteDTO;
    private ProjectConfigDTO harbor;
    private ProjectConfigDTO chart;

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
