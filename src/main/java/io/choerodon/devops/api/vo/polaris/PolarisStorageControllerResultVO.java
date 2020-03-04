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
    @ApiModelProperty("是否有error级别的检测项")
    private Boolean hasErrors;

    public PolarisStorageControllerResultVO() {
    }

    public PolarisStorageControllerResultVO(Boolean hasErrors) {
        this.hasErrors = hasErrors;
    }

    public PolarisStorageControllerResultVO(String name, String kind, String namespace, PolarisStoragePodResultVO podResult, List<PolarisResultItemVO> results, Boolean hasErrors) {
        this.name = name;
        this.kind = kind;
        this.namespace = namespace;
        this.podResult = podResult;
        this.results = results;
        this.hasErrors = hasErrors;
    }

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

    public Boolean getHasErrors() {
        return hasErrors;
    }

    public void setHasErrors(Boolean hasErrors) {
        this.hasErrors = hasErrors;
    }
}
