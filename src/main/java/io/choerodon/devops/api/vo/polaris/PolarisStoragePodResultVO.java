package io.choerodon.devops.api.vo.polaris;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author zmf
 * @since 2/19/20
 */
public class PolarisStoragePodResultVO {
    @ApiModelProperty("pod名称")
    private String name;

    @ApiModelProperty("校验结果")
    private List<PolarisResultItemVO> results;

    @ApiModelProperty("container的相关数据")
    private List<PolarisStorageContainerResultVO> containerResults;

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

    public List<PolarisStorageContainerResultVO> getContainerResults() {
        return containerResults;
    }

    public void setContainerResults(List<PolarisStorageContainerResultVO> containerResults) {
        this.containerResults = containerResults;
    }
}
