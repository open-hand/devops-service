package io.choerodon.devops.api.vo.polaris;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author zmf
 * @since 2/19/20
 */
public class PolarisStorageContainerResultVO {
    @ApiModelProperty("容器name")
    private String name;
    @ApiModelProperty("校验结果")
    private List<PolarisResultItemVO> results;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PolarisResultItemVO> getResults() {
        return results;
    }

    public void setResults(List<PolarisResultItemVO> results) {
        this.results = results;
    }
}
