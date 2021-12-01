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
    private Long devopsCdJobId;

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

    public Long getDevopsCdJobId() {
        return devopsCdJobId;
    }

    public void setDevopsCdJobId(Long devopsCdJobId) {
        this.devopsCdJobId = devopsCdJobId;
    }

    @Override
    public String toString() {
        return "CdApiTestConfigForSagaVO{" +
                "apiTestTaskId=" + apiTestTaskId +
                ", apiTestConfigId=" + apiTestConfigId +
                ", devopsCdJobId=" + devopsCdJobId +
                '}';
    }
}
