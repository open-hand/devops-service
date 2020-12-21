package io.choerodon.devops.infra.dto.market;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * @author zmf
 * @since 2020/12/18
 */
@VersionAudit
@ModifyAudit
@Table(name = "market_chart_value")
public class MarketChartValueDTO extends AuditDomain {
    @Encrypt
    @Id
    @GeneratedValue
    @ApiModelProperty("主键")
    private Long id;

    @NotNull
    @ApiModelProperty("对应的chart包的values")
    private String value;

    @NotNull
    @ApiModelProperty("市场部署对象id")
    private Long marketDeployObjectId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Long getMarketDeployObjectId() {
        return marketDeployObjectId;
    }

    public void setMarketDeployObjectId(Long marketDeployObjectId) {
        this.marketDeployObjectId = marketDeployObjectId;
    }
}
