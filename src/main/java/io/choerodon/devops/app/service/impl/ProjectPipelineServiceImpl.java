package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.ProjectPipelineResultTotalDTO;
import io.choerodon.devops.app.service.ProjectPipelineService;
import io.choerodon.devops.domain.application.entity.ApplicationE;
import io.choerodon.devops.domain.application.entity.ProjectE;
import io.choerodon.devops.domain.application.entity.gitlab.BranchE;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabJobE;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabPipelineE;
import io.choerodon.devops.domain.application.entity.iam.UserE;
import io.choerodon.devops.domain.application.repository.ApplicationRepository;
import io.choerodon.devops.domain.application.repository.GitlabProjectRepository;
import io.choerodon.devops.domain.application.repository.IamRepository;
import io.choerodon.devops.domain.application.valueobject.Organization;
import io.choerodon.devops.domain.application.valueobject.PipelineResultV;
import io.choerodon.devops.domain.application.valueobject.ProjectPipelineResultTotalV;
import io.choerodon.devops.infra.common.util.GitUserNameUtil;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by Zenger on 2018/4/10.
 */
@Service
public class ProjectPipelineServiceImpl implements ProjectPipelineService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectPipelineServiceImpl.class);
    private ApplicationRepository applicationRepository;
    private IamRepository iamRepository;
    private GitlabProjectRepository gitlabProjectRepository;
    @Value("${services.gitlab.url}")
    private String gitlabUrl;

    /**
     * 构造函数
     */
    public ProjectPipelineServiceImpl(GitlabProjectRepository gitlabProjectRepository,
                                      ApplicationRepository applicationRepository,
                                      IamRepository iamRepository) {
        this.gitlabProjectRepository = gitlabProjectRepository;
        this.applicationRepository = applicationRepository;
        this.iamRepository = iamRepository;
    }

    @Override
    public ProjectPipelineResultTotalDTO listPipelines(Long projectId, Long appId, PageRequest pageRequest) {
        ApplicationE app = applicationRepository.query(appId);
        if (app == null) {
            throw new CommonException("error.application.query");
        }

        String username = GitUserNameUtil.getUsername();
        String realname = GitUserNameUtil.getRealUsername();
        Integer gitlabProjectId = app.getGitlabProjectE().getId();
        int page = pageRequest.getPage();
        int size = pageRequest.getSize();
        List<GitlabPipelineE> gitlabPipelineEList = gitlabProjectRepository.listPipeline(gitlabProjectId);
        List<GitlabPipelineE> gitlabPipelineEListByPage = gitlabProjectRepository.listPipelines(
                gitlabProjectId, page + 1, size);

        List<String> branchNames = gitlabProjectRepository.listBranches(
                gitlabProjectId).stream().map(BranchE::getName).collect(Collectors.toList());

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        List<PipelineResultV> pipelineResultVS = new ArrayList<>();
        ProjectPipelineResultTotalV projectPipelineResultTotalV = new ProjectPipelineResultTotalV();
        if (gitlabPipelineEListByPage != null && !gitlabPipelineEListByPage.isEmpty()) {
            for (GitlabPipelineE gitlabPipeline : gitlabPipelineEListByPage) {
                Future<PipelineResultV> future = executorService.submit(new TaskWithResult(
                        gitlabProjectId, app, username, gitlabPipeline.getId(), projectId, realname));

                try {
                    PipelineResultV pipelineResultV = future.get();
                    if (pipelineResultV != null) {
                        pipelineResultVS.add(pipelineResultV);
                    }
                } catch (ExecutionException | InterruptedException e) {
                    logger.error(e.getMessage());
                }
            }
            executorService.shutdown();

            branchNames.stream().forEach(b -> {
                final long[] id = {0};
                gitlabPipelineEList.parallelStream().forEach(p -> {
                    if (b.contains(p.getRef()) && p.getId() > id[0]) {
                        id[0] = p.getId();
                    }
                });

                for (PipelineResultV p : pipelineResultVS) {
                    if (p.getId() == id[0]) {
                        p.setLatest(true);
                    }
                }
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
                pipelineId.intValue(), GitUserNameUtil.getUsername());
    }

    @Override
    public Boolean cancel(Long gitlabProjectId, Long pipelineId) {
        return gitlabProjectRepository.cancel(gitlabProjectId.intValue(),
                pipelineId.intValue(), GitUserNameUtil.getUsername());
    }

    /**
     * 获取CI执行时间间隔
     *
     * @param startTime 起始时间
     * @param finshTime 结束时间
     * @return Long[]
     */
    public Long[] getStageTime(Date startTime, Date finshTime) {
        long day = 0;
        long hour = 0;
        long min = 0;
        long sec = 0;
        long time1 = startTime.getTime();
        long time2 = finshTime.getTime();
        long diff;
        if (time1 < time2) {
            diff = time2 - time1;
        } else {
            diff = time1 - time2;
        }
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
            if (stages.contains(gitlabJobE.getStage())) {
                continue;
            } else {
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
            result.add(gitlabJobE);
        }
        return result;
    }

    public class TaskWithResult implements Callable<PipelineResultV> {

        private Integer gitlabProjectId;
        private ApplicationE app;
        private String username;
        private Integer gitlabPipeline;
        private Long projectId;
        private String realname;

        /**
         * 构造函数
         */
        public TaskWithResult(Integer gitlabProjectId, ApplicationE app, String username,
                              Integer gitlabPipeline, Long projectId, String realname) {
            this.gitlabProjectId = gitlabProjectId;
            this.app = app;
            this.username = username;
            this.gitlabPipeline = gitlabPipeline;
            this.projectId = projectId;
            this.realname = realname;
        }

        @Override
        public PipelineResultV call() throws Exception {
            PipelineResultV pipelineResultV = new PipelineResultV();
            pipelineResultV.setGitlabProjectId(gitlabProjectId.longValue());
            pipelineResultV.setAppCode(app.getCode());
            pipelineResultV.setAppName(app.getName());
            pipelineResultV.setAppStatus(app.getActive());
            pipelineResultV.setLatest(false);

            GitlabPipelineE gitlabPipelineE = gitlabProjectRepository.getPipeline(
                    gitlabProjectId, gitlabPipeline, username);
            if (gitlabPipelineE != null) {
                pipelineResultV.setId(gitlabPipelineE.getId().longValue());
                pipelineResultV.setStatus(gitlabPipelineE.getStatus().toString());
                pipelineResultV.setCreateUser(gitlabPipelineE.getUser().getUsername());
                pipelineResultV.setRef(gitlabPipelineE.getRef());
                pipelineResultV.setSha(gitlabPipelineE.getSha());
                if (gitlabPipelineE.getStarted_at() != null && gitlabPipelineE.getFinished_at() != null) {
                    pipelineResultV.setTime(getStageTime(gitlabPipelineE.getStarted_at(),
                            gitlabPipelineE.getFinished_at()));
                }
                pipelineResultV.setCreatedAt(gitlabPipelineE.getCreatedAt());

                List<GitlabJobE> jobs =
                        gitlabProjectRepository.listJobs(gitlabProjectId, gitlabPipelineE.getId(), username);
                if (jobs != null) {
                    List<GitlabJobE> realJobs = getRealJobs(jobs);
                    pipelineResultV.setJobs(realJobs);
                }

                UserE userE = iamRepository.queryByLoginName(realname);
                if (userE != null) {
                    pipelineResultV.setImageUrl(userE.getImageUrl());
                }

                ProjectE projectE = iamRepository.queryIamProject(projectId);
                Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
                pipelineResultV.setGitlabUrl(gitlabUrl + "/"
                        + organization.getCode() + "-" + projectE.getCode() + "/"
                        + app.getCode() + ".git");
            }
            return pipelineResultV;
        }
    }
}
