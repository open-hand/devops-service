package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;
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
    @ApiModelProperty(value = "测试任务类型")
    private String taskType;
    @Encrypt
    private Long apiTestConfigId;
    @Encrypt
    private Long apiTestSuiteId;
    private String deployJobName;
    private WarningSettingVO warningSettingVO;

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public Long getApiTestSuiteId() {
        return apiTestSuiteId;
    }

    public void setApiTestSuiteId(Long apiTestSuiteId) {
        this.apiTestSuiteId = apiTestSuiteId;
    }

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

    public Long getApiTestConfigId() {
        return apiTestConfigId;
    }

    public void setApiTestConfigId(Long apiTestConfigId) {
        this.apiTestConfigId = apiTestConfigId;
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
                ", apiTestConfigId=" + apiTestConfigId +
                ", deployJobName='" + deployJobName + '\'' +
                ", warningSettingVO=" + warningSettingVO +
                '}';
    }
}
