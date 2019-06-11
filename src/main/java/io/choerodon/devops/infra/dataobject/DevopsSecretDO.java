package io.choerodon.devops.infra.dataobject;

import javax.persistence.*;

import io.choerodon.mybatis.entity.BaseDTO;

/**
 * Created by n!Ck
 * Date: 18-12-4
 * Time: 上午10:08
 * Description:
 */

@Table(name = "devops_secret")
public class DevopsSecretDO extends BaseDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long envId;
    private String name;
    private String description;
    private String value;
    private Long commandId;

    @Transient
    private String status;
    @Transient
    private String commandType;

    public DevopsSecretDO() {
    }

    public DevopsSecretDO(Long id) {
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }
}
