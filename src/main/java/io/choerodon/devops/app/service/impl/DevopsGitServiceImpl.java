package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.devops.api.dto.DevopsBranchDTO;
import io.choerodon.devops.app.service.DevopsGitService;
import io.choerodon.devops.domain.application.entity.ApplicationE;
import io.choerodon.devops.domain.application.entity.DevopsBranchE;
import io.choerodon.devops.domain.application.entity.UserAttrE;
import io.choerodon.devops.domain.application.repository.ApplicationRepository;
import io.choerodon.devops.domain.application.repository.DevopsGitRepository;
import io.choerodon.devops.domain.application.repository.GitFlowRepository;
import io.choerodon.devops.domain.application.repository.UserAttrRepository;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.dataobject.gitlab.BranchDO;

/**
 * Creator: Runge
 * Date: 2018/7/2
 * Time: 14:44
 * Description:
 */
@Component
public class DevopsGitServiceImpl implements DevopsGitService {
    @Autowired
    private DevopsGitRepository devopsGitRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private GitFlowRepository gitFlowRepository;
    @Autowired
    private UserAttrRepository userAttrRepository;

    @Override
    public void createTag(Long projectId, Long appId, String tag, String ref) {
        applicationRepository.checkApp(projectId, appId);
        Integer gitLabProjectId = devopsGitRepository.getGitLabId(appId);
        Integer gitLabUserId = devopsGitRepository.getGitlabUserId();
        devopsGitRepository.createTag(gitLabProjectId, tag, ref, gitLabUserId);
    }

    @Override
    public void createBranch(Long projectId, Long applicationId, DevopsBranchDTO devopsBranchDTO) {
        DevopsBranchE devopsBranchE = ConvertHelper.convert(devopsBranchDTO, DevopsBranchE.class);
        devopsBranchE.initApplicationE(applicationId);
        ApplicationE applicationE = applicationRepository.query(applicationId);
        UserAttrE userAttrE = userAttrRepository.queryById(devopsBranchDTO.getUserId());
        BranchDO branchDO = gitFlowRepository.createBranch(TypeUtil.objToInteger(applicationE.getGitlabProjectE().getId()), devopsBranchDTO.getBranchName(), devopsBranchDTO.getOriginBranch(), TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
        devopsBranchE.setLastCommitDate(branchDO.getCommit().getCommittedDate());
        devopsGitRepository.createBranch(devopsBranchE);
    }
}
