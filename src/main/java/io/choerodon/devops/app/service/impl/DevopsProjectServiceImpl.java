package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.ProjectReqVO;
import io.choerodon.devops.api.vo.iam.UserVO;
import io.choerodon.devops.app.eventhandler.payload.ProjectPayload;
import io.choerodon.devops.app.service.DevopsProjectService;
import io.choerodon.devops.infra.dto.DevopsProjectDTO;
import io.choerodon.devops.infra.dto.GitlabProjectSimple;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.DevopsProjectMapper;
import io.choerodon.devops.infra.util.*;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

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
        DevopsProjectDTO devopsProjectDTO = queryWithoutCheck(projectId);
        if (devopsProjectDTO == null) {
            throw new CommonException("error.group.not.sync");
        }
        if (devopsProjectDTO.getDevopsAppGroupId() == null || devopsProjectDTO.getDevopsEnvGroupId() == null) {
            throw new CommonException("error.gitlab.groupId.select");
        }
        return devopsProjectDTO;
    }

    @Nullable
    @Override
    public DevopsProjectDTO queryWithoutCheck(Long projectId) {
        DevopsProjectDTO devopsProjectDTO = new DevopsProjectDTO();
        devopsProjectDTO.setIamProjectId(Objects.requireNonNull(projectId));
        return devopsProjectMapper.selectOne(devopsProjectDTO);
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
    @Override
    public void baseUpdate(DevopsProjectDTO devopsProjectDTO) {
        // 查询纪录是否存在
        DevopsProjectDTO oldDevopsProjectDTO = devopsProjectMapper.selectByPrimaryKey(devopsProjectDTO);
        if (oldDevopsProjectDTO == null) {
            try {
                MapperUtil.resultJudgedInsertSelective(devopsProjectMapper, devopsProjectDTO, "error.project.insert", (Object[]) null);
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
            MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsProjectMapper, devopsProjectDTO, "error.project.update", (Object[]) null);
        }
    }

    @Override
    public void baseUpdateByPrimaryKey(DevopsProjectDTO devopsProjectDTO) {
        DevopsProjectDTO oldDevopsProjectDTO = devopsProjectMapper.selectByPrimaryKey(devopsProjectDTO);
        if (oldDevopsProjectDTO == null) {
            MapperUtil.resultJudgedInsertSelective(devopsProjectMapper, devopsProjectDTO, "error.project.insert", (Object[]) null);
        } else {
            devopsProjectDTO.setObjectVersionNumber(oldDevopsProjectDTO.getObjectVersionNumber());
            MapperUtil.resultJudgedUpdateByPrimaryKey(devopsProjectMapper, devopsProjectDTO, "error.project.update", (Object[]) null);
        }
    }

    @Override
    public Page<ProjectReqVO> pageProjects(Long projectId, PageRequest pageable, String searchParams) {
        ProjectDTO iamProjectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
        return pageProjectsByOrganizationId(iamProjectDTO.getOrganizationId(), pageable, searchParams);
    }

    @Override
    public Page<ProjectReqVO> pageProjectsByOrganizationId(Long organizationId, PageRequest pageable, String searchParams) {
        Map<String, Object> searchMap = TypeUtil.castMapParams(searchParams);
        Map<String, Object> searchParamsMap = TypeUtil.cast(searchMap.get(TypeUtil.SEARCH_PARAM));
        String name = null;
        String code = null;
        if (!CollectionUtils.isEmpty(searchParamsMap)) {
            name = TypeUtil.cast(searchParamsMap.get("name"));
            code = TypeUtil.cast(searchParamsMap.get("code"));
        }
        List<String> paramList = TypeUtil.cast(searchMap.get(TypeUtil.PARAMS));


        Page<ProjectDTO> projectDTOPageInfo = baseServiceClientOperator.pageProjectByOrgId(
                Objects.requireNonNull(organizationId),
                pageable.getPage(), pageable.getSize(), pageable.getSort(), name, code,
                CollectionUtils.isEmpty(paramList) ? null : paramList.get(0));
        return ConvertUtils.convertPage(projectDTOPageInfo, ProjectReqVO.class);
    }

    @Override
    public Page<UserVO> listAllOwnerAndMembers(Long projectId, PageRequest pageable, String params) {
        List<IamUserDTO> allMember = baseServiceClientOperator.getAllMember(projectId, params);
        List<Long> selectedIamUserIds = new ArrayList<>();
        if (!StringUtils.isEmpty(params)) {
            Map maps = JSONObject.parseObject(params, Map.class);
            selectedIamUserIds = KeyDecryptHelper.decryptIdList((JSONArray) maps.get("ids"));
        }
        if (!CollectionUtils.isEmpty(selectedIamUserIds)) {
            List<IamUserDTO> iamUserDTOList = baseServiceClientOperator.queryUsersByUserIds(selectedIamUserIds);
            if (!CollectionUtils.isEmpty(iamUserDTOList)) {
                iamUserDTOList.forEach(iamUserDTO -> {
                    if (!allMember.isEmpty()) {
                        allMember.remove(iamUserDTO);
                        allMember.add(0, iamUserDTO);
                    } else {
                        allMember.add(iamUserDTO);
                    }
                });
            }
        }

        return PageInfoUtil.createPageFromList(allMember.stream().map(this::userDTOTOVO).collect(Collectors.toList()), pageable);
    }

    @Override
    public List<DevopsProjectDTO> listAll() {
        return devopsProjectMapper.selectAll();
    }

    @Override
    public List<GitlabProjectSimple> queryGitlabGroups(List<Long> projectIds) {
       return devopsProjectMapper.selectByProjectIds(projectIds);
    }

    @Override
    public List<DevopsProjectDTO> listExistGroup(Set<Integer> groupIds) {
        return devopsProjectMapper.listExistGroup(groupIds);
    }

    private UserVO userDTOTOVO(IamUserDTO iamUserDTOList) {
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(iamUserDTOList, userVO);
        if (iamUserDTOList.getLdap()) {
            userVO.setLoginName(iamUserDTOList.getLoginName());
        } else {
            userVO.setLoginName(iamUserDTOList.getEmail());
        }
        return userVO;
    }


}
