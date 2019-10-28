package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Joiner;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.util.GitUserNameUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsDeployRecordVO;
import io.choerodon.devops.app.service.DevopsDeployRecordService;
import io.choerodon.devops.app.service.DevopsEnvironmentService;
import io.choerodon.devops.app.service.PipelineService;
import io.choerodon.devops.infra.dto.DevopsDeployRecordDTO;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.DevopsDeployRecordMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.PageRequestUtil;
import io.choerodon.devops.infra.util.TypeUtil;

/**
 * Created by Sheep on 2019/7/29.
 */
@Service
public class DevopsDeployRecordServiceImpl implements DevopsDeployRecordService {
    private static final String COMMA = ",";
    private static final String DEPLOY_STATUS = "deployStatus";
    private static final String DEPLOY_TYPE = "deployType";
    private static final String RUNNING = "running";
    private static final String MANUAL = "manual";


    @Autowired
    private DevopsDeployRecordMapper devopsDeployRecordMapper;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private PipelineService pipelineService;

    @Override
    public PageInfo<DevopsDeployRecordVO> pageByProjectId(Long projectId, String params, PageRequest pageRequest) {
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
        Boolean projectOwner = baseServiceClientOperator.isProjectOwner(TypeUtil.objToLong(GitUserNameUtil.getUserId()), projectDTO);

        PageInfo<DevopsDeployRecordDTO> devopsDeployRecordDTOPageInfo = basePageByProjectId(projectId, params, pageRequest);

        PageInfo<DevopsDeployRecordVO> devopsDeployRecordVOPageInfo = ConvertUtils.convertPage(devopsDeployRecordDTOPageInfo, DevopsDeployRecordVO.class);

        //查询用户信息
        List<Long> userIds = devopsDeployRecordVOPageInfo.getList().stream().map(DevopsDeployRecordVO::getDeployCreatedBy).collect(Collectors.toList());
        Map<Long, IamUserDTO> userMap = new HashMap<>(pageRequest.getSize());
        baseServiceClientOperator.listUsersByIds(userIds).forEach(user -> userMap.put(user.getId(), user));

        //设置环境信息以及用户信息
        devopsDeployRecordVOPageInfo.getList().forEach(devopsDeployRecordVO -> {
            if (devopsDeployRecordVO.getDeployType().equals("auto") && !devopsDeployRecordVO.getDeployStatus().equals("success")) {
                pipelineService.setPipelineRecordDetail(projectOwner, devopsDeployRecordVO);
            }
            if (userMap.containsKey(devopsDeployRecordVO.getDeployCreatedBy())) {
                IamUserDTO targetUser = userMap.get(devopsDeployRecordVO.getDeployCreatedBy());
                devopsDeployRecordVO.setUserName(targetUser.getRealName());
                if(targetUser.getLdap()) {
                    devopsDeployRecordVO.setUserLoginName(targetUser.getLoginName());
                }else {
                    devopsDeployRecordVO.setUserLoginName(targetUser.getEmail());
                }
                devopsDeployRecordVO.setUserImage(targetUser.getImageUrl());
            }
        });
        return devopsDeployRecordVOPageInfo;
    }


    @Override
    public PageInfo<DevopsDeployRecordDTO> basePageByProjectId(Long projectId, String params, PageRequest pageRequest) {
        Map<String, Object> maps = TypeUtil.castMapParams(params);
        Map<String, Object> cast = TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM));
        if (cast.get(DEPLOY_TYPE) != null && cast.get(DEPLOY_STATUS) != null) {
            if (MANUAL.equals(cast.get(DEPLOY_TYPE)) && RUNNING.equals(cast.get(DEPLOY_STATUS))) {
                cast.put(DEPLOY_STATUS, "operating");
            } else if ("auto".equals(cast.get(DEPLOY_TYPE)) && RUNNING.equals(cast.get(DEPLOY_STATUS))) {
                cast.put(DEPLOY_STATUS, RUNNING);
            }
        }
        maps.put(TypeUtil.SEARCH_PARAM, cast);
        return PageHelper.startPage(pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(
                () -> devopsDeployRecordMapper.listByProjectId(projectId,
                        TypeUtil.cast(maps.get(TypeUtil.PARAMS)),
                        TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM))
                )
        );
    }


    @Override
    public void baseCreate(DevopsDeployRecordDTO devopsDeployRecordDTO) {
        if (devopsDeployRecordMapper.insert(devopsDeployRecordDTO) != 1) {
            throw new CommonException("error.deploy.record.insert");
        }
    }

    @Override
    public void baseDelete(DevopsDeployRecordDTO devopsDeployRecordDTO) {
        devopsDeployRecordMapper.delete(devopsDeployRecordDTO);
    }

    @Override
    public void deleteManualRecordByEnv(Long envId) {
        DevopsDeployRecordDTO deleteCondition = new DevopsDeployRecordDTO();
        deleteCondition.setEnv(String.valueOf(envId));
        deleteCondition.setDeployType("manual");

        devopsDeployRecordMapper.delete(deleteCondition);
    }

    @Override
    public void deleteRelatedRecordOfInstance(Long instanceId) {
        devopsDeployRecordMapper.deleteRelatedRecordOfInstance(instanceId);
    }
}
