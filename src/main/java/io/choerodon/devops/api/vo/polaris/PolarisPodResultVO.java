package io.choerodon.devops.api.vo.polaris;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author zmf
 * @since 2/14/20
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PolarisPodResultVO {
    @ApiModelProperty("pod名称")
    private String name;

    /**
     * 这个值的类型应该是Map，且结构是{@link PolarisResultItemVO}
     */
    @ApiModelProperty("校验结果")
    private Map<String, Object> results;

    @ApiModelProperty("container的相关数据")
    private List<PolarisContainerResultVO> containerResults;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Object> getResults() {
        return results;
    }

    public void setResults(Map<String, Object> results) {
        this.results = results;
    }

    public List<PolarisContainerResultVO> getContainerResults() {
        return containerResults;
    }

    public void setContainerResults(List<PolarisContainerResultVO> containerResults) {
        this.containerResults = containerResults;
    }
}
