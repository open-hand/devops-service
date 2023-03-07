package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * devops_cd_api_test_info(DevopsCdApiTestInfo)实体类
 *
 * @author hao.li@zknow.com
 * @since 2022-11-07
 */

@ApiModel("devops_ci_tpl_api_test_info_cfg")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_ci_tpl_api_test_info_cfg")
public class CiTplApiTestInfoCfgDTO extends AuditDomain {
    public static final String FIELD_ID = "id";
    public static final String FIELD_API_TEST_CONFIG_ID = "apiTestConfigId";
    public static final String FIELD_SEND_EMAIL = "sendEmail";
    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "是否启用告警设置")
    private Boolean enableWarningSetting;

    @ApiModelProperty(value = "阈值")
    private Double performThreshold;

    @ApiModelProperty(value = "是否发送邮件")
    private Boolean sendEmail;

    @ApiModelProperty(value = "是否发送站内信")
    private Boolean sendSiteMessage;

    @ApiModelProperty(value = "是否中断后续的任务")
    private Boolean blockAfterJob;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getEnableWarningSetting() {
        return enableWarningSetting;
    }

    public void setEnableWarningSetting(Boolean enableWarningSetting) {
        this.enableWarningSetting = enableWarningSetting;
    }

    public Double getPerformThreshold() {
        return performThreshold;
    }

    public void setPerformThreshold(Double performThreshold) {
        this.performThreshold = performThreshold;
    }

    public Boolean getSendEmail() {
        return sendEmail;
    }

    public void setSendEmail(Boolean sendEmail) {
        this.sendEmail = sendEmail;
    }

    public Boolean getSendSiteMessage() {
        return sendSiteMessage;
    }

    public void setSendSiteMessage(Boolean sendSiteMessage) {
        this.sendSiteMessage = sendSiteMessage;
    }

    public Boolean getBlockAfterJob() {
        return blockAfterJob;
    }

    public void setBlockAfterJob(Boolean blockAfterJob) {
        this.blockAfterJob = blockAfterJob;
    }

}

