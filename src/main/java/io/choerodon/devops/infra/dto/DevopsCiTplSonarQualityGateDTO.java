
package io.choerodon.devops.infra.dto;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;

import java.util.List;

/**
 * sonar质量门
 * 〈〉
 *
 * @author lihao
 * @since 2022/11/18
 */
@Table(name = "devops_ci_tpl_sonar_quality_gate")
@ModifyAudit
@VersionAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class DevopsCiTplSonarQualityGateDTO {
    @Id
    @Encrypt
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ApiModelProperty("质量门关联的sonar配置id")
    private Long configId;
    @ApiModelProperty("是否开启质量门")
    private Boolean gatesEnable;
    @ApiModelProperty("质量门失败后是否阻塞后续job")
    private Boolean gatesBlockAfterFail;

    @ApiModelProperty("sonar质量门禁条件")
    @Transient
    private List<DevopsCiTplSonarQualityGateConditionDTO> sonarQualityGateConditionVOList;

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

    public Long getConfigId() {
        return configId;
    }

    public void setConfigId(Long configId) {
        this.configId = configId;
    }

    public List<DevopsCiTplSonarQualityGateConditionDTO> getSonarQualityGateConditionVOList() {
        return sonarQualityGateConditionVOList;
    }

    public void setSonarQualityGateConditionVOList(List<DevopsCiTplSonarQualityGateConditionDTO> sonarQualityGateConditionVOList) {
        this.sonarQualityGateConditionVOList = sonarQualityGateConditionVOList;
    }
}
