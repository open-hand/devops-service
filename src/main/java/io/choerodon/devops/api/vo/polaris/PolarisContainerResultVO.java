package io.choerodon.devops.api.vo.polaris;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author zmf
 * @since 2/14/20
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PolarisContainerResultVO {

    @ApiModelProperty("容器name")
    private String name;
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

    public Map<String, Object> getResults() {
        return results;
    }

    public void setResults(Map<String, Object> results) {
        this.results = results;
    }
}
