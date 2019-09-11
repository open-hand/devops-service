package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.ProjectReqVO;
import io.choerodon.devops.api.vo.iam.UserVO;
import io.choerodon.devops.app.eventhandler.payload.ProjectPayload;
import io.choerodon.devops.app.service.DevopsProjectService;
import io.choerodon.devops.infra.dto.DevopsProjectDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.DevopsProjectMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.TypeUtil;

/**
 * Created by Sheep on 2019/7/15.
 */
@Service
public class DevopsProjectServiceImpl implements DevopsProjectService {
    private Logger logger = LoggerFactory.getLogger(DevopsProjectServiceImpl.class);

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

    /**
     * 插入或更新项目相关信息
     *
     * @param devopsProjectDTO 项目相关信息
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Override
    public void baseUpdate(DevopsProjectDTO devopsProjectDTO) {
        // 查询纪录是否存在
        DevopsProjectDTO oldDevopsProjectDTO = devopsProjectMapper.selectByPrimaryKey(devopsProjectDTO);
        if (oldDevopsProjectDTO == null) {
            try {
                devopsProjectMapper.insertSelective(devopsProjectDTO);
            } catch (Exception e) {
                logger.info("An exception occurred when inserting into devops_project: {}", JSONObject.toJSONString(devopsProjectDTO));
                logger.info("The exception is: ", e);
                // 如果插入纪录失败则说明在查询纪录为null之后有别的线程插入了数据
                // 此时对此数据再进行一次更新操作
                oldDevopsProjectDTO = devopsProjectMapper.selectByPrimaryKey(devopsProjectDTO);
                devopsProjectDTO.setObjectVersionNumber(oldDevopsProjectDTO.getObjectVersionNumber());
                devopsProjectMapper.updateByPrimaryKeySelective(devopsProjectDTO);
            }
        } else {
            devopsProjectDTO.setObjectVersionNumber(oldDevopsProjectDTO.getObjectVersionNumber());
            devopsProjectMapper.updateByPrimaryKeySelective(devopsProjectDTO);
        }
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

    @Override
    public List<UserVO> listAllOwnerAndMembers(Long projectId) {
        return ConvertUtils.convertList(baseServiceClientOperator.getAllMember(projectId), UserVO.class);
    }
}
