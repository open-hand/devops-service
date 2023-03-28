package io.choerodon.devops.api.vo;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * 证书及其相关的Ingress名称
 *
 * @author zmf
 */
public class CertificationRespVO extends DevopsResourceDataInfoVO {
    @Encrypt
    @ApiModelProperty("证书id")
    private Long id;
    @ApiModelProperty("证书名称")
    private String name;
    @ApiModelProperty("泛域名")
    private String commonName;
    @ApiModelProperty("DNS名称")
    private List<String> DNSNames;
    @ApiModelProperty("证书关联的ingress名称")
    private List<String> ingresses;
    @ApiModelProperty("生效时间")
    private Date validFrom;
    @ApiModelProperty("过期时间")
    private Date validUntil;

    @ApiModelProperty("命令类型")
    private String commandType;

    @ApiModelProperty("命令状态")
    private String commandStatus;

    @ApiModelProperty("命令错误")
    private String error;

    private List<C7nCertificationCreateOrUpdateVO.NotifyObject> notifyObjects;

    @ApiModelProperty("是否设置到期前通知")
    private Boolean expireNotice;

    @ApiModelProperty("到期提前多长时间通知")
    private Integer advanceDays;

    @Encrypt
    private Long certId;

    @ApiModelProperty("绑定的域名,如果证书类型是选择证书，将会被设置成去掉泛域名后的前缀部分")
    private List<String> domains;
    @ApiModelProperty("绑定的完整域名")
    private List<String> fullDomains;

    private String type;
    private String certValue;
    private String keyValue;

    private Long objectVersionNumber;

    public List<String> getDomains() {
        return domains;
    }

    public void setDomains(List<String> domains) {
        this.domains = domains;
    }

    public List<String> getFullDomains() {
        return fullDomains;
    }

    public void setFullDomains(List<String> fullDomains) {
        this.fullDomains = fullDomains;
    }

    public Long getCertId() {
        return certId;
    }

    public void setCertId(Long certId) {
        this.certId = certId;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCertValue() {
        return certValue;
    }

    public void setCertValue(String certValue) {
        this.certValue = certValue;
    }

    public String getKeyValue() {
        return keyValue;
    }

    public void setKeyValue(String keyValue) {
        this.keyValue = keyValue;
    }

    public List<C7nCertificationCreateOrUpdateVO.NotifyObject> getNotifyObjects() {
        return notifyObjects;
    }

    public void setNotifyObjects(List<C7nCertificationCreateOrUpdateVO.NotifyObject> notifyObjects) {
        this.notifyObjects = notifyObjects;
    }

    public Boolean getExpireNotice() {
        return expireNotice;
    }

    public void setExpireNotice(Boolean expireNotice) {
        this.expireNotice = expireNotice;
    }

    public Integer getAdvanceDays() {
        return advanceDays;
    }

    public void setAdvanceDays(Integer advanceDays) {
        this.advanceDays = advanceDays;
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

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    /**
     * 不使用此注解，则DNSNames这个字段会被序列化为dnsnames
     */
    @JsonProperty(value = "DNSNames")
    public List<String> getDNSNames() {
        return DNSNames;
    }

    public void setDNSNames(List<String> DNSNames) {
        this.DNSNames = DNSNames;
    }

    public List<String> getIngresses() {
        return ingresses;
    }

    public void setIngresses(List<String> ingresses) {
        this.ingresses = ingresses;
    }

    public Date getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
    }

    public Date getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(Date validUntil) {
        this.validUntil = validUntil;
    }

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public String getCommandStatus() {
        return commandStatus;
    }

    public void setCommandStatus(String commandStatus) {
        this.commandStatus = commandStatus;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
