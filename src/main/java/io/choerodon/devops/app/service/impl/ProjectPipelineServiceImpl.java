package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.CheckGitlabAccessLevelService;
import io.choerodon.devops.app.service.ProjectPipelineService;
import io.choerodon.devops.app.service.UserAttrService;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.UserAttrDTO;
import io.choerodon.devops.infra.enums.AppServiceEvent;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.mapper.AppServiceMapper;
import io.choerodon.devops.infra.util.GitUserNameUtil;
import io.choerodon.devops.infra.util.TypeUtil;

/**
 * Created by Zenger on 2018/4/10.
 */
@Service
public class ProjectPipelineServiceImpl implements ProjectPipelineService {
    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator;
    @Autowired
    private UserAttrService userAttrService;
    @Autowired
    private CheckGitlabAccessLevelService checkGitlabAccessLevelService;
    @Autowired
    private AppServiceMapper appServiceMapper;


    public Integer getGitlabUserId() {
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        return TypeUtil.objToInteger(userAttrDTO.getGitlabUserId());
    }


    @Override
    public Boolean retry(Long gitlabProjectId, Long pipelineId) {
        AppServiceDTO appServiceDTO = appServiceMapper.selectOne(new AppServiceDTO().setGitlabProjectId(TypeUtil.objToInteger(gitlabProjectId)));
        checkGitlabAccessLevelService.checkGitlabPermission(appServiceDTO.getProjectId(), appServiceDTO.getId(), AppServiceEvent.CICD_OPERATION);
        return gitlabServiceClientOperator.retryPipeline(gitlabProjectId.intValue(),
                pipelineId.intValue(), getGitlabUserId(), null) != null;
    }

    @Override
    public Boolean cancel(Long gitlabProjectId, Long pipelineId) {
        AppServiceDTO appServiceDTO = appServiceMapper.selectOne(new AppServiceDTO().setGitlabProjectId(TypeUtil.objToInteger(gitlabProjectId)));
        checkGitlabAccessLevelService.checkGitlabPermission(appServiceDTO.getProjectId(), appServiceDTO.getId(), AppServiceEvent.CICD_OPERATION);
        return gitlabServiceClientOperator.cancelPipeline(gitlabProjectId.intValue(),
                pipelineId.intValue(), getGitlabUserId(), null) != null;
    }
}
