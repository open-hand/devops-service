package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author zmf
 * @since 2020/9/15
 */
public class DevopsHostConnectionTestResultVO {
    /**
     * {@link io.choerodon.devops.infra.enums.DevopsHostStatus}
     */
    @ApiModelProperty("ssh校验结果")
    private String sshStatus;

    /**
     * {@link io.choerodon.devops.infra.enums.DevopsHostStatus}
     */
    @ApiModelProperty("jmeter校验结果 / 可为空")
    private String jmeterStatus;

    public String getSshStatus() {
        return sshStatus;
    }

    public void setSshStatus(String sshStatus) {
        this.sshStatus = sshStatus;
    }

    public String getJmeterStatus() {
        return jmeterStatus;
    }

    public void setJmeterStatus(String jmeterStatus) {
        this.jmeterStatus = jmeterStatus;
    }
}
