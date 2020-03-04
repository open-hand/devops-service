package io.choerodon.devops.api.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author zmf
 * @since 2/18/20
 */
public class ClusterPolarisSummaryItemVO {
    @ApiModelProperty("分数")
    private Long score;

    /**
     * {@link io.choerodon.devops.api.vo.polaris.PolarisSummaryItemContentVO} 数组
     */
    @ApiModelProperty("详情json")
    private String detail;

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

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
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
