package io.choerodon.devops.domain.application.event;

import java.util.List;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  11:33 2019/3/14
 * Description: 传递所有要迁移的gitlabProjectId
 */
public class DevopsVariablePayLoad {
    private List<Integer> gitlabProjectIds;
    private Long harborConfigId;
    private Long chartConfigId;

    public DevopsVariablePayLoad(List<Integer> gitlabProjectIds, Long harborConfigId, Long chartConfigId) {
        this.gitlabProjectIds = gitlabProjectIds;
        this.harborConfigId = harborConfigId;
        this.chartConfigId = chartConfigId;
    }

    public List<Integer> getGitlabProjectIds() {
        return gitlabProjectIds;
    }

    public void setGitlabProjectIds(List<Integer> gitlabProjectIds) {
        this.gitlabProjectIds = gitlabProjectIds;
    }

    public Long getHarborConfigId() {
        return harborConfigId;
    }

    public void setHarborConfigId(Long harborConfigId) {
        this.harborConfigId = harborConfigId;
    }

    public Long getChartConfigId() {
        return chartConfigId;
    }

    public void setChartConfigId(Long chartConfigId) {
        this.chartConfigId = chartConfigId;
    }
}
