package io.choerodon.devops.api.vo;

/**
 * @author: 25499
 * @date: 2019/11/11 10:44
 * @description:
 */
public class PrometheusStageVO {
    private String createPvc;
    private String createConfig;
    private String installPrometheus;

    public PrometheusStageVO() {
    }

    public PrometheusStageVO(String createPvc, String createConfig, String installPrometheus) {
        this.createPvc = createPvc;
        this.createConfig = createConfig;
        this.installPrometheus = installPrometheus;
    }

    public String getCreatePvc() {
        return createPvc;
    }

    public void setCreatePvc(String createPvc) {
        this.createPvc = createPvc;
    }

    public String getCreateConfig() {
        return createConfig;
    }

    public void setCreateConfig(String createConfig) {
        this.createConfig = createConfig;
    }

    public String getInstallPrometheus() {
        return installPrometheus;
    }

    public void setInstallPrometheus(String installPrometheus) {
        this.installPrometheus = installPrometheus;
    }
}
