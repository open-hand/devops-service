package io.choerodon.devops.infra.dto.deploy;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

import java.util.Date;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/7/28 1:37
 */
@Table(name = "devops_hzero_deploy_details")
@ModifyAudit
@VersionAudit
public class DevopsHzeroDeployDetailsDTO extends AuditDomain {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ApiModelProperty("hzero部署记录id")
    @Encrypt
    private Long deployRecordId;

    @ApiModelProperty("环境id")
    @Encrypt
    private Long envId;

    @ApiModelProperty("市场服务id")
    @Encrypt
    private Long mktServiceId;

    @ApiModelProperty("部署对象id")
    @Encrypt
    private Long mktDeployObjectId;

    @ApiModelProperty("部署配置id")
    @Encrypt
    private Long valueId;

    /**
     * {@link io.choerodon.devops.infra.enums.HzeroDeployDetailsStatusEnum}
     */
    @ApiModelProperty("部署状态")
    private String status;

    @ApiModelProperty("应用id")
    private Long appId;

    @ApiModelProperty("应用名称")
    private String appName;

    @ApiModelProperty("实例code")
    private String appCode;

    @ApiModelProperty("部署顺序")
    private Long sequence;
    @Encrypt
    @ApiModelProperty("gitops 操作记录id")
    private Long commandId;

    @ApiModelProperty("开始部署时间")
    private Date startTime;

    @ApiModelProperty("结束部署时间")
    private Date endTime;

    @ApiModelProperty("hzero版本类型")
    private String hzeroType;

    public DevopsHzeroDeployDetailsDTO() {
    }

    public DevopsHzeroDeployDetailsDTO(Long deployRecordId,
                                       Long envId,
                                       Long mktServiceId,
                                       Long mktDeployObjectId,
                                       Long valueId,
                                       String status,
                                       String appCode,
                                       String appName,
                                       Long sequence,
                                       String hzeroType) {
        this.deployRecordId = deployRecordId;
        this.envId = envId;
        this.mktServiceId = mktServiceId;
        this.mktDeployObjectId = mktDeployObjectId;
        this.valueId = valueId;
        this.status = status;
        this.appCode = appCode;
        this.appName = appName;
        this.sequence = sequence;
        this.hzeroType = hzeroType;
    }

    public String getHzeroType() {
        return hzeroType;
    }

    public void setHzeroType(String hzeroType) {
        this.hzeroType = hzeroType;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Long getCommandId() {
        return commandId;
    }

    public void setCommandId(Long commandId) {
        this.commandId = commandId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDeployRecordId() {
        return deployRecordId;
    }

    public void setDeployRecordId(Long deployRecordId) {
        this.deployRecordId = deployRecordId;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public Long getMktServiceId() {
        return mktServiceId;
    }

    public void setMktServiceId(Long mktServiceId) {
        this.mktServiceId = mktServiceId;
    }

    public Long getMktDeployObjectId() {
        return mktDeployObjectId;
    }

    public void setMktDeployObjectId(Long mktDeployObjectId) {
        this.mktDeployObjectId = mktDeployObjectId;
    }

    public Long getValueId() {
        return valueId;
    }

    public void setValueId(Long valueId) {
        this.valueId = valueId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getSequence() {
        return sequence;
    }

    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
}
