package io.choerodon.devops.api.vo.polaris;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author zmf
 * @since 2/19/20
 */
public class PolarisStorageControllerResultVO {
    @ApiModelProperty("资源的名称")
    private String name;
    @ApiModelProperty("资源的类型  Deployments,StatefulSets,DaemonSets,Jobs,CronJobs,ReplicationControllers")
    private String kind;
    @ApiModelProperty("所在集群namespace")
    private String namespace;
    @ApiModelProperty("pod相关扫描情况")
    private PolarisStoragePodResultVO podResult;
    @ApiModelProperty("校验结果")
    private List<PolarisResultItemVO> results;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public PolarisStoragePodResultVO getPodResult() {
        return podResult;
    }

    public void setPodResult(PolarisStoragePodResultVO podResult) {
        this.podResult = podResult;
    }

    public List<PolarisResultItemVO> getResults() {
        return results;
    }

    public void setResults(List<PolarisResultItemVO> results) {
        this.results = results;
    }
}
