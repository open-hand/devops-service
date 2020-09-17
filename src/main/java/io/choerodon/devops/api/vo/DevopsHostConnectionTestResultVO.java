package io.choerodon.devops.api.vo;

import javax.annotation.Nullable;

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

    @Nullable
    @ApiModelProperty("主机连接错误信息 / 可为空")
    private String hostCheckError;

    @Nullable
    @ApiModelProperty("jmeter_check_error / 可为空")
    private String jmeterCheckError;

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

    @Nullable
    public String getHostCheckError() {
        return hostCheckError;
    }

    public void setHostCheckError(@Nullable String hostCheckError) {
        this.hostCheckError = hostCheckError;
    }

    @Nullable
    public String getJmeterCheckError() {
        return jmeterCheckError;
    }

    public void setJmeterCheckError(@Nullable String jmeterCheckError) {
        this.jmeterCheckError = jmeterCheckError;
    }

    @Override
    public String toString() {
        return "DevopsHostConnectionTestResultVO{" +
                "sshStatus='" + sshStatus + '\'' +
                ", jmeterStatus='" + jmeterStatus + '\'' +
                ", hostCheckError='" + hostCheckError + '\'' +
                ", jmeterCheckError='" + jmeterCheckError + '\'' +
                '}';
    }
}
