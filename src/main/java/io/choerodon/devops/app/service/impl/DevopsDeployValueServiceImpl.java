package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.choerodon.devops.infra.constant.ExceptionConstants.AppServiceCode.DEVOPS_APP_ID_NOT_EXIST;
import static io.choerodon.devops.infra.constant.ExceptionConstants.AppServiceCode.DEVOPS_APP_NOT_IN_THIS_PROJECT;
import static io.choerodon.devops.infra.constant.ExceptionConstants.EnvironmentCode.DEVOPS_ENV_ID_NOT_EXIST;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.DevopsDeployValueVO;
import io.choerodon.devops.api.vo.PipelineInstanceReferenceVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.AppServiceInstanceDTO;
import io.choerodon.devops.infra.dto.DevopsDeployValueDTO;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.AppServiceInstanceMapper;
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
    //    @Autowired
//    private DevopsCdEnvDeployInfoService devopsCdEnvDeployInfoService;
    @Autowired
    @Lazy
    private DevopsCiJobService devopsCiJobService;

    @Autowired
    @Lazy
    private PipelineService pipelineService;

    @Autowired
    private AppServiceInstanceMapper appServiceInstanceMapper;
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
        devopsDeployValueVO.setIndex(CollectionUtils.isEmpty(checkDelete(projectId, valueId)));
        // 设置环境连接状态
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsDeployValueVO.getEnvId());
        List<Long> updatedEnvList = clusterConnectionHandler.getUpdatedClusterList();
        devopsDeployValueVO.setEnvStatus(updatedEnvList.contains(devopsEnvironmentDTO.getClusterId()));
        return devopsDeployValueVO;
    }

    @Override
    public void checkName(Long projectId, String name, Long envId) {
        // 当查询结果不为空且不是更新部署配置时抛出异常
        if (!isNameUnique(projectId, name, envId)) {
            throw new CommonException("devops.pipeline.value.name.exit");
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
    public List<PipelineInstanceReferenceVO> checkDelete(Long projectId, Long valueId) {
        List<PipelineInstanceReferenceVO> pipelineInstanceReferenceVOList = new ArrayList<>();
        List<PipelineInstanceReferenceVO> ciPipelineInstanceReferenceVOList = devopsCiJobService.listDeployValuePipelineReference(projectId, valueId);
        if (!CollectionUtils.isEmpty(ciPipelineInstanceReferenceVOList)) {
            pipelineInstanceReferenceVOList.addAll(ciPipelineInstanceReferenceVOList);
        }
        List<PipelineInstanceReferenceVO> pipelineInstanceReferenceVOList1 = pipelineService.listDeployValuePipelineReference(projectId, valueId);
        if (!CollectionUtils.isEmpty(pipelineInstanceReferenceVOList1)) {
            pipelineInstanceReferenceVOList.addAll(pipelineInstanceReferenceVOList1);
        }
        return pipelineInstanceReferenceVOList;
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
            MapperUtil.resultJudgedInsert(devopsDeployValueMapper, devopsDeployValueDTO, "devops.insert.pipeline.value");
        } else {
            DevopsDeployValueDTO original = devopsDeployValueMapper.selectByPrimaryKey(devopsDeployValueDTO.getId());
            // 更新了名字就校验名称的唯一性
            if (!Objects.equals(original.getName(), devopsDeployValueDTO.getName())) {
                checkName(devopsDeployValueDTO.getProjectId(), devopsDeployValueDTO.getName(), devopsDeployValueDTO.getEnvId());
            }

            devopsDeployValueDTO.setEnvId(null);
            devopsDeployValueDTO.setAppServiceId(null);
            devopsDeployValueDTO.setObjectVersionNumber(original.getObjectVersionNumber());
            MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsDeployValueMapper, devopsDeployValueDTO, "devops.update.pipeline.value");
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
            throw new CommonException(DEVOPS_ENV_ID_NOT_EXIST, envId);
        }
        if (!projectId.equals(devopsEnvironmentDTO.getProjectId())) {
            throw new CommonException("devops.env.not.in.this.project", envId, projectId);
        }

        AppServiceDTO appServiceDTO = appServiceService.baseQuery(appServiceId);
        if (appServiceDTO == null) {
            throw new CommonException(DEVOPS_APP_ID_NOT_EXIST, appServiceId);
        }
        if (!projectId.equals(appServiceDTO.getProjectId())) {
            throw new CommonException(DEVOPS_APP_NOT_IN_THIS_PROJECT, appServiceId, projectId);
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

    @Override
    public List<DevopsDeployValueDTO> listValueByInstanceId(Long projectId, Long instanceId) {
        // 查询流水线和持续部署是否绑定配置
        List<DevopsDeployValueDTO> list = devopsDeployValueMapper.listByInstanceId(instanceId);
        if (!CollectionUtils.isEmpty(list)) {
            return list.stream().distinct().collect(Collectors.toList());
        } else {
            // 查询界面是否同步配置到资源
            AppServiceInstanceDTO instanceDTO = appServiceInstanceMapper.selectByPrimaryKey(instanceId);
            if (instanceDTO.getSyncDeployValueId() != null) {
                return Collections.singletonList(devopsDeployValueMapper.selectByPrimaryKey(instanceDTO.getSyncDeployValueId()));
            } else {
                return null;
            }
        }
    }

    @Override
    public void updateValueByInstanceId(Long projectId, Long instanceId, HashMap<String, String> mapValue) {
        if (Objects.isNull(mapValue.get("value"))) {
            throw new CommonException("devops.value.is.null");
        }
        List<DevopsDeployValueDTO> list = devopsDeployValueMapper.listByInstanceId(instanceId);
        if (CollectionUtils.isEmpty(list)) {
            throw new CommonException("devops.instance.not.bind.values");
        }
        list.forEach(t -> {
            t.setValue(mapValue.get("value"));
            devopsDeployValueMapper.updateByPrimaryKey(t);
        });
    }
}
