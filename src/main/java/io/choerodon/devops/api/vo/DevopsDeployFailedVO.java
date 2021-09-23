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
    private Long instanceId;
    @Encrypt
    private Long commandId;

    public DevopsDeployFailedVO() {
    }

    public DevopsDeployFailedVO(Long instanceId, Long commandId) {
        this.instanceId = instanceId;
        this.commandId = commandId;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public Long getCommandId() {
        return commandId;
    }

    public void setCommandId(Long commandId) {
        this.commandId = commandId;
    }
}
