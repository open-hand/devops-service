package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Joiner;
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
        PageInfo<DevopsDeployRecordDTO> devopsDeployRecordDTOPageInfo = basePageByProjectId(projectId, params, pageRequest);
        Set<Long> envIds = new HashSet<>();

        //获取所有涉及到的环境id
        devopsDeployRecordDTOPageInfo.getList().stream().filter(devopsDeployRecordDTO -> devopsDeployRecordDTO.getEnv() != null).forEach(devopsDeployRecordDTO -> {
            String[] envs = devopsDeployRecordDTO.getEnv().split(",");
            for (String env : envs) {
                envIds.add(TypeUtil.objToLong(env));
            }
        });

        //查询环境
        Map<Long, DevopsEnvironmentDTO> envMap = new HashMap<>(pageRequest.getSize());
        devopsEnvironmentService.baseListByIds(new ArrayList<>(envIds)).forEach(env -> envMap.put(env.getId(), env));

        PageInfo<DevopsDeployRecordVO> devopsDeployRecordVOPageInfo = ConvertUtils.convertPage(devopsDeployRecordDTOPageInfo, DevopsDeployRecordVO.class);

        //查询用户信息
        List<Long> userIds = devopsDeployRecordVOPageInfo.getList().stream().map(DevopsDeployRecordVO::getDeployCreatedBy).collect(Collectors.toList());
        Map<Long, IamUserDTO> userMap = new HashMap<>(pageRequest.getSize());
        baseServiceClientOperator.listUsersByIds(userIds).forEach(user -> userMap.put(user.getId(), user));


        //设置环境信息以及用户信息
        devopsDeployRecordVOPageInfo.getList().forEach(devopsDeployRecordVO -> {
            if (devopsDeployRecordVO.getDeployType().equals("auto")) {
                pipelineService.setPipelineRecordDetail(projectId, devopsDeployRecordVO);
            }

            // 把原本的形如"1,199"的id值转为形如"staging,production"的名称值
            if (devopsDeployRecordVO.getEnv() != null) {
                List<String> envNames = Arrays.stream(devopsDeployRecordVO.getEnv().split(COMMA))
                        .map(id -> {
                            Long envId = TypeUtil.objToLong(id);
                            if (envMap.containsKey(envId)) {
                                return envMap.get(envId).getName();
                            }
                            return null;
                        }).collect(Collectors.toList());
                if (!envNames.isEmpty()) {
                    devopsDeployRecordVO.setEnv(Joiner.on(COMMA).join(envNames));
                }
            }

            if (userMap.containsKey(devopsDeployRecordVO.getDeployCreatedBy())) {
                IamUserDTO targetUser = userMap.get(devopsDeployRecordVO.getDeployCreatedBy());
                devopsDeployRecordVO.setUserName(targetUser.getRealName());
                devopsDeployRecordVO.setUserImage(targetUser.getImageUrl());
            }
        });
        return devopsDeployRecordVOPageInfo;
    }


    @Override
    public PageInfo<DevopsDeployRecordDTO> basePageByProjectId(Long projectId, String params, PageRequest pageRequest) {
        Map<String, Object> maps = TypeUtil.castMapParams(params);
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
}
