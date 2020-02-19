package io.choerodon.devops.api.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import springfox.documentation.annotations.ApiIgnore;

/**
 * @author zmf
 * @since 2/18/20
 */
public class ClusterPolarisSummaryItemVO {
    @ApiModelProperty("分数")
    private Long score;

    @ApiModelProperty("内容items")
    private List<ClusterPolarisSummaryItemContentVO> items;

    @ApiModelProperty(value = "类别", hidden = true)
    @JsonIgnore
    private String category;

    @ApiModelProperty("是否有error级别的检测项")
    private Boolean hasErrors;

    public Long getScore() {
        return score;
    }

    public void setScore(Long score) {
        this.score = score;
    }

    public List<ClusterPolarisSummaryItemContentVO> getItems() {
        return items;
    }

    public void setItems(List<ClusterPolarisSummaryItemContentVO> items) {
        this.items = items;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Boolean getHasErrors() {
        return hasErrors;
    }

    public void setHasErrors(Boolean hasErrors) {
        this.hasErrors = hasErrors;
    }
}
