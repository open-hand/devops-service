package io.choerodon.devops.api.vo;

import org.hzero.starter.keyencrypt.core.Encrypt;

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
    @Encrypt
    private Long apiTestConfigId;
    @Encrypt
    private Long devopsCdJobRecordId;

    public Long getApiTestTaskId() {
        return apiTestTaskId;
    }

    public void setApiTestTaskId(Long apiTestTaskId) {
        this.apiTestTaskId = apiTestTaskId;
    }

    public Long getApiTestConfigId() {
        return apiTestConfigId;
    }

    public void setApiTestConfigId(Long apiTestConfigId) {
        this.apiTestConfigId = apiTestConfigId;
    }

    public Long getDevopsCdJobRecordId() {
        return devopsCdJobRecordId;
    }

    public void setDevopsCdJobRecordId(Long devopsCdJobRecordId) {
        this.devopsCdJobRecordId = devopsCdJobRecordId;
    }

    @Override
    public String toString() {
        return "CdApiTestConfigForSagaVO{" +
                "apiTestTaskId=" + apiTestTaskId +
                ", apiTestConfigId=" + apiTestConfigId +
                ", devopsCdJobRecordId=" + devopsCdJobRecordId +
                '}';
    }
}
