package io.choerodon.devops.infra.persistence.impl;

<<<<<<< HEAD
<<<<<<< HEAD
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.DevopsProjectE;
import io.choerodon.devops.domain.application.repository.DevopsProjectRepository;
<<<<<<< HEAD
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.devops.infra.dataobject.DevopsProjectDO;
=======
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.dataobject.DevopsProjectDTO;
>>>>>>> [IMP] applicationController重构
import io.choerodon.devops.infra.mapper.DevopsProjectMapper;
import org.springframework.stereotype.Component;
=======

=======
>>>>>>> [IMP]修复后端结构
import io.choerodon.devops.api.vo.iam.entity.DevopsProjectE;
<<<<<<< HEAD
>>>>>>> [IMP] 修改AppControler重构
=======


>>>>>>> [IMP]修复后端结构

/**
 * Created by younger on 2018/3/29.
 */
@Component
public class DevopsProjectRepositoryImpl implements DevopsProjectRepository {
    private DevopsProjectMapper devopsProjectMapper;

    public DevopsProjectRepositoryImpl(DevopsProjectMapper devopsProjectMapper) {
        this.devopsProjectMapper = devopsProjectMapper;
    }

    @Override
    public DevopsProjectE queryDevopsProject(Long projectId) {
        DevopsProjectDTO devopsProjectDO = devopsProjectMapper.selectByPrimaryKey(projectId);
        if (devopsProjectDO == null) {
            throw new CommonException("error.group.not.sync");
        }
        if (devopsProjectDO.getDevopsAppGroupId() == null || devopsProjectDO.getDevopsEnvGroupId() == null) {
            throw new CommonException("error.gitlab.groupId.select");
        }
        return ConvertHelper.convert(devopsProjectDO, DevopsProjectE.class);
    }

    @Override
    public DevopsProjectE queryByGitlabGroupId(Integer gitlabGroupId) {
        return ConvertHelper.convert(devopsProjectMapper.queryByGitlabGroupId(gitlabGroupId), DevopsProjectE.class);
    }

    @Override
    public DevopsProjectE queryByEnvGroupId(Integer envGroupId) {
        DevopsProjectDTO devopsProjectDO = new DevopsProjectDTO();
        devopsProjectDO.setDevopsEnvGroupId(TypeUtil.objToLong(envGroupId));
        return ConvertHelper.convert(devopsProjectMapper.selectOne(devopsProjectDO), DevopsProjectE.class);
    }

    @Override
    public void createProject(DevopsProjectDTO devopsProjectDO) {
        if (devopsProjectMapper.insert(devopsProjectDO) != 1) {
            throw new CommonException("insert project attr error");
        }
    }

    @Override
    public void updateProjectAttr(DevopsProjectDTO devopsProjectDO) {
        DevopsProjectDTO oldDevopsProjectDO = devopsProjectMapper.selectByPrimaryKey(devopsProjectDO.getIamProjectId());
        if (oldDevopsProjectDO == null) {
            devopsProjectMapper.insert(devopsProjectDO);
        } else {
            devopsProjectDO.setObjectVersionNumber(oldDevopsProjectDO.getObjectVersionNumber());
            if (oldDevopsProjectDO.getObjectVersionNumber() == null) {
                devopsProjectMapper.updateObjectVersionNumber(devopsProjectDO.getIamProjectId());
                devopsProjectDO.setObjectVersionNumber(1L);
            }
            devopsProjectMapper.updateByPrimaryKeySelective(devopsProjectDO);
        }
    }
}
