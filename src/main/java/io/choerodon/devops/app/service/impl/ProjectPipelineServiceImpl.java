package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.ProjectPipelineService;
import io.choerodon.devops.api.vo.iam.entity.UserAttrE;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.infra.util.GitUserNameUtil;
import io.choerodon.devops.infra.util.TypeUtil;

/**
 * Created by Zenger on 2018/4/10.
 */
@Service
public class ProjectPipelineServiceImpl implements ProjectPipelineService {
    @Value("${services.gitlab.url}")
    private String gitlabUrl;
    @Autowired
    private GitlabProjectRepository gitlabProjectRepository;
    @Autowired
    private UserAttrRepository userAttrRepository;


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
}
