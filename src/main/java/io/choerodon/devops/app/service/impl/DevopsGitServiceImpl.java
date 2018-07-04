package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.BranchDTO;
import io.choerodon.devops.api.dto.DevopsBranchDTO;
import io.choerodon.devops.api.dto.MergeRequestDTO;
import io.choerodon.devops.app.service.DevopsGitService;
import io.choerodon.devops.domain.application.entity.ApplicationE;
import io.choerodon.devops.domain.application.entity.DevopsBranchE;
import io.choerodon.devops.domain.application.entity.ProjectE;
import io.choerodon.devops.domain.application.entity.UserAttrE;
import io.choerodon.devops.domain.application.entity.iam.UserE;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.domain.application.valueobject.Issue;
import io.choerodon.devops.domain.application.valueobject.Organization;
import io.choerodon.devops.domain.application.valueobject.ProjectInfo;
import io.choerodon.devops.infra.common.util.GitUserNameUtil;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.dataobject.gitlab.BranchDO;
import io.choerodon.devops.infra.mapper.ApplicationMapper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Creator: Runge
 * Date: 2018/7/2
 * Time: 14:44
 * Description:
 */
@Component
public class DevopsGitServiceImpl implements DevopsGitService {

    @Value("${services.gitlab.url}")
    private String gitlabUrl;

    @Autowired
    private DevopsGitRepository devopsGitRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private UserAttrRepository userAttrRepository;
    @Autowired
    private IamRepository iamRepository;
    @Autowired
    private AgileRepository agileRepository;
    @Autowired
    private ApplicationMapper applicationMapper;

    public Integer getGitlabUserId() {
        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        return TypeUtil.objToInteger(userAttrE.getGitlabUserId());
    }

    @Override
    public String getUrl(Long projectId, Long appId) {
        ApplicationE applicationE = applicationRepository.query(appId);
        if (applicationE.getGitlabProjectE() != null && applicationE.getGitlabProjectE().getId() != null) {
            ProjectE projectE = iamRepository.queryIamProject(projectId);
            Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
            String urlSlash = gitlabUrl.endsWith("/") ? "" : "/";
            return gitlabUrl + urlSlash
                    + organization.getCode() + "-" + projectE.getCode() + "/"
                    + applicationE.getCode();
        }
        return "";
    }

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
        BranchDO branchDO = devopsGitRepository.createBranch(
                TypeUtil.objToInteger(applicationE.getGitlabProjectE().getId()),
                devopsBranchDTO.getBranchName(),
                devopsBranchDTO.getOriginBranch(),
                getGitlabUserId());
        devopsBranchE.setLastCommitDate(branchDO.getCommit().getCommittedDate());
        devopsBranchE.setCommit(branchDO.getCommit().getShortId());
        devopsBranchE.setUserId(TypeUtil.objToLong(getGitlabUserId()));
        devopsGitRepository.createDevopsBranch(devopsBranchE);
    }

    @Override
    public List<BranchDTO> listBranches(Long projectId, Long applicationId) {
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        ApplicationE applicationE = applicationRepository.query(applicationId);
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        String urlSlash = gitlabUrl.endsWith("/") ? "" : "/";
        String path = String.format("%s%s%s-%s/%s",
                gitlabUrl, urlSlash, organization.getCode(), projectE.getCode(), applicationE.getCode());
        Integer gitLabId = devopsGitRepository.getGitLabId(applicationId);
        List<BranchDO> branches =
                devopsGitRepository.listBranches(gitLabId, path, getGitlabUserId());
        return branches.stream().map(t -> {
            DevopsBranchE devopsBranchE = devopsGitRepository.queryByAppAndBranchName(applicationId, t.getName());
            UserE userE = null;
            ProjectInfo projectInfo = null;
            Issue issue = null;
            if (devopsBranchE != null) {
                if (devopsBranchE.getIssueId() != null) {
                    issue = agileRepository.queryIssue(projectId, devopsBranchE.getIssueId());
                    projectInfo = agileRepository.queryProjectInfo(projectId);

                }
                userE = iamRepository.queryById(devopsBranchE.getUserId());
            }
            UserE commitUserE = iamRepository.queryByLoginName(t.getCommit()
                    .getAuthorName().equals("root") ? "admin" : t.getCommit().getAuthorName());
            return getBranchDTO(t, commitUserE, userE, devopsBranchE, projectInfo, issue);
        }).collect(Collectors.toList());
    }

    @Override
    public DevopsBranchDTO queryBranch(Long projectId, Long applicationId, String branchName) {
        return ConvertHelper.convert(devopsGitRepository
                .queryByAppAndBranchName(applicationId, branchName), DevopsBranchDTO.class);
    }

    @Override
    public void updateBranch(Long projectId, Long applicationId, DevopsBranchDTO devopsBranchDTO) {
        DevopsBranchE devopsBranchE = ConvertHelper.convert(devopsBranchDTO, DevopsBranchE.class);
        devopsBranchE.setUserId(TypeUtil.objToLong(getGitlabUserId()));
        devopsGitRepository.updateBranch(applicationId, devopsBranchE);
    }

    @Override
    public void deleteBranch(Long projectId, Long applicationId, String branchName) {
        Integer gitLabId = devopsGitRepository.getGitLabId(applicationId);
        devopsGitRepository.deleteBranch(gitLabId, branchName, getGitlabUserId());
    }


    private BranchDTO getBranchDTO(BranchDO t, UserE commitUserE, UserE userE,
                                   DevopsBranchE devopsBranchE,
                                   ProjectInfo projectInfo,
                                   Issue issue) {
        String createUserUrl = null;
        String createUserName = null;
        Long issueId = null;
        if (userE != null) {
            createUserName = userE.getLoginName();
            if (userE.getImageUrl() != null) {
                createUserUrl = userE.getImageUrl();
            } else {
                createUserUrl = createUserName;
            }
        }
        if (devopsBranchE != null && devopsBranchE.getIssueId() != null) {
            issueId = devopsBranchE.getIssueId();
        }
        return new BranchDTO(t, devopsBranchE == null ? null : devopsBranchE.getCreationDate(), createUserUrl, issueId, projectInfo == null ? null : projectInfo.getProjectCode() + issue.getIssueNum(), issue == null ? null : issue.getSummary(), commitUserE.getImageUrl() == null ? commitUserE.getLoginName() : commitUserE.getImageUrl(), issue == null ? null : issue.getTypeCode(), commitUserE.getLoginName(), createUserName);
    }

    @Override
    public Page<MergeRequestDTO> getMergeRequestList(Long projectId, Long applicationId, String state, PageRequest pageRequest) {
        applicationRepository.checkApp(projectId, applicationId);
        Integer gitLabProjectId = devopsGitRepository.getGitLabId(applicationId);
        if (gitLabProjectId == null) {
            throw new CommonException("error.gitlabProjectId.not.exists");
        }
        return devopsGitRepository.getMergeRequestList(gitLabProjectId, state, pageRequest);
    }
}
