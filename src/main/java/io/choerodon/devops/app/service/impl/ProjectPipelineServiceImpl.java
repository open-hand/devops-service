package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.ProjectPipelineService;
import io.choerodon.devops.domain.application.entity.UserAttrE;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabJobE;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.infra.common.util.GitUserNameUtil;
import io.choerodon.devops.infra.common.util.TypeUtil;

/**
 * Created by Zenger on 2018/4/10.
 */
@Service
public class ProjectPipelineServiceImpl implements ProjectPipelineService {
    private static final String SONAR_QUBE = "sonarqube";
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
    private DevopsGitRepository devopsGitRepository;


    public Integer getGitlabUserId() {
        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        return TypeUtil.objToInteger(userAttrE.getGitlabUserId());
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
        stages.stream().forEach(s -> {

            List<GitlabJobE> gitlabJobEList = jobs.stream()
                    .filter(gitlabJobE -> gitlabJobE.getStage().equals(s)).collect(Collectors.toList());
            if (!s.equals(SONAR_QUBE)) {
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
            } else {
                GitlabJobE gitlabJobE = new GitlabJobE();
                gitlabJobE.setStage(gitlabJobEList.get(0).getStage());
                gitlabJobE.setName(gitlabJobEList.get(0).getName());
                gitlabJobE.setStatus(gitlabJobEList.get(0).getStatus());
                gitlabJobE.setDescription(gitlabJobEList.get(0).getDescription());
                result.add(gitlabJobE);

            }
        });
        return result;
    }
}
