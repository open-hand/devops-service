package io.choerodon.devops.api.vo.polaris;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author zmf
 * @since 2/14/20
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PolarisControllerResultVO {
    @ApiModelProperty("资源的名称")
    private String name;
    @ApiModelProperty("资源的类型  Deployments,StatefulSets,DaemonSets,Jobs,CronJobs,ReplicationControllers")
    private String kind;
    @ApiModelProperty("所在集群namespace")
    private String namespace;
    @ApiModelProperty("pod相关扫描情况")
    private PolarisPodResultVO podResult;

    /**
     * 这个值的类型应该是Map，且结构是{@link PolarisResultItemVO}
     */
    @ApiModelProperty("校验结果")
    private Map<String, Object> results;

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

    public PolarisPodResultVO getPodResult() {
        return podResult;
    }

    public void setPodResult(PolarisPodResultVO podResult) {
        this.podResult = podResult;
    }

    public Map<String, Object> getResults() {
        return results;
    }

    public void setResults(Map<String, Object> results) {
        this.results = results;
    }
}
