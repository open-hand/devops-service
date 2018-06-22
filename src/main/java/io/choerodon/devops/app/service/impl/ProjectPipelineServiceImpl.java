package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.ProjectPipelineResultTotalDTO;
import io.choerodon.devops.app.service.ProjectPipelineService;
import io.choerodon.devops.domain.application.entity.ApplicationE;
import io.choerodon.devops.domain.application.entity.ProjectE;
import io.choerodon.devops.domain.application.entity.UserAttrE;
import io.choerodon.devops.domain.application.entity.gitlab.BranchE;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabJobE;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabPipelineE;
import io.choerodon.devops.domain.application.entity.iam.UserE;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.domain.application.valueobject.Organization;
import io.choerodon.devops.domain.application.valueobject.PipelineResultV;
import io.choerodon.devops.domain.application.valueobject.ProjectPipelineResultTotalV;
import io.choerodon.devops.infra.common.util.GitUserNameUtil;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.dataobject.gitlab.TagDO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by Zenger on 2018/4/10.
 */
@Service
public class ProjectPipelineServiceImpl implements ProjectPipelineService {
    @Value("${services.gitlab.url}")
    private String gitlabUrl;

    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private IamRepository iamRepository;
    @Autowired
    private GitlabProjectRepository gitlabProjectRepository;
    @Autowired
    private UserAttrRepository userAttrRepository;
    @Autowired
    private GitFlowRepository gitFlowRepository;


    public Integer getGitlabUserId() {
        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        return TypeUtil.objToInteger(userAttrE.getGitlabUserId());
    }

    @Override
    public ProjectPipelineResultTotalDTO listPipelines(Long projectId, Long appId, PageRequest pageRequest) {
        ProjectPipelineResultTotalV projectPipelineResultTotalV = new ProjectPipelineResultTotalV();
        ApplicationE app = applicationRepository.query(appId);
        if (app == null) {
            throw new CommonException("error.application.query");
        }
        Integer userId = getGitlabUserId();
        String userName = GitUserNameUtil.getUsername();
        Integer gitlabProjectId = app.getGitlabProjectE().getId();
        List<BranchE> branchES = gitlabProjectRepository.listBranches(
                gitlabProjectId, getGitlabUserId());
        List<TagDO> tagTotalList = gitFlowRepository.getGitLabTags(gitlabProjectId, userId);
        if (branchES == null) {
            return new ProjectPipelineResultTotalDTO();
        }
        int page = pageRequest.getPage();
        int size = pageRequest.getSize();
        List<GitlabPipelineE> gitlabPipelineEList =
                gitlabProjectRepository.listPipeline(gitlabProjectId, getGitlabUserId());
        List<GitlabPipelineE> gitlabPipelineEListByPage = gitlabProjectRepository.listPipelines(
                gitlabProjectId, page + 1, size, getGitlabUserId());
        List<String> branchNames = branchES.stream().map(BranchE::getName).collect(Collectors.toList());
        List<String> tagNames = tagTotalList.stream().map(TagDO::getName).collect(Collectors.toList());
        branchNames.addAll(tagNames);
        List<PipelineResultV> pipelineResultVS = new ArrayList<>();
        if (gitlabPipelineEListByPage != null && !gitlabPipelineEListByPage.isEmpty()) {
            listPipelineResultV(
                    gitlabPipelineEListByPage,
                    pipelineResultVS,
                    app,
                    gitlabProjectId,
                    userId,
                    projectId,
                    userName);
            branchNames.forEach(branchName -> {
                final long[] id = {0};
                gitlabPipelineEList.parallelStream()
                        .filter(p -> branchName.contains(p.getRef()) && p.getId() > id[0])
                        .forEach(r -> id[0] = r.getId());
                pipelineResultVS.parallelStream().filter(p -> p.getId() == id[0]).forEach(r -> r.setLatest(true));
            });
            Collections.sort(pipelineResultVS);
            int allIndex = gitlabPipelineEList.size();
            int totalPages = 0;
            if (size != 0) {
                totalPages = allIndex % size == 0 ? allIndex / size : allIndex / size + 1;
            }
            projectPipelineResultTotalV.setTotalElements(allIndex);
            projectPipelineResultTotalV.setTotalPages(totalPages);
            projectPipelineResultTotalV.setNumberOfElements(gitlabPipelineEListByPage.size());
            projectPipelineResultTotalV.setNumber(page);
            projectPipelineResultTotalV.setSize(size);
            projectPipelineResultTotalV.setContent(pipelineResultVS);
        }
        return ConvertHelper.convert(projectPipelineResultTotalV,
                ProjectPipelineResultTotalDTO.class);
    }

    @Override
    public Boolean retry(Long gitlabProjectId, Long pipelineId) {
        return gitlabProjectRepository.retry(gitlabProjectId.intValue(),
                pipelineId.intValue(), getGitlabUserId());
    }

    @Override
    public Boolean cancel(Long gitlabProjectId, Long pipelineId) {
        return gitlabProjectRepository.cancel(gitlabProjectId.intValue(),
                pipelineId.intValue(), getGitlabUserId());
    }


    private void listPipelineResultV(List<GitlabPipelineE> gitlabPipelineEListByPage,
                                     List<PipelineResultV> pipelineResultVS,
                                     ApplicationE applicationE,
                                     Integer gitlabProjectId,
                                     Integer userId,
                                     Long projectId,
                                     String userName) {

        gitlabPipelineEListByPage.parallelStream().forEach(gitlabPipeline -> {
            GitlabPipelineE gitlabPipelineE =
                    gitlabProjectRepository.getPipeline(gitlabProjectId, gitlabPipeline.getId(), userId);
            PipelineResultV pipelineResultV = new PipelineResultV();
            pipelineResultV.setGitlabProjectId(gitlabProjectId.longValue());
            pipelineResultV.setAppCode(applicationE.getCode());
            pipelineResultV.setAppName(applicationE.getName());
            pipelineResultV.setAppStatus(applicationE.getActive());
            pipelineResultV.setLatest(false);
            pipelineResultV.setId(gitlabPipelineE.getId().longValue());
            pipelineResultV.setStatus(gitlabPipelineE.getStatus().toString());
            pipelineResultV.setCreateUser(gitlabPipelineE.getUser().getUsername());
            pipelineResultV.setRef(gitlabPipelineE.getRef());
            pipelineResultV.setSha(gitlabPipelineE.getSha());
            pipelineResultV.setCreatedAt(gitlabPipelineE.getCreatedAt());

            List<GitlabJobE> jobs =
                    gitlabProjectRepository.listJobs(gitlabProjectId, gitlabPipelineE.getId(), userId);
            if (jobs != null) {
                List<GitlabJobE> realJobs = getRealJobs(jobs);
                pipelineResultV.setJobs(realJobs);
                Long diffs = 0L;
                for (Long diff : realJobs.stream().map(GitlabJobE::getJobTime).collect(Collectors.toList())) {
                    diffs = diff + diffs;
                }
                pipelineResultV.setTime(getStageTime(diffs));
            }

            UserE userE = iamRepository.queryByLoginName(userName);
            if (userE != null) {
                pipelineResultV.setImageUrl(userE.getImageUrl());
            }

            ProjectE projectE = iamRepository.queryIamProject(projectId);
            Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
            pipelineResultV.setGitlabUrl(gitlabUrl + "/"
                    + organization.getCode() + "-" + projectE.getCode() + "/"
                    + applicationE.getCode() + ".git");
            pipelineResultVS.add(pipelineResultV);
        });


    }


    /**
     * 获取CI执行时间间隔
     *
     * @param diff 时间戳
     * @return Long[]
     */
    public Long[] getStageTime(Long diff) {
        long day = 0;
        long hour = 0;
        long min = 0;
        long sec = 0;
        day = diff / (24 * 60 * 60 * 1000);
        hour = (diff / (60 * 60 * 1000) - day * 24);
        min = ((diff / (60 * 1000)) - day * 24 * 60 - hour * 60);
        sec = (diff / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
        return new Long[]{day, hour, min, sec};
    }

    /**
     * job正序
     *
     * @param jobs jobs
     * @return
     */
    public List<GitlabJobE> getRealJobs(List<GitlabJobE> jobs) {
        List<GitlabJobE> result = new ArrayList<>();
        List<String> stages = new ArrayList<>();
        for (GitlabJobE gitlabJobE : jobs) {
            if (!stages.contains(gitlabJobE.getStage())) {
                stages.add(gitlabJobE.getStage());
            }
        }
        for (String string : stages) {
            List<GitlabJobE> gitlabJobEList = new ArrayList<>();
            for (GitlabJobE jobs1 : jobs) {
                if (string.equals(jobs1.getStage())) {
                    gitlabJobEList.add(jobs1);
                }
            }
            Date max = gitlabJobEList.get(0).getCreatedAt();
            int index = 0;
            for (int i = 0; i < gitlabJobEList.size(); i++) {
                if (gitlabJobEList.get(i).getCreatedAt().after(max)) {
                    index = i;
                }
            }
            GitlabJobE gitlabJobE = new GitlabJobE();
            gitlabJobE.setId(gitlabJobEList.get(index).getId());
            gitlabJobE.setName(gitlabJobEList.get(index).getName());
            gitlabJobE.setStage(gitlabJobEList.get(index).getStage());
            gitlabJobE.setStatus(gitlabJobEList.get(index).getStatus());
            gitlabJobE.setStartedAt(gitlabJobEList.get(index).getStartedAt());
            gitlabJobE.setFinishedAt(gitlabJobEList.get(index).getFinishedAt());
            result.add(gitlabJobE);
        }
        return result;
    }

}
