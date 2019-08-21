package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Map;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.ProjectReqVO;
import io.choerodon.devops.app.eventhandler.payload.ProjectPayload;
import io.choerodon.devops.app.service.DevopsProjectService;
import io.choerodon.devops.infra.dto.DevopsProjectDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.DevopsProjectMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.TypeUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Sheep on 2019/7/15.
 */
@Service
public class DevopsProjectServiceImpl implements DevopsProjectService {
    @Autowired
    private DevopsProjectMapper devopsProjectMapper;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;

    @Override
    public boolean queryProjectGitlabGroupReady(Long projectId) {
        DevopsProjectDTO devopsProjectDTO = new DevopsProjectDTO();
        devopsProjectDTO.setIamProjectId(projectId);
        devopsProjectDTO = devopsProjectMapper.selectOne(devopsProjectDTO);
        if (devopsProjectDTO == null) {
            throw new CommonException("error.group.not.sync");
        }
        if (devopsProjectDTO.getDevopsAppGroupId() == null || devopsProjectDTO.getDevopsEnvGroupId() == null) {
            throw new CommonException("error.gitlab.groupId.select");
        }
        return devopsProjectDTO.getDevopsAppGroupId() != null;
    }

    @Override
    public DevopsProjectDTO baseQueryByProjectId(Long projectId) {
        DevopsProjectDTO devopsProjectDTO = new DevopsProjectDTO();
        devopsProjectDTO.setIamProjectId(projectId);
        devopsProjectDTO = devopsProjectMapper.selectOne(devopsProjectDTO);
        if (devopsProjectDTO == null) {
            throw new CommonException("error.group.not.sync");
        }
        if (devopsProjectDTO.getDevopsAppGroupId() == null || devopsProjectDTO.getDevopsEnvGroupId() == null) {
            throw new CommonException("error.gitlab.groupId.select");
        }
        return devopsProjectDTO;
    }

    @Override
    public DevopsProjectDTO queryByAppId(Long appId) {
        return devopsProjectMapper.selectByPrimaryKey(appId);
    }

    @Override
    public void createProject(ProjectPayload projectPayload) {
        // create project in db
        DevopsProjectDTO devopsProjectDTO = new DevopsProjectDTO(projectPayload.getProjectId());
        if (devopsProjectMapper.insert(devopsProjectDTO) != 1) {
            throw new CommonException("insert project attr error");
        }
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

    @Override
    public void baseUpdate(DevopsProjectDTO devopsProjectDTO) {
        DevopsProjectDTO oldDevopsProjectDTO = devopsProjectMapper.selectByPrimaryKey(devopsProjectDTO);
        if (oldDevopsProjectDTO == null) {
            devopsProjectMapper.insertSelective(devopsProjectDTO);
        } else {
            devopsProjectDTO.setObjectVersionNumber(oldDevopsProjectDTO.getObjectVersionNumber());
            devopsProjectMapper.updateByPrimaryKeySelective(devopsProjectDTO);
        }
    }

    @Override
    public Long queryAppIdByProjectId(Long projectId) {
        return devopsProjectMapper.queryAppIdByProjectId(projectId);
    }

    @Override
    public Long queryProjectIdByAppId(Long appId) {
        DevopsProjectDTO devopsProjectDTO = new DevopsProjectDTO();
        devopsProjectDTO.setAppId(appId);
        return devopsProjectMapper.selectOne(devopsProjectDTO).getIamProjectId();
    }

    @Override
    public PageInfo<ProjectReqVO> pageProjects(Long projectId, PageRequest pageRequest, String searchParams) {
        Map<String, Object> searchMap = TypeUtil.castMapParams(searchParams);
        List<String> paramList = TypeUtil.cast(searchMap.get(TypeUtil.PARAMS));

        ProjectDTO iamProjectDTO = baseServiceClientOperator.queryIamProjectById(projectId);

        PageInfo<ProjectDTO> projectDTOPageInfo = baseServiceClientOperator.pageProjectByOrgId(
                iamProjectDTO.getOrganizationId(),
                pageRequest.getPage(), pageRequest.getSize(), null,
                paramList == null ? null : paramList.toArray(new String[0]));
        return ConvertUtils.convertPage(projectDTOPageInfo, ProjectReqVO.class);
    }
}
