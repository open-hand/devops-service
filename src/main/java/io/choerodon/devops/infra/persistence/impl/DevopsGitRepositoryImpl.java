package io.choerodon.devops.infra.persistence.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.UserAttrE;
import io.choerodon.devops.domain.application.repository.DevopsGitRepository;
import io.choerodon.devops.domain.application.repository.UserAttrRepository;
import io.choerodon.devops.infra.common.util.GitUserNameUtil;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.dataobject.ApplicationDO;
import io.choerodon.devops.infra.feign.GitlabServiceClient;
import io.choerodon.devops.infra.mapper.ApplicationMapper;

/**
 * Creator: Runge
 * Date: 2018/7/2
 * Time: 14:02
 * Description:
 */
@Component
public class DevopsGitRepositoryImpl implements DevopsGitRepository {
    @Autowired
    private GitlabServiceClient gitlabServiceClient;
    @Autowired
    private ApplicationMapper applicationMapper;
    @Autowired
    private UserAttrRepository userAttrRepository;

    @Override
    public void createTag(Integer gitLabProjectId, String tag, String ref, Integer userId) {
        gitlabServiceClient.createTag(gitLabProjectId, tag, ref, userId);
    }

    @Override
    public Integer getGitLabId(Long applicationId) {
        ApplicationDO applicationDO = applicationMapper.selectByPrimaryKey(applicationId);
        if (applicationDO != null) {
            return applicationDO.getGitlabProjectId();
        } else {
            throw new CommonException("error.application.select");
        }
    }

    @Override
    public Integer getGitlabUserId() {
        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        return TypeUtil.objToInteger(userAttrE.getGitlabUserId());
    }
}
