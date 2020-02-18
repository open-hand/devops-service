package io.choerodon.devops.api.vo.polaris;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModelProperty;

import io.choerodon.devops.api.vo.ClusterSummaryInfoVO;

/**
 * @author zmf
 * @since 2/14/20
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PolarisScanAuditDataVO {
    @ApiModelProperty("集群信息")
    private ClusterSummaryInfoVO clusterInfo;
    @ApiModelProperty("扫描出的配置文件结果")
    private List<PolarisControllerResultVO> results;

    public ClusterSummaryInfoVO getClusterInfo() {
        return clusterInfo;
    }

    public void setClusterInfo(ClusterSummaryInfoVO clusterInfo) {
        this.clusterInfo = clusterInfo;
    }

    public List<PolarisControllerResultVO> getResults() {
        return results;
    }

    public void setResults(List<PolarisControllerResultVO> results) {
        this.results = results;
    }
}
