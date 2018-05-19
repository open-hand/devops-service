package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.GitFlowService;
import io.choerodon.devops.domain.application.entity.ApplicationE;
import io.choerodon.devops.domain.application.entity.ProjectE;
import io.choerodon.devops.domain.application.entity.gitlab.GitFlowE;
import io.choerodon.devops.domain.application.event.GitFlowFinishPayload;
import io.choerodon.devops.domain.application.event.GitFlowStartPayload;
import io.choerodon.devops.domain.application.repository.ApplicationRepository;
import io.choerodon.devops.domain.application.repository.GitFlowRepository;
import io.choerodon.devops.domain.application.repository.IamRepository;
import io.choerodon.devops.domain.application.valueobject.Organization;
import io.choerodon.devops.domain.service.IGitFlowService;
import io.choerodon.devops.infra.common.util.GitUserNameUtil;
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
    private static final String DEVOPS_SERVICE = "devops-service";
    private final EventProducerTemplate eventProducerTemplate;
    @Value("${services.gitlab.url}")
    private String gitlabUrl;
    private GitFlowRepository gitFlowRepository;
    private IGitFlowService igitflowservice;
    private IamRepository iamRepository;
    private ApplicationRepository applicationRepository;

    /**
     * 构造方法
     */
    public GitFlowServiceImpl(GitFlowRepository gitFlowRepository,
                              IGitFlowService igitflowservice,
                              EventProducerTemplate eventProducerTemplate,
                              IamRepository iamRepository,
                              ApplicationRepository applicationRepository) {
        this.gitFlowRepository = gitFlowRepository;
        this.igitflowservice = igitflowservice;
        this.eventProducerTemplate = eventProducerTemplate;
        this.iamRepository = iamRepository;
        this.applicationRepository = applicationRepository;
    }

    @Override
    public TagsDO getTags(Long projectId, Long applicationId, Integer page, Integer size) {
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        ApplicationE applicationE = applicationRepository.query(applicationId);
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        String path = String.format("%s/%s-%s/%s",
                gitlabUrl, organization.getCode(), projectE.getCode(), applicationE.getCode());
        return gitFlowRepository.getTags(applicationId, path, page, size);
    }

    @Override
    public String updateMRStatus(Long applicationId, String branchName) {
        Integer projectId = gitFlowRepository.getGitLabId(applicationId);
        String username = GitUserNameUtil.getUsername();
        Integer outcome = igitflowservice.getBranchState(projectId, branchName, username);
        return igitflowservice.getBranchStatus(branchName, outcome);
    }

    @Override
    public List<GitFlowE> getBranches(Long projectId, Long applicationId) {
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        ApplicationE applicationE = applicationRepository.query(applicationId);
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        String path = String.format("%s/%s-%s/%s",
                gitlabUrl, organization.getCode(), projectE.getCode(), applicationE.getCode());
        Integer gitLabId = gitFlowRepository.getGitLabId(applicationId);
        List<BranchDO> branches =
                gitFlowRepository.listBranches(gitLabId, path);
        return branches.stream()
                .map(t -> StringUtils.isEmpty(t.getName()) ? null : new GitFlowE(t.getName(), t.getCommit()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public String getReleaseNumber(Long applicationId, String username) {
        return igitflowservice.getReleaseNumber(applicationId, username);
    }

    @Override
    public String getHotfixNumber(Long applicationId, String username) {
        return igitflowservice.getHotfixNumber(applicationId, username);
    }

    @Override
    public void startGitFlow(Long applicationId, String branchName) {
        Integer projectId = gitFlowRepository.getGitLabId(applicationId);
        GitFlowStartPayload gitFlowStartPayload =
                new GitFlowStartPayload(projectId, branchName, GitUserNameUtil.getUsername());
        igitflowservice.createBranch(projectId, branchName);
        gitFlowStartEvent(gitFlowStartPayload);
    }

    @Override
    public void gitFlowStart(GitFlowStartPayload gitFlowStartPayload) {
        Integer projectId = gitFlowStartPayload.getProjectId();
        String branchName = gitFlowStartPayload.getBranchName();
        String username = gitFlowStartPayload.getUsername();
        igitflowservice.createMergeRequest(projectId, branchName, username);
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
        String username = GitUserNameUtil.getUsername();
        // getBranchState 查看具体状态
        Integer branchState = igitflowservice.getBranchState(projectId, branchName, username);
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
                    username
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
        String username = gitFlowFinishPayload.getUsername();

        if (!branchName.startsWith(FEATURE_PREFIX)) {
            igitflowservice.finishBranch(projectId, branchName, false, masterMergeStatus, username);
            if (masterMergeStatus == 0 || masterMergeStatus == 3) {
                igitflowservice.createTag(applicationId, projectId, branchName, username);
            }
        }
        igitflowservice.finishBranch(projectId, branchName, true, devMergeStatus, username);
        igitflowservice.deleteBranchSafely(projectId, branchName, devMergeStatus, masterMergeStatus, username);
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
}
