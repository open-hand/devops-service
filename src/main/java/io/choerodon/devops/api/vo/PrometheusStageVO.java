package io.choerodon.devops.api.vo;

/**
 * @author: 25499
 * @date: 2019/11/11 10:44
 * @description:
 */
public class PrometheusStageVO {
    private String parserPrometheus;
    private String installPrometheus;
    private String error;

    public PrometheusStageVO() {
    }

    public PrometheusStageVO( String parserPrometheus, String installPrometheus) {
        this.parserPrometheus = parserPrometheus;
        this.installPrometheus = installPrometheus;
    }

    public String getParserPrometheus() {
        return parserPrometheus;
    }

    public void setParserPrometheus(String parserPrometheus) {
        this.parserPrometheus = parserPrometheus;
    }

    public String getInstallPrometheus() {
        return installPrometheus;
    }

    public void setInstallPrometheus(String installPrometheus) {
        this.installPrometheus = installPrometheus;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
