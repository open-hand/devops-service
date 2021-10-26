package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.DevopsDeployValueVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.DevopsDeployValueMapper;
import io.choerodon.devops.infra.util.*;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:01 2019/4/10
 * Description:
 */
@Service
public class DevopsDeployValueServiceImpl implements DevopsDeployValueService {
    @Autowired
    private DevopsDeployValueMapper devopsDeployValueMapper;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private ClusterConnectionHandler clusterConnectionHandler;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    @Lazy
    private AppServiceInstanceService appServiceInstanceService;
    @Autowired
    private PermissionHelper permissionHelper;
    @Autowired
    private AppServiceService appServiceService;
    @Autowired
    private DevopsCdEnvDeployInfoService devopsCdEnvDeployInfoService;

    /**
     * 前端传入的排序字段和Mapper文件中的字段名的映射
     */
    private static final Map<String, String> ORDER_BY_FIELD_MAP_FOR_OWNER;
    private static final Map<String, String> ORDER_BY_FIELD_MAP_FOR_MEMBER;

    static {
        Map<String, String> map = new HashMap<>();
        map.put("envName", "de.name");
        map.put("appServiceName", "da.name");
        map.put("name", "dpv.name");
        map.put("lastUpdateDate", "dpv.last_update_date");
        ORDER_BY_FIELD_MAP_FOR_OWNER = Collections.unmodifiableMap(map);

        Map<String, String> map2 = new HashMap<>();
        map2.put("envName", "c.env_name");
        map2.put("appServiceName", "c.app_service_name");
        map2.put("name", "c.name");
        map2.put("lastUpdateDate", "c.last_update_date");
        ORDER_BY_FIELD_MAP_FOR_MEMBER = Collections.unmodifiableMap(map2);
    }

    @Override
    public DevopsDeployValueVO createOrUpdate(Long projectId, DevopsDeployValueVO devopsDeployValueVO) {
        permissionHelper.checkEnvBelongToProject(projectId, devopsDeployValueVO.getEnvId());
        permissionHelper.checkAppServiceBelongToProject(projectId, devopsDeployValueVO.getAppServiceId());

        FileUtil.checkYamlFormat(devopsDeployValueVO.getValue());

        DevopsDeployValueDTO devopsDeployValueDTO = ConvertUtils.convertObject(devopsDeployValueVO, DevopsDeployValueDTO.class);
        devopsDeployValueDTO.setProjectId(projectId);
        devopsDeployValueDTO = baseCreateOrUpdate(devopsDeployValueDTO);
        return ConvertUtils.convertObject(devopsDeployValueDTO, DevopsDeployValueVO.class);
    }

    @Override
    public void delete(Long projectId, Long valueId) {
        DevopsDeployValueDTO devopsDeployValueDTO = devopsDeployValueMapper.selectByPrimaryKey(valueId);
        permissionHelper.checkEnvBelongToProject(projectId, devopsDeployValueDTO.getEnvId());
        baseDelete(valueId);
    }

    @Override
    public Page<DevopsDeployValueVO> pageByOptions(Long projectId, Long appServiceId, Long envId, PageRequest pageable, String params) {
        List<Long> updatedEnvList = clusterConnectionHandler.getUpdatedClusterList();
        Long userId = DetailsHelper.getUserDetails().getUserId();
        Page<DevopsDeployValueDTO> deployValueDTOPageInfo;
        boolean projectOwnerOrRoot = permissionHelper.isGitlabProjectOwnerOrGitlabAdmin(projectId, userId);

        if (projectOwnerOrRoot) {
            deployValueDTOPageInfo = basePageByOptionsWithOwner(projectId, appServiceId, envId, userId, pageable, params);
        } else {
            deployValueDTOPageInfo = basePageByOptionsWithMember(projectId, appServiceId, envId, userId, pageable, params);
        }
        Page<DevopsDeployValueVO> page = ConvertUtils.convertPage(deployValueDTOPageInfo, DevopsDeployValueVO.class);
        page.getContent().forEach(value -> {
            IamUserDTO iamUserDTO = baseServiceClientOperator.queryUserByUserId(value.getCreatedBy());
            value.setCreateUserName(iamUserDTO.getLoginName());
            value.setCreateUserUrl(iamUserDTO.getImageUrl());
            value.setCreateUserRealName(iamUserDTO.getRealName());
            DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(value.getEnvId());
            if (updatedEnvList.contains(devopsEnvironmentDTO.getClusterId())) {
                value.setEnvStatus(true);
            }
        });
        return page;
    }

    @Override
    public DevopsDeployValueVO query(Long projectId, Long valueId) {
        DevopsDeployValueVO devopsDeployValueVO = ConvertUtils.convertObject(devopsDeployValueMapper.queryById(valueId), DevopsDeployValueVO.class);
        devopsDeployValueVO.setIndex(checkDelete(projectId, valueId));
        return devopsDeployValueVO;
    }

    @Override
    public void checkName(Long projectId, String name, Long envId) {
        // 当查询结果不为空且不是更新部署配置时抛出异常
        if (!isNameUnique(projectId, name, envId)) {
            throw new CommonException("error.devops.pipeline.value.name.exit");
        }
    }

    @Override
    public boolean isNameUnique(Long projectId, String name, Long envId) {
        DevopsDeployValueDTO devopsDeployValueDTO = new DevopsDeployValueDTO();
        devopsDeployValueDTO.setEnvId(Objects.requireNonNull(envId));
        devopsDeployValueDTO.setName(Objects.requireNonNull(name));
        return devopsDeployValueMapper.selectCount(devopsDeployValueDTO) == 0;
    }

    @Override
    public List<DevopsDeployValueVO> listByEnvAndApp(Long projectId, Long appServiceId, Long envId, String name) {
        List<DevopsDeployValueVO> devopsDeployValueVOS = ConvertUtils.convertList(baseQueryByAppIdAndEnvId(projectId, appServiceId, envId, name), DevopsDeployValueVO.class);
        if (CollectionUtils.isEmpty(devopsDeployValueVOS)) {
            return new ArrayList<>();
        }
        // 添加用户信息
        List<Long> userIds = devopsDeployValueVOS.stream().map(DevopsDeployValueVO::getCreatedBy).collect(Collectors.toList());
        List<IamUserDTO> iamUserDTOS = baseServiceClientOperator.listUsersByIds(userIds);
        Map<Long, IamUserDTO> userDTOMap = iamUserDTOS.stream().collect(Collectors.toMap(IamUserDTO::getId, Function.identity()));
        devopsDeployValueVOS.forEach(devopsDeployValueVO -> {
            IamUserDTO iamUserDTO = userDTOMap.get(devopsDeployValueVO.getCreatedBy());
            if (iamUserDTO != null) {
                devopsDeployValueVO.setCreator(iamUserDTO);
            }
        });
        return devopsDeployValueVOS;
    }

    @Override
    public Boolean checkDelete(Long projectId, Long valueId) {
        DevopsDeployValueDTO devopsDeployValueDTO = devopsDeployValueMapper.selectByPrimaryKey(valueId);
        permissionHelper.checkEnvBelongToProject(projectId, devopsDeployValueDTO.getEnvId());

        List<DevopsCdEnvDeployInfoDTO> devopsCdEnvDeployInfoDTOS = devopsCdEnvDeployInfoService.queryCurrentByValueId(valueId);
        if (devopsCdEnvDeployInfoDTOS == null || devopsCdEnvDeployInfoDTOS.isEmpty()) {
            List<AppServiceInstanceDTO> appServiceInstanceDTOS = appServiceInstanceService.baseListByValueId(valueId);
            return CollectionUtils.isEmpty(appServiceInstanceDTOS);
        }
        return false;
    }

    @Override
    public Page<DevopsDeployValueDTO> basePageByOptionsWithOwner(Long projectId, Long appServiceId, Long envId, Long userId, PageRequest pageable, String params) {
        Map<String, Object> maps = TypeUtil.castMapParams(params);
        Map<String, Object> searchParamMap = TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM));
        List<String> paramList = TypeUtil.cast(maps.get(TypeUtil.PARAMS));
        return PageHelper.doPageAndSort(PageRequestUtil.getMappedPage(pageable, ORDER_BY_FIELD_MAP_FOR_OWNER), () -> devopsDeployValueMapper.listByOptionsWithOwner(projectId, appServiceId, envId, userId, searchParamMap, paramList));
    }

    @Override
    public Page<DevopsDeployValueDTO> basePageByOptionsWithMember(Long projectId, Long appServiceId, Long envId, Long userId, PageRequest pageable, String params) {
        Map<String, Object> maps = TypeUtil.castMapParams(params);
        Map<String, Object> searchParamMap = TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM));
        List<String> paramList = TypeUtil.cast(maps.get(TypeUtil.PARAMS));
        return PageHelper.doPageAndSort(PageRequestUtil.getMappedPage(pageable, ORDER_BY_FIELD_MAP_FOR_MEMBER), () -> devopsDeployValueMapper.listByOptionsWithMember(projectId, appServiceId, envId, userId, searchParamMap, paramList));
    }

    @Override
    public DevopsDeployValueDTO baseCreateOrUpdate(DevopsDeployValueDTO devopsDeployValueDTO) {
        if (devopsDeployValueDTO.getId() == null) {
            checkName(devopsDeployValueDTO.getProjectId(), devopsDeployValueDTO.getName(), devopsDeployValueDTO.getEnvId());

            checkAppServiceAndEnvInProject(devopsDeployValueDTO.getProjectId(), devopsDeployValueDTO.getEnvId(), devopsDeployValueDTO.getAppServiceId());
            MapperUtil.resultJudgedInsert(devopsDeployValueMapper, devopsDeployValueDTO, "error.insert.pipeline.value");
        } else {
            DevopsDeployValueDTO original = devopsDeployValueMapper.selectByPrimaryKey(devopsDeployValueDTO.getId());
            // 更新了名字就校验名称的唯一性
            if (!Objects.equals(original.getName(), devopsDeployValueDTO.getName())) {
                checkName(devopsDeployValueDTO.getProjectId(), devopsDeployValueDTO.getName(), devopsDeployValueDTO.getEnvId());
            }

            devopsDeployValueDTO.setEnvId(null);
            devopsDeployValueDTO.setAppServiceId(null);
            devopsDeployValueDTO.setObjectVersionNumber(original.getObjectVersionNumber());
            MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsDeployValueMapper, devopsDeployValueDTO, "error.update.pipeline.value");
        }
        return devopsDeployValueMapper.selectByPrimaryKey(devopsDeployValueDTO.getId());
    }

    /**
     * 校验环境和应用服务存在且都在这个项目下
     *
     * @param projectId    项目id
     * @param envId        环境id
     * @param appServiceId 应用服务id
     */
    private void checkAppServiceAndEnvInProject(Long projectId, Long envId, Long appServiceId) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(envId);
        if (devopsEnvironmentDTO == null) {
            throw new CommonException("error.env.id.not.exist", envId);
        }
        if (!projectId.equals(devopsEnvironmentDTO.getProjectId())) {
            throw new CommonException("error.env.not.in.this.project", envId, projectId);
        }

        AppServiceDTO appServiceDTO = appServiceService.baseQuery(appServiceId);
        if (appServiceDTO == null) {
            throw new CommonException("error.app.id.not.exist", appServiceId);
        }
        if (!projectId.equals(appServiceDTO.getProjectId())) {
            throw new CommonException("error.app.not.in.this.project", appServiceId, projectId);
        }
    }

    @Override
    public void baseDelete(Long valueId) {
        DevopsDeployValueDTO devopsDeployValueDTO = new DevopsDeployValueDTO();
        devopsDeployValueDTO.setId(valueId);
        devopsDeployValueMapper.deleteByPrimaryKey(devopsDeployValueDTO);
    }

    @Override
    public DevopsDeployValueDTO baseQueryById(Long valueId) {
        DevopsDeployValueDTO devopsDeployValueDTO = new DevopsDeployValueDTO();
        devopsDeployValueDTO.setId(valueId);
        return devopsDeployValueMapper.selectByPrimaryKey(devopsDeployValueDTO);
    }

    @Override
    public List<DevopsDeployValueDTO> baseQueryByAppIdAndEnvId(Long projectId, Long appServiceId, Long envId, String name) {
        return devopsDeployValueMapper.listByAppServiceIdAndEnvId(projectId, appServiceId, envId, name);
    }

    @Override
    public void deleteByEnvId(Long envId) {
        DevopsDeployValueDTO condition = new DevopsDeployValueDTO();
        condition.setEnvId(Objects.requireNonNull(envId));
        devopsDeployValueMapper.delete(condition);
    }
}
