package io.choerodon.devops.api.vo;

import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.infra.dto.DevopsCdEnvDeployInfoDTO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/9/9 9:55
 */
public class CdApiTestConfigVO extends DevopsCdEnvDeployInfoDTO {
    @Encrypt
    private Long apiTestTaskId;
    private String apiTestTaskName;
    private Boolean blockAfterJob;


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

    public Boolean getBlockAfterJob() {
        return blockAfterJob;
    }

    public void setBlockAfterJob(Boolean blockAfterJob) {
        this.blockAfterJob = blockAfterJob;
    }
}
