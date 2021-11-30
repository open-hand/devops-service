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
public class CdApiTestConfigForSagaVO {
    @Encrypt
    private Long apiTestTaskId;
    private String apiTestTaskName;
    @Encrypt
    private Long apITestConfigId;
    private String apiTestConfigName;
    private String deployJobName;
    private WarningSettingVO warningSettingVO;
    private Long devopsCdJobRecordId;

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

    public String getApiTestConfigName() {
        return apiTestConfigName;
    }

    public void setApiTestConfigName(String apiTestConfigName) {
        this.apiTestConfigName = apiTestConfigName;
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

    public Long getDevopsCdJobRecordId() {
        return devopsCdJobRecordId;
    }

    public void setDevopsCdJobRecordId(Long devopsCdJobRecordId) {
        this.devopsCdJobRecordId = devopsCdJobRecordId;
    }

    @Override
    public String toString() {
        return "CdApiTestConfigVO{" +
                "apiTestTaskId=" + apiTestTaskId +
                ", apiTestTaskName='" + apiTestTaskName + '\'' +
                ", apITestConfigId=" + apITestConfigId +
                ", apiTestConfigName='" + apiTestConfigName + '\'' +
                ", deployJobName='" + deployJobName + '\'' +
                ", warningSettingVO=" + warningSettingVO +
                '}';
    }
}
