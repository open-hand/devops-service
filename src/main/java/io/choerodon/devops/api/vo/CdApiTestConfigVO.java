package io.choerodon.devops.api.vo;

import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.api.vo.pipeline.WarningSettingVO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/9/9 9:55
 */
public class CdApiTestConfigVO {
    @Encrypt
    private Long apiTestTaskId;
    private String apiTestTaskName;
    @Encrypt
    private Long apITestConfigId;
    private String deployJobName;
    private WarningSettingVO warningSettingVO;


    public Long getApiTestTaskId() {
        return apiTestTaskId;
    }

    public void setApiTestTaskId(Long apiTestTaskId) {
        this.apiTestTaskId = apiTestTaskId;
    }

    public String getApiTestTaskName() {
        return apiTestTaskName;
    }

    public void setApiTestTaskName(String apiTestTaskName) {
        this.apiTestTaskName = apiTestTaskName;
    }

    public Long getApITestConfigId() {
        return apITestConfigId;
    }

    public void setApITestConfigId(Long apITestConfigId) {
        this.apITestConfigId = apITestConfigId;
    }

    public String getDeployJobName() {
        return deployJobName;
    }

    public void setDeployJobName(String deployJobName) {
        this.deployJobName = deployJobName;
    }

    public WarningSettingVO getWarningSettingVO() {
        return warningSettingVO;
    }

    public void setWarningSettingVO(WarningSettingVO warningSettingVO) {
        this.warningSettingVO = warningSettingVO;
    }

    @Override
    public String toString() {
        return "CdApiTestConfigVO{" +
                "apiTestTaskId=" + apiTestTaskId +
                ", apiTestTaskName='" + apiTestTaskName + '\'' +
                ", apITestConfigId=" + apITestConfigId +
                ", deployJobName='" + deployJobName + '\'' +
                ", warningSettingVO=" + warningSettingVO +
                '}';
    }
}
