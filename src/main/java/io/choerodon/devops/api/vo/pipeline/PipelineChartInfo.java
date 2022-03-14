package io.choerodon.devops.api.vo.pipeline;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/12/15 17:18
 */
public class PipelineChartInfo {
    private String chartVersion;

    public PipelineChartInfo(String chartVersion) {
        this.chartVersion = chartVersion;
    }

    public String getChartVersion() {
        return chartVersion;
    }

    public void setChartVersion(String chartVersion) {
        this.chartVersion = chartVersion;
    }
}
