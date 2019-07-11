package io.choerodon.devops.infra.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.iam.entity.DevopsGitlabPipelineE;
import io.choerodon.devops.infra.dto.DevopsGitlabPipelineDO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/DevopsGitlabPipelineConvertor.java
=======
import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.iam.entity.DevopsGitlabPipelineE;
import io.choerodon.devops.infra.dataobject.DevopsGitlabPipelineDO;

>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/DevopsGitlabPipelineConvertor.java
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
