package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Joiner;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsDeployRecordVO;
import io.choerodon.devops.app.service.DevopsDeployRecordService;
import io.choerodon.devops.app.service.DevopsEnvironmentService;
import io.choerodon.devops.infra.dto.DevopsDeployRecordDTO;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.feign.operator.IamServiceClientOperator;
import io.choerodon.devops.infra.mapper.DevopsDeployRecordMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.PageRequestUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Sheep on 2019/7/29.
 */

@Service
public class DevopsDeployRecordServiceImpl implements DevopsDeployRecordService {


    private static final Gson gson = new Gson();

    @Autowired
    private DevopsDeployRecordMapper devopsDeployRecordMapper;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private IamServiceClientOperator iamServiceClientOperator;

    @Override
    public PageInfo<DevopsDeployRecordVO> pageByProjectId(Long projectId, String params, PageRequest pageRequest) {
        PageInfo<DevopsDeployRecordDTO> devopsDeployRecordDTOPageInfo = basePageByProjectId(projectId, params, pageRequest);
        Set<Long> envIds = new HashSet<>();

        //获取环境id
        devopsDeployRecordDTOPageInfo.getList().stream().filter(devopsDeployRecordDTO -> devopsDeployRecordDTO.getEnv() != null).forEach(devopsDeployRecordDTO -> {
            String[] envs = devopsDeployRecordDTO.getEnv().split(",");
            for (String env : envs) {
                envIds.add(TypeUtil.objToLong(env));
            }
        });

        //查询环境
        List<DevopsEnvironmentDTO> devopsEnvironmentDTOS = devopsEnvironmentService.baseListByIds(new ArrayList<>(envIds));

        PageInfo<DevopsDeployRecordVO> devopsDeployRecordVOPageInfo = ConvertUtils.convertPage(devopsDeployRecordDTOPageInfo, DevopsDeployRecordVO.class);

        //查询用户信息
        List<Long> userIds = devopsDeployRecordVOPageInfo.getList().stream().map(DevopsDeployRecordVO::getDeployCreatedBy).collect(Collectors.toList());
        List<IamUserDTO> iamUserDTOS = iamServiceClientOperator.listUsersByIds(userIds);


        //设置环境信息以及用户信息
        devopsDeployRecordVOPageInfo.getList().forEach(devopsDeployRecordVO -> {
            if (devopsDeployRecordVO.getEnv() != null) {
                List<String> env = Arrays.asList(devopsDeployRecordVO.getEnv().split(",")).stream().map(s -> devopsEnvironmentDTOS.stream().filter(devopsEnvironmentDTO -> devopsEnvironmentDTO.getId().equals(TypeUtil.objToLong(s))).collect(Collectors.toList()).get(0).getName()).collect(Collectors.toList());
                devopsDeployRecordVO.setEnv(Joiner.on(",").join(env));
            }
            iamUserDTOS.forEach(iamUserDTO -> {
                if (devopsDeployRecordVO.getDeployCreatedBy().equals(iamUserDTO.getId())) {
                    devopsDeployRecordVO.setUserName(iamUserDTO.getRealName());
                    devopsDeployRecordVO.setUserImage(iamUserDTO.getImageUrl());
                }
            });
        });
        return devopsDeployRecordVOPageInfo;
    }


    @Override
    public PageInfo<DevopsDeployRecordDTO> basePageByProjectId(Long projectId, String params, PageRequest pageRequest) {
        Map<String, Object> maps = TypeUtil.castMapParams(params);
        return PageHelper.startPage(pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(
                () -> devopsDeployRecordMapper.listByProjectId(projectId,
                        TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM)),
                        TypeUtil.cast(maps.get(TypeUtil.PARAMS)))
        );
    }


    @Override
    public void baseCreate(DevopsDeployRecordDTO devopsDeployRecordDTO) {
        if (devopsDeployRecordMapper.insert(devopsDeployRecordDTO) != 1) {
            throw new CommonException("error.deploy.record.insert");
        }
    }
}
