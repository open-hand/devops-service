package io.choerodon.devops.domain.application.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.domain.application.entity.DevopsGitlabPipelineE;
import io.choerodon.devops.infra.dataobject.DevopsGitlabPipelineDO;

@Component
public class DevopsGitlabPipelineConvertor implements ConvertorI<DevopsGitlabPipelineE, DevopsGitlabPipelineDO, Object> {

    @Override
    public DevopsGitlabPipelineE doToEntity(DevopsGitlabPipelineDO devopsGitlabPipelineDO) {
        DevopsGitlabPipelineE devopsGitlabPipelineE = new DevopsGitlabPipelineE();
        BeanUtils.copyProperties(devopsGitlabPipelineDO, devopsGitlabPipelineE);
        if (devopsGitlabPipelineDO.getCommitId() != null) {
            devopsGitlabPipelineE.initDevopsGitlabCommitE(devopsGitlabPipelineDO.getCommitId(), devopsGitlabPipelineDO.getRef(), devopsGitlabPipelineDO.getSha(), devopsGitlabPipelineDO.getCommitUserId(), devopsGitlabPipelineDO.getContent());
        }
        return devopsGitlabPipelineE;
    }

    @Override
    public DevopsGitlabPipelineDO entityToDo(DevopsGitlabPipelineE devopsGitlabPipelineE) {
        DevopsGitlabPipelineDO devopsGitlabPipelineDO = new DevopsGitlabPipelineDO();
        BeanUtils.copyProperties(devopsGitlabPipelineE, devopsGitlabPipelineDO);
        if (devopsGitlabPipelineE.getDevopsGitlabCommitE() != null) {
            devopsGitlabPipelineDO.setCommitId(devopsGitlabPipelineE.getDevopsGitlabCommitE().getId()
            );
        }

        return devopsGitlabPipelineDO;
    }
}
