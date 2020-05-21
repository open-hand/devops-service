package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.annotations.ApiModelProperty;

<<<<<<< HEAD
import io.choerodon.mybatis.entity.BaseDTO;
=======
import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;
>>>>>>> [ADD] add ModifyAudit VersionAudit for table dto

/**
 * 按类别分的资源详细扫描结果
 *
 * @author zmf
 * @since 2/17/20
 */
@ModifyAudit
@VersionAudit
@Table(name = "devops_polaris_category_detail")
public class DevopsPolarisCategoryDetailDTO extends BaseDTO {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @ApiModelProperty("自增id")
    private Long id;

    /**
     * json结构是 {@link io.choerodon.devops.api.vo.polaris.PolarisSummaryItemContentVO} 数组
     */
    @ApiModelProperty("每一类的扫描结果json数据")
    private String detail;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }
}
