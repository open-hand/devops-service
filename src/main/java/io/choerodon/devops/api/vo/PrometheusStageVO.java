package io.choerodon.devops.api.vo;

/**
 * @author: 25499
 * @date: 2019/11/11 10:44
 * @description:
 */
public class PrometheusStageVO {
    private String parserPvc;
    private String boundPvc;
    private String parserPrometheus;
    private String installPrometheus;
    private String error;

    public PrometheusStageVO() {
    }

    public PrometheusStageVO(String parserPvc, String boundPvc, String parserPrometheus, String installPrometheus) {
        this.parserPvc = parserPvc;
        this.boundPvc = boundPvc;
        this.parserPrometheus = parserPrometheus;
        this.installPrometheus = installPrometheus;
    }

    public String getBoundPvc() {
        return boundPvc;
    }

    public void setBoundPvc(String boundPvc) {
        this.boundPvc = boundPvc;
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

    public String getParserPvc() {
        return parserPvc;
    }

    public void setParserPvc(String parserPvc) {
        this.parserPvc = parserPvc;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
