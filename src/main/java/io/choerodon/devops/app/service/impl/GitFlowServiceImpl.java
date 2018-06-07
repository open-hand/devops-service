package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.GitFlowService;
import io.choerodon.devops.domain.application.entity.ApplicationE;
import io.choerodon.devops.domain.application.entity.ProjectE;
import io.choerodon.devops.domain.application.entity.UserAttrE;
import io.choerodon.devops.domain.application.entity.gitlab.GitFlowE;
import io.choerodon.devops.domain.application.event.GitFlowFinishPayload;
import io.choerodon.devops.domain.application.event.GitFlowStartPayload;
import io.choerodon.devops.domain.application.repository.ApplicationRepository;
import io.choerodon.devops.domain.application.repository.GitFlowRepository;
import io.choerodon.devops.domain.application.repository.IamRepository;
import io.choerodon.devops.domain.application.repository.UserAttrRepository;
import io.choerodon.devops.domain.application.valueobject.Organization;
import io.choerodon.devops.domain.service.IGitFlowService;
import io.choerodon.devops.infra.common.util.GitUserNameUtil;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.dataobject.gitlab.BranchDO;
import io.choerodon.devops.infra.dataobject.gitlab.TagsDO;
import io.choerodon.event.producer.execute.EventProducerTemplate;

/**
 * Created with IntelliJ IDEA.
 * User: Runge
 * Date: 2018/4/9
 * Time: 11:42
 * Description:
 */
@Component
public class GitFlowServiceImpl implements GitFlowService {
    private static final String FEATURE_PREFIX = "feature-";
    private static final String HOTFIX_PREFIX = "hotfix-";
    private static final String RELEAS_PREFIX = "release-";
    private static final String DEVOPS_SERVICE = "devops-service";

    @Value("${services.gitlab.url}")
    private String gitlabUrl;

    @Autowired
    private EventProducerTemplate eventProducerTemplate;
    @Autowired
    private GitFlowRepository gitFlowRepository;
    @Autowired
    private IGitFlowService igitflowservice;
    @Autowired
    private IamRepository iamRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private UserAttrRepository userAttrRepository;

    public Integer getGitlabUserId() {
        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        return TypeUtil.objToInteger(userAttrE.getGitlabUserId());
    }

    @Override
    public TagsDO getTags(Long projectId, Long applicationId, Integer page, Integer size) {
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        ApplicationE applicationE = applicationRepository.query(applicationId);
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        String urlSlash = gitlabUrl.endsWith("/") ? "" : "/";
        String path = String.format("%s%s%s-%s/%s",
                gitlabUrl, urlSlash, organization.getCode(), projectE.getCode(), applicationE.getCode());
        return gitFlowRepository.getTags(applicationId, path, page, size, getGitlabUserId());
    }

    @Override
    public String updateMRStatus(Long applicationId, String branchName) {
        Integer projectId = gitFlowRepository.getGitLabId(applicationId);
        Integer outcome = igitflowservice.getBranchState(projectId, branchName, getGitlabUserId());
        if (branchName.startsWith(HOTFIX_PREFIX) && outcome == 8) {
            outcome = 10;
        }
        return igitflowservice.getBranchStatus(branchName, outcome);
    }

    @Override
    public List<GitFlowE> getBranches(Long projectId, Long applicationId) {
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        ApplicationE applicationE = applicationRepository.query(applicationId);
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        String urlSlash = gitlabUrl.endsWith("/") ? "" : "/";
        String path = String.format("%s%s%s-%s/%s",
                gitlabUrl, urlSlash, organization.getCode(), projectE.getCode(), applicationE.getCode());
        Integer gitLabId = gitFlowRepository.getGitLabId(applicationId);
        List<BranchDO> branches =
                gitFlowRepository.listBranches(gitLabId, path, getGitlabUserId());
        return branches.stream()
                .map(t -> StringUtils.isEmpty(t.getName()) ? null : new GitFlowE(t.getName(), t.getCommit()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public String getReleaseNumber(Long applicationId, String branch) {
        Boolean branchPresent = branch != null;
        if (branchPresent && !branch.startsWith(RELEAS_PREFIX)) {
            throw new CommonException("error.release.branch.notMatch");
        }
        return !branchPresent
                ? igitflowservice.getReleaseNumber(applicationId, getGitlabUserId())
                : getNextTag(applicationId, branch, getGitlabUserId());
    }

    @Override
    public String getHotfixNumber(Long applicationId, String branch) {
        Boolean branchPresent = branch != null;
        if (branchPresent && !branch.startsWith(HOTFIX_PREFIX)) {
            throw new CommonException("error.hotfix.branch.notMatch");
        }
        return !branchPresent
                ? igitflowservice.getHotfixNumber(applicationId, getGitlabUserId())
                : getNextTag(applicationId, branch, getGitlabUserId());
    }

    @Override
    public void startGitFlow(Long applicationId, String branchName) {
        Integer projectId = gitFlowRepository.getGitLabId(applicationId);
        GitFlowStartPayload gitFlowStartPayload =
                new GitFlowStartPayload(projectId, branchName, getGitlabUserId());
        igitflowservice.createBranch(projectId, branchName);
        gitFlowStartEvent(gitFlowStartPayload);
    }

    @Override
    public void gitFlowStart(GitFlowStartPayload gitFlowStartPayload) {
        Integer projectId = gitFlowStartPayload.getProjectId();
        String branchName = gitFlowStartPayload.getBranchName();
        igitflowservice.createMergeRequest(projectId, branchName, gitFlowStartPayload.getUserId());
    }

    @Override
    public void finishGitFlowFeature(Long applicationId, String branchName) {
        if (!branchName.startsWith(FEATURE_PREFIX)) {
            throw new CommonException("error.gitFlow.event.create.feature");
        }
        finishGitFlowEvent(applicationId, branchName);
    }

    @Override
    public void finishGitFlow(Long applicationId, String branchName) {
        finishGitFlowEvent(applicationId, branchName);
    }

    private void finishGitFlowEvent(Long applicationId, String branchName) {
        Integer projectId = gitFlowRepository.getGitLabId(applicationId);
        // getBranchState 查看具体状态
        Integer branchState = igitflowservice.getBranchState(projectId, branchName, getGitlabUserId());
        if (branchName.startsWith(HOTFIX_PREFIX) && branchState == 8) {
            branchState = 10;
        }
        Integer devBranchStatus = branchState % 4;
        Integer masterBranchStatus = -1;
        if (!branchName.startsWith(FEATURE_PREFIX)) {
            masterBranchStatus = branchState / 4;
        }
        if (devBranchStatus != 1 && masterBranchStatus != 1) {
            gitFlowEndEvent(new GitFlowFinishPayload(
                    applicationId,
                    projectId,
                    branchName,
                    devBranchStatus,
                    masterBranchStatus,
                    getGitlabUserId()
            ));
        } else if (devBranchStatus == 1 && masterBranchStatus == 1) {
            throw new CommonException("error.gitFlow.mergeConflict.both");
        } else if (devBranchStatus == 1) {
            throw new CommonException("error.gitFlow.mergeConflict.dev");
        } else {
            throw new CommonException("error.gitFlow.mergeConflict.master");
        }
    }

    @Override
    public void gitFlowFinish(GitFlowFinishPayload gitFlowFinishPayload) {
        String branchName = gitFlowFinishPayload.getBranchName();
        Integer devMergeStatus = gitFlowFinishPayload.getDevMergeStatus();
        Integer masterMergeStatus = gitFlowFinishPayload.getMasterMergeStatus();
        Integer projectId = gitFlowFinishPayload.getProjectId();
        Long applicationId = gitFlowFinishPayload.getApplicationId();
        Integer userId = gitFlowFinishPayload.getUserId();

        if (!branchName.startsWith(FEATURE_PREFIX)) {
            igitflowservice.finishBranch(projectId, branchName, false, masterMergeStatus, userId);
            if (masterMergeStatus == 0 || masterMergeStatus == 3) {
                igitflowservice.createTag(applicationId, projectId, branchName, userId);
            }
        }
        igitflowservice.finishBranch(projectId, branchName, true, devMergeStatus, userId);
        igitflowservice.deleteBranchSafely(projectId, branchName, devMergeStatus, masterMergeStatus, userId);
    }

    private void gitFlowStartEvent(GitFlowStartPayload gitFlowStartPayload) {
        Exception exception = eventProducerTemplate.execute("gitFlowStart", DEVOPS_SERVICE, gitFlowStartPayload,
                (String uuid) -> {
                });
        if (exception != null) {
            throw new CommonException(exception.getMessage());
        }
    }

    private void gitFlowEndEvent(GitFlowFinishPayload gitFlowFinishPayload) {
        Exception exception = eventProducerTemplate.execute("gitFlowFinish", DEVOPS_SERVICE, gitFlowFinishPayload,
                (String uuid) -> {
                });
        if (exception != null) {
            throw new CommonException(exception.getMessage());
        }
    }

    private String getNextTag(Long applicationId, String branch, Integer userId) {
        return igitflowservice.getTag(applicationId, branch, userId);
    }
}
