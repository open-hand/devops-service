
package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;

/**
 * sonar质量门
 * 〈〉
 *
 * @author lihao
 * @since 2022/11/18
 */
@Table(name = "devops_ci_sonar_quality_gate")
@ModifyAudit
@VersionAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class DevopsCiSonarQualityGateDTO {
    @Id
    @Encrypt
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ApiModelProperty("质量门在sonar的id")
    private String sonarGateId;

    @ApiModelProperty("质量门名称")
    private String name;

    @ApiModelProperty("质量门关联的sonar配置id")
    private Long configId;

    @ApiModelProperty("是否开启质量门")
    private Boolean gatesEnable;
    @ApiModelProperty("质量门失败后是否阻塞后续job")
    private Boolean gatesBlockAfterFail;

    public Boolean getGatesEnable() {
        return gatesEnable;
    }

    public void setGatesEnable(Boolean gatesEnable) {
        this.gatesEnable = gatesEnable;
    }

    public Boolean getGatesBlockAfterFail() {
        return gatesBlockAfterFail;
    }

    public void setGatesBlockAfterFail(Boolean gatesBlockAfterFail) {
        this.gatesBlockAfterFail = gatesBlockAfterFail;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getConfigId() {
        return configId;
    }

    public void setConfigId(Long configId) {
        this.configId = configId;
    }

    public String getSonarGateId() {
        return sonarGateId;
    }

    public void setSonarGateId(String sonarGateId) {
        this.sonarGateId = sonarGateId;
    }
}
