package io.choerodon.devops.infra.dto;

import java.util.List;
import javax.persistence.*;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.devops.api.vo.polaris.PolarisSummaryItemContentVO;
import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * @author zmf
 * @since 2/17/20
 */
@ModifyAudit
@VersionAudit
@Table(name = "devops_polaris_category_result")
public class DevopsPolarisCategoryResultDTO extends AuditDomain {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @ApiModelProperty("自增主键")
    private Long id;

    @ApiModelProperty("这个type所属的类型")
    private String category;

    @ApiModelProperty("扫描纪录id")
    private Long recordId;

    @ApiModelProperty("此条资源详细扫描纪录id")
    private Long detailId;

    @ApiModelProperty("扫描结果的得分")
    private Long score;

    @ApiModelProperty("是否有error级别的检测项")
    private Boolean hasErrors;

    @Transient
    @ApiModelProperty("详情json")
    private String detail;

    @Transient
    @ApiModelProperty("这个类别对应的items")
    private List<PolarisSummaryItemContentVO> items;

    public DevopsPolarisCategoryResultDTO() {
    }

    public DevopsPolarisCategoryResultDTO(String category, Long recordId, Long detailId, Long score, Boolean hasErrors, String detail, List<PolarisSummaryItemContentVO> items) {
        this.category = category;
        this.recordId = recordId;
        this.detailId = detailId;
        this.score = score;
        this.hasErrors = hasErrors;
        this.detail = detail;
        this.items = items;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public Long getDetailId() {
        return detailId;
    }

    public void setDetailId(Long detailId) {
        this.detailId = detailId;
    }

    public Long getScore() {
        return score;
    }

    public void setScore(Long score) {
        this.score = score;
    }

    public Boolean getHasErrors() {
        return hasErrors;
    }

    public void setHasErrors(Boolean hasErrors) {
        this.hasErrors = hasErrors;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public List<PolarisSummaryItemContentVO> getItems() {
        return items;
    }

    public void setItems(List<PolarisSummaryItemContentVO> items) {
        this.items = items;
    }
}
