package io.choerodon.devops.infra.dto;

import java.util.Map;
import javax.persistence.*;

import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * Created by n!Ck
 * Date: 18-12-4
 * Time: 上午10:08
 * Description:
 */
@ModifyAudit
@VersionAudit
@Table(name = "devops_secret")
public class DevopsSecretDTO extends AuditDomain {
    public static final String ENCRYPT_KEY = "devops_secret";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Encrypt(DevopsSecretDTO.ENCRYPT_KEY)
    private Long id;
    private Long envId;
    private String name;
    private String description;
    private String value;
    private Long commandId;
    private Long appServiceId;

    @Transient
    private String commandStatus;
    @Transient
    private String commandType;
    @Transient
    private String error;
    @Transient
    private Map<String, String> valueMap;

    public Map<String, String> getValueMap() {
        return valueMap;
    }

    public void setValueMap(Map<String, String> valueMap) {
        this.valueMap = valueMap;
    }

    public DevopsSecretDTO() {
    }

    public DevopsSecretDTO(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Long getCommandId() {
        return commandId;
    }

    public void setCommandId(Long commandId) {
        this.commandId = commandId;
    }

    public String getCommandStatus() {
        return commandStatus;
    }

    public void setCommandStatus(String commandStatus) {
        this.commandStatus = commandStatus;
    }

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }
}
