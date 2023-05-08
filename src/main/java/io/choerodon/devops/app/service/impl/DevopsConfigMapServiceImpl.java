package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.infra.constant.ExceptionConstants.PublicCode.DEVOPS_NAME_EXIST;
import static io.choerodon.devops.infra.constant.MiscConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsConfigMapRespVO;
import io.choerodon.devops.api.vo.DevopsConfigMapVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.enums.CommandStatus;
import io.choerodon.devops.infra.enums.CommandType;
import io.choerodon.devops.infra.enums.ObjectType;
import io.choerodon.devops.infra.enums.SendSettingEnum;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.gitops.ResourceConvertToYamlHandler;
import io.choerodon.devops.infra.gitops.ResourceFileCheckHandler;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.DevopsConfigMapMapper;
import io.choerodon.devops.infra.util.*;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

@Service
public class DevopsConfigMapServiceImpl implements DevopsConfigMapService {
    public static final String CONFIGMAP = "ConfigMap";
    public static final String CONFIG_MAP_PREFIX = "configMap-";
    private static final String MASTER = "master";
    private Gson gson = new Gson();


    @Autowired
    private ClusterConnectionHandler clusterConnectionHandler;
    @Autowired
    private GitlabGroupMemberService gitlabGroupMemberService;
    @Autowired
    private ResourceFileCheckHandler resourceFileCheckHandler;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private DevopsConfigMapMapper devopsConfigMapMapper;
    @Autowired
    private UserAttrService userAttrService;
    @Autowired
    private DevopsEnvCommandService devopsEnvCommandService;
    @Autowired
    private DevopsEnvFileResourceService devopsEnvFileResourceService;
    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    @Lazy
    private SendNotificationService sendNotificationService;
    @Autowired
    private PermissionHelper permissionHelper;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOrUpdate(Long projectId, Boolean sync, DevopsConfigMapVO devopsConfigMapVO) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = permissionHelper.checkEnvBelongToProject(projectId, devopsConfigMapVO.getEnvId());
        UserAttrDTO userAttrDTO;
        if (!sync) {
            userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
            //检验gitops库是否存在，校验操作人是否是有gitops库的权限
            gitlabGroupMemberService.checkEnvProject(devopsEnvironmentDTO, userAttrDTO);
        } else {
            userAttrDTO = new UserAttrDTO();
            userAttrDTO.setGitlabUserId(1L);
        }

        //校验用户是否有环境的权限
        if (!sync) {
            //校验环境相关信息
            devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);
        }

        //初始化ConfigMap对象
        V1ConfigMap v1ConfigMap = initConfigMap(devopsConfigMapVO);
        //处理创建数据
        DevopsConfigMapDTO devopsConfigMapDTO = ConvertUtils.convertObject(devopsConfigMapVO, DevopsConfigMapDTO.class);
        devopsConfigMapDTO.setValue(gson.toJson(devopsConfigMapVO.getValue()));
        //更新判断configMap key-value是否改变
        if (devopsConfigMapVO.getType().equals(UPDATE_TYPE)) {
            //更新configMap的时候校验gitops库文件是否存在,处理部署configMap时，由于没有创gitops文件导致的部署失败
            resourceFileCheckHandler.check(devopsEnvironmentDTO, devopsConfigMapVO.getId(), devopsConfigMapVO.getName(), CONFIGMAP);
            if (devopsConfigMapVO.getValue().equals(gson.fromJson(baseQueryById(devopsConfigMapDTO.getId()).getValue(), Map.class))) {
                baseUpdate(devopsConfigMapDTO);
                return;
            }
        }
        DevopsEnvCommandDTO devopsEnvCommandDTO = initDevopsEnvCommandDTO(devopsConfigMapVO.getType());
        //判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
        String filePath = clusterConnectionHandler.handDevopsEnvGitRepository(devopsEnvironmentDTO, devopsEnvironmentDTO.getProjectId(), devopsEnvironmentDTO.getCode(), devopsEnvironmentDTO.getId(), devopsEnvironmentDTO.getEnvIdRsa(), devopsEnvironmentDTO.getType(), devopsEnvironmentDTO.getClusterCode());
        //在gitops库处理ingress文件
        operateEnvGitLabFile(
                TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), v1ConfigMap, devopsConfigMapVO.getType().equals(CREATE_TYPE), filePath, devopsConfigMapDTO, userAttrDTO, devopsEnvCommandDTO, devopsConfigMapVO.getAppServiceId());
    }


    @Override
    public DevopsConfigMapRespVO createOrUpdateByGitOps(DevopsConfigMapVO devopsConfigMapVO, Long userId) {
        DevopsEnvironmentDTO environmentDTO = devopsEnvironmentService.baseQueryById(devopsConfigMapVO.getEnvId());
        //校验环境是否连接
        clusterConnectionHandler.checkEnvConnection(environmentDTO.getClusterId());

        //处理创建数据
        DevopsConfigMapDTO devopsConfigMapDTO = ConvertUtils.convertObject(devopsConfigMapVO, DevopsConfigMapDTO.class);
        devopsConfigMapDTO.setValue(gson.toJson(devopsConfigMapVO.getValue()));
        DevopsEnvCommandDTO devopsEnvCommandDTO = initDevopsEnvCommandDTO(devopsConfigMapVO.getType());
        devopsEnvCommandDTO.setCreatedBy(userId);

        if (devopsConfigMapVO.getType().equals(CREATE_TYPE)) {
            Long configMapId = baseCreate(devopsConfigMapDTO).getId();
            devopsEnvCommandDTO.setObjectId(configMapId);
            devopsConfigMapDTO.setId(configMapId);
            devopsConfigMapDTO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());
            baseUpdate(devopsConfigMapDTO);
        } else {
            devopsEnvCommandDTO.setObjectId(devopsConfigMapDTO.getId());
            devopsConfigMapDTO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());
            baseUpdate(devopsConfigMapDTO);
        }
        return ConvertUtils.convertObject(devopsConfigMapDTO, DevopsConfigMapRespVO.class);
    }

    @Override
    public DevopsConfigMapRespVO query(Long configMapId) {
        DevopsConfigMapDTO devopsConfigMapDTO = devopsConfigMapMapper.queryById(configMapId);

        if (configMapId == null) {
            return null;
        }

        DevopsConfigMapRespVO devopsConfigMapRespVO = ConvertUtils.convertObject(devopsConfigMapDTO, DevopsConfigMapRespVO.class);

        devopsConfigMapRespVO.setValue(gson.fromJson(devopsConfigMapDTO.getValue(), new TypeToken<Map<String, String>>() {
        }.getType()));

        if (devopsConfigMapDTO.getCreatedBy() != null && devopsConfigMapDTO.getCreatedBy() != 0) {
            devopsConfigMapRespVO.setCreatorName(ResourceCreatorInfoUtil.getOperatorName(baseServiceClientOperator, devopsConfigMapDTO.getCreatedBy()));
        }
        if (devopsConfigMapDTO.getLastUpdatedBy() != null && devopsConfigMapDTO.getLastUpdatedBy() != 0) {
            devopsConfigMapRespVO.setLastUpdaterName(ResourceCreatorInfoUtil.getOperatorName(baseServiceClientOperator, devopsConfigMapDTO.getLastUpdatedBy()));
        }

        return devopsConfigMapRespVO;
    }

    @Override
    public Page<DevopsConfigMapRespVO> pageByOptions(Long projectId, Long envId, PageRequest pageable, String searchParam, Long appServiceId) {

        Page<DevopsConfigMapDTO> devopsConfigMapDTOPageInfo = basePageByEnv(
                envId, pageable, searchParam, appServiceId);
        devopsConfigMapDTOPageInfo.getContent().forEach(devopsConfigMapRepDTO -> {
            List<String> keys = new ArrayList<>();
            gson.fromJson(devopsConfigMapRepDTO.getValue(), Map.class).forEach((key, value) ->
                    keys.add(key.toString()));
            devopsConfigMapRepDTO.setKey(keys);
        });
        return ConvertUtils.convertPage(devopsConfigMapDTOPageInfo, DevopsConfigMapRespVO.class);
    }


    @Override
    public void deleteByGitOps(Long configMapId) {
        DevopsConfigMapDTO devopsConfigMapDTO = baseQueryById(configMapId);
        //校验环境是否链接
        DevopsEnvironmentDTO environmentDTO = devopsEnvironmentService.baseQueryById(devopsConfigMapDTO.getEnvId());
        clusterConnectionHandler.checkEnvConnection(environmentDTO.getClusterId());

        devopsEnvCommandService.baseListByObject(ObjectType.CONFIGMAP.getType(), configMapId).forEach(devopsEnvCommandDTO -> devopsEnvCommandService.baseDelete(devopsEnvCommandDTO.getId()));
        baseDelete(configMapId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long projectId, Long configMapId) {
        DevopsConfigMapDTO devopsConfigMapDTO = baseQueryById(configMapId);

        if (devopsConfigMapDTO == null) {
            return;
        }

        DevopsEnvironmentDTO devopsEnvironmentDTO = permissionHelper.checkEnvBelongToProject(projectId, devopsConfigMapDTO.getEnvId());

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);

        DevopsEnvCommandDTO devopsEnvCommandE = initDevopsEnvCommandDTO(DELETE_TYPE);

        //更新configmap
        devopsEnvCommandE.setObjectId(configMapId);
        devopsConfigMapDTO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandE).getId());
        baseUpdate(devopsConfigMapDTO);


        //判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
        String path = clusterConnectionHandler.handDevopsEnvGitRepository(devopsEnvironmentDTO, devopsEnvironmentDTO.getProjectId(), devopsEnvironmentDTO.getCode(), devopsEnvironmentDTO.getId(), devopsEnvironmentDTO.getEnvIdRsa(), devopsEnvironmentDTO.getType(), devopsEnvironmentDTO.getClusterCode());

        //查询改对象所在文件中是否含有其它对象
        DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService
                .baseQueryByEnvIdAndResourceId(devopsEnvironmentDTO.getId(), configMapId, CONFIGMAP);
        if (devopsEnvFileResourceDTO == null) {
            baseDelete(configMapId);
            if (gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), MASTER,
                    CONFIG_MAP_PREFIX + devopsConfigMapDTO.getName() + ".yaml")) {
                gitlabServiceClientOperator.deleteFile(
                        TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()),
                        CONFIG_MAP_PREFIX + devopsConfigMapDTO.getName() + ".yaml",
                        String.format("【DELETE】%s", CONFIG_MAP_PREFIX + devopsConfigMapDTO.getName() + ".yaml"),
                        TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()), MASTER);
            }
            return;
        } else {
            if (!gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), MASTER,
                    devopsEnvFileResourceDTO.getFilePath())) {
                baseDelete(configMapId);
                devopsEnvFileResourceService.baseDeleteById(devopsEnvFileResourceDTO.getId());
                return;
            }
        }
        List<DevopsEnvFileResourceDTO> devopsEnvFileResourceDTOS = devopsEnvFileResourceService.baseQueryByEnvIdAndPath(devopsEnvironmentDTO.getId(), devopsEnvFileResourceDTO.getFilePath());

        //如果对象所在文件只有一个对象，则直接删除文件,否则把对象从文件中去掉，更新文件
        if (devopsEnvFileResourceDTOS.size() == 1) {
            if (gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), MASTER,
                    devopsEnvFileResourceDTO.getFilePath())) {
                gitlabServiceClientOperator.deleteFile(
                        TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()),
                        devopsEnvFileResourceDTO.getFilePath(),
                        String.format("【DELETE】%s", devopsEnvFileResourceDTO.getFilePath()),
                        TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()), "master");
            }
        } else {
            ResourceConvertToYamlHandler<V1ConfigMap> resourceConvertToYamlHandler = new ResourceConvertToYamlHandler<>();
            V1ConfigMap v1ConfigMap = new V1ConfigMap();
            V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
            v1ObjectMeta.setName(devopsConfigMapDTO.getName());
            v1ConfigMap.setMetadata(v1ObjectMeta);
            resourceConvertToYamlHandler.setType(v1ConfigMap);
            Integer gitalbEnvProjectId = TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId());
            resourceConvertToYamlHandler.operationEnvGitlabFile(
                    null,
                    gitalbEnvProjectId,
                    DELETE_TYPE,
                    userAttrDTO.getGitlabUserId(),
                    devopsConfigMapDTO.getId(), CONFIGMAP, null, false, devopsEnvironmentDTO.getId(), path);
        }
        //删除配置映射加上消息发送
        sendNotificationService.sendWhenConfigMap(devopsConfigMapDTO, SendSettingEnum.DELETE_RESOURCE.value());
    }

    @Override
    public void checkName(Long envId, String name) {
        if (!isNameUnique(envId, name)) {
            throw new CommonException(DEVOPS_NAME_EXIST);
        }
    }

    @Override
    public boolean isNameUnique(Long envId, String name) {
        DevopsConfigMapDTO devopsConfigMapDTO = new DevopsConfigMapDTO();
        devopsConfigMapDTO.setName(name);
        devopsConfigMapDTO.setEnvId(envId);
        return devopsConfigMapMapper.selectCount(devopsConfigMapDTO) == 0;
    }

    @Override
    public DevopsConfigMapDTO baseQueryByEnvIdAndName(Long envId, String name) {
        DevopsConfigMapDTO devopsConfigMapDTO = new DevopsConfigMapDTO();
        devopsConfigMapDTO.setName(name);
        devopsConfigMapDTO.setEnvId(envId);
        return devopsConfigMapMapper.selectOne(devopsConfigMapDTO);
    }

    @Override
    public DevopsConfigMapDTO baseCreate(DevopsConfigMapDTO devopsConfigMapDTO) {
        if (devopsConfigMapMapper.insert(devopsConfigMapDTO) != 1) {
            throw new CommonException("devops.configMap.create");
        }
        return devopsConfigMapDTO;
    }

    @Override
    public DevopsConfigMapDTO baseUpdate(DevopsConfigMapDTO devopsConfigMapDTO) {
        DevopsConfigMapDTO oldDevopsConfigMapDTO = devopsConfigMapMapper.selectByPrimaryKey(devopsConfigMapDTO.getId());
        devopsConfigMapDTO.setObjectVersionNumber(oldDevopsConfigMapDTO.getObjectVersionNumber());
        if (devopsConfigMapMapper.updateByPrimaryKeySelective(devopsConfigMapDTO) != 1) {
            throw new CommonException("devops.configMap.update");
        }
        return devopsConfigMapDTO;
    }

    @Override
    public DevopsConfigMapDTO baseQueryById(Long id) {
        return devopsConfigMapMapper.selectByPrimaryKey(id);
    }

    @Override
    public void baseDelete(Long id) {
        devopsConfigMapMapper.deleteByPrimaryKey(id);
    }

    @Override
    public Page<DevopsConfigMapDTO> basePageByEnv(Long envId, PageRequest pageable, String params, Long appServiceId) {
        Map maps = gson.fromJson(params, Map.class);
        return PageHelper.doPageAndSort(PageRequestUtil.simpleConvertSortForPage(pageable), () -> devopsConfigMapMapper.listByEnv(envId,
                TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM)),
                TypeUtil.cast(maps.get(TypeUtil.PARAMS)),
                appServiceId));
    }

    @Override
    public List<DevopsConfigMapDTO> baseListByEnv(Long envId) {
        DevopsConfigMapDTO devopsConfigMapDTO = new DevopsConfigMapDTO();
        devopsConfigMapDTO.setEnvId(envId);
        return devopsConfigMapMapper.select(devopsConfigMapDTO);
    }

    private V1ConfigMap initConfigMap(DevopsConfigMapVO devopsConfigMapVO) {
        V1ConfigMap v1ConfigMap = new V1ConfigMap();
        v1ConfigMap.setApiVersion("v1");
        v1ConfigMap.setKind(CONFIGMAP);
        V1ObjectMeta metadata = new V1ObjectMeta();
        metadata.setName(devopsConfigMapVO.getName());
        v1ConfigMap.setMetadata(metadata);
        v1ConfigMap.setData(devopsConfigMapVO.getValue());
        return v1ConfigMap;
    }

    private void operateEnvGitLabFile(Integer envGitLabProjectId,
                                      V1ConfigMap v1ConfigMap,
                                      Boolean isCreate,
                                      String path,
                                      DevopsConfigMapDTO devopsConfigMapDTO,
                                      UserAttrDTO userAttrDTO,
                                      DevopsEnvCommandDTO devopsEnvCommandDTO,
                                      Long appServiceId) {
        //操作configMap数据库
        if (isCreate) {
            Long configMapId = baseCreate(devopsConfigMapDTO).getId();
            if (appServiceId != null) {
                devopsConfigMapDTO.setAppServiceId(appServiceId);
            }
            devopsEnvCommandDTO.setObjectId(configMapId);
            devopsConfigMapDTO.setId(configMapId);
            devopsConfigMapDTO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());
            baseUpdate(devopsConfigMapDTO);
        } else {
            devopsEnvCommandDTO.setObjectId(devopsConfigMapDTO.getId());
            devopsConfigMapDTO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());
            baseUpdate(devopsConfigMapDTO);
        }

        ResourceConvertToYamlHandler<V1ConfigMap> resourceConvertToYamlHandler = new ResourceConvertToYamlHandler<>();
        resourceConvertToYamlHandler.setType(v1ConfigMap);
        resourceConvertToYamlHandler.operationEnvGitlabFile(CONFIG_MAP_PREFIX + devopsConfigMapDTO.getName(), envGitLabProjectId, isCreate ? CREATE_TYPE : UPDATE_TYPE,
                userAttrDTO.getGitlabUserId(), devopsConfigMapDTO.getId(), CONFIGMAP, null, false, devopsConfigMapDTO.getEnvId(), path);
    }

    private DevopsEnvCommandDTO initDevopsEnvCommandDTO(String type) {
        DevopsEnvCommandDTO devopsEnvCommandDTO = new DevopsEnvCommandDTO();
        if (type.equals(CREATE_TYPE)) {
            devopsEnvCommandDTO.setCommandType(CommandType.CREATE.getType());
        } else if (type.equals(UPDATE_TYPE)) {
            devopsEnvCommandDTO.setCommandType(CommandType.UPDATE.getType());
        } else {
            devopsEnvCommandDTO.setCommandType(CommandType.DELETE.getType());
        }
        devopsEnvCommandDTO.setObject(ObjectType.CONFIGMAP.getType());
        devopsEnvCommandDTO.setStatus(CommandStatus.OPERATING.getStatus());
        return devopsEnvCommandDTO;
    }

    @Override
    public void baseDeleteByEnvId(Long envId) {
        DevopsConfigMapDTO devopsConfigMapDTO = new DevopsConfigMapDTO();
        devopsConfigMapDTO.setEnvId(envId);
        devopsConfigMapMapper.delete(devopsConfigMapDTO);
    }
}
