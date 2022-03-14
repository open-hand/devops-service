package io.choerodon.devops.api.vo.pipeline;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/12/27 15:47
 */
public class CiNodeJsReportVO {
    private DevopsCiUnitTestResultVO stats;

    public DevopsCiUnitTestResultVO getStats() {
        return stats;
    }

    public void setStats(DevopsCiUnitTestResultVO stats) {
        this.stats = stats;
    }
}
