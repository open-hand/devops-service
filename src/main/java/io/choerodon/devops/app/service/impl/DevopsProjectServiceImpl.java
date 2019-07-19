package io.choerodon.devops.app.service.impl;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.iam.entity.DevopsProjectVO;
import io.choerodon.devops.app.service.DevopsProjectService;
import io.choerodon.devops.infra.dto.DevopsProjectDTO;
import io.choerodon.devops.infra.mapper.DevopsProjectMapper;
import io.choerodon.devops.infra.util.TypeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Sheep on 2019/7/15.
 */
@Service
public class DevopsProjectServiceImpl implements DevopsProjectService {


    @Autowired
    DevopsProjectMapper devopsProjectMapper;

    @Override
    public DevopsProjectDTO baseQueryByProjectId(Long projectId) {
        DevopsProjectDTO devopsProjectDTO = devopsProjectMapper.selectByPrimaryKey(projectId);
        if (devopsProjectDTO == null) {
            throw new CommonException("error.group.not.sync");
        }
        if (devopsProjectDTO.getDevopsAppGroupId() == null || devopsProjectDTO.getDevopsEnvGroupId() == null) {
            throw new CommonException("error.gitlab.groupId.select");
        }
        return devopsProjectDTO;
    }

    @Override
    public DevopsProjectDTO baseQueryByGitlabAppGroupId(Integer appGroupId) {
        DevopsProjectDTO devopsProjectDTO = new DevopsProjectDTO();
        devopsProjectDTO.setDevopsAppGroupId(TypeUtil.objToLong(appGroupId));
        return devopsProjectMapper.selectOne(devopsProjectDTO);
    }

    @Override
    public DevopsProjectDTO baseQueryByGitlabEnvGroupId(Integer envGroupId) {
        DevopsProjectDTO devopsProjectDTO = new DevopsProjectDTO();
        devopsProjectDTO.setDevopsEnvGroupId(TypeUtil.objToLong(envGroupId));
        return devopsProjectMapper.selectOne(devopsProjectDTO);
    }

    public void baseCreate(DevopsProjectDTO devopsProjectDTO) {
        if (devopsProjectMapper.insert(devopsProjectDTO) != 1) {
            throw new CommonException("insert project attr error");
        }
    }

    public void baseUpdate(DevopsProjectDTO devopsProjectDTO) {
        DevopsProjectDTO oldDevopsProjectDTO = devopsProjectMapper.selectByPrimaryKey(devopsProjectDTO.getIamProjectId());
        if (oldDevopsProjectDTO == null) {
            devopsProjectMapper.insert(devopsProjectDTO);
        } else {
            devopsProjectDTO.setObjectVersionNumber(oldDevopsProjectDTO.getObjectVersionNumber());
            devopsProjectMapper.updateByPrimaryKeySelective(devopsProjectDTO);
        }
    }

}
