package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * 资源详细扫描结果
 *
 * @author zmf
 * @since 2/17/20
 */
@ModifyAudit
@VersionAudit
@Table(name = "devops_polaris_namespace_detail")
public class DevopsPolarisNamespaceDetailDTO extends AuditDomain {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @ApiModelProperty("自增id")
    private Long id;

    /**
     * 当，是扫描的集群时，json结构是 {@link io.choerodon.devops.api.vo.polaris.PolarisStorageControllerResultVO} 的数组
     * 当，是扫描的环境时，json结构是 {@link io.choerodon.devops.api.vo.polaris.InstanceWithPolarisStorageVO} 的数组
     */
    @ApiModelProperty("是这个namespace下所有扫描数据json，根据扫描范围是env或者是cluster结构会有不同")
    private String detail;

    public DevopsPolarisNamespaceDetailDTO() {
    }

    public DevopsPolarisNamespaceDetailDTO(String detail) {
        this.detail = detail;
    }

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
