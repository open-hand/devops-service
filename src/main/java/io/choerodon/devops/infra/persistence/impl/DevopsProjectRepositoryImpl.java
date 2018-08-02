package io.choerodon.devops.infra.persistence.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabGroupE;
import io.choerodon.devops.domain.application.repository.DevopsProjectRepository;
import io.choerodon.devops.infra.dataobject.DevopsProjectDO;
import io.choerodon.devops.infra.mapper.DevopsProjectMapper;

/**
 * Created by younger on 2018/3/29.
 */
@Component
public class DevopsProjectRepositoryImpl implements DevopsProjectRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsProjectRepositoryImpl.class);

    private DevopsProjectMapper devopsProjectMapper;

    public DevopsProjectRepositoryImpl(DevopsProjectMapper devopsProjectMapper) {
        this.devopsProjectMapper = devopsProjectMapper;
    }

    @Override
    public GitlabGroupE queryDevopsProject(Long projectId) {
        DevopsProjectDO devopsProjectDO = devopsProjectMapper.selectByPrimaryKey(projectId);
        if (devopsProjectDO.getGitlabGroupId() == null) {
            throw new CommonException("error.gitlab.groupId.select");
        }
        return ConvertHelper.convert(devopsProjectDO, GitlabGroupE.class);
    }

    @Override
    public GitlabGroupE queryByGitlabGroupId(Integer gitlabGroupId) {
        return ConvertHelper.convert(devopsProjectMapper.queryByGitlabGroupId(gitlabGroupId), GitlabGroupE.class);
    }

    @Override
    public Boolean checkGroupExist(String uuid) {
        DevopsProjectDO devopsProjectDO = new DevopsProjectDO();
        devopsProjectDO.setGitlabUuid(uuid);
        return devopsProjectMapper.selectCount(devopsProjectDO) > 0;
    }

    @Override
    public Boolean checkHarborExist(String uuid) {
        DevopsProjectDO devopsProjectDO = new DevopsProjectDO();
        devopsProjectDO.setHarborUuid(uuid);
        return devopsProjectMapper.selectCount(devopsProjectDO) > 0;
    }

    @Override
    public Boolean checkMemberExist(String uuid) {
        DevopsProjectDO devopsProjectDO = new DevopsProjectDO();
        devopsProjectDO.setMemberUuid(uuid);
        return devopsProjectMapper.selectCount(devopsProjectDO) > 0;
    }

    @Override
    public GitlabGroupE queryByEnvGroupId(Integer envGroupId) {
        DevopsProjectDO devopsProjectDO = new DevopsProjectDO();
        devopsProjectDO.setEnvGroupId(envGroupId);
        return ConvertHelper.convert(devopsProjectMapper.selectOne(devopsProjectDO), GitlabGroupE.class);
    }

    @Override
    public void createProject(DevopsProjectDO devopsProjectDO) {
        if (devopsProjectMapper.insert(devopsProjectDO) != 1) {
            LOGGER.error("insert project attr error");
        }
    }

    @Override
    public void updateProjectAttr(DevopsProjectDO devopsProjectDO) {
        devopsProjectMapper.updateByPrimaryKeySelective(devopsProjectDO);
    }
}
