package io.choerodon.devops.api.vo;

import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/8/27 10:59
 */
public class DevopsDeployFailedVO {
    @Encrypt
    private Long envId;
    private String instanceCode;
    @Encrypt
    private Long commandId;

    public DevopsDeployFailedVO(Long envId, String instanceCode, Long commandId) {
        this.envId = envId;
        this.instanceCode = instanceCode;
        this.commandId = commandId;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public String getInstanceCode() {
        return instanceCode;
    }

    public void setInstanceCode(String instanceCode) {
        this.instanceCode = instanceCode;
    }

    public Long getCommandId() {
        return commandId;
    }

    public void setCommandId(Long commandId) {
        this.commandId = commandId;
    }
}
