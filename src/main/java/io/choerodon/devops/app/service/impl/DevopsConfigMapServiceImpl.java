package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsConfigMapRespVO;
import io.choerodon.devops.api.vo.DevopsConfigMapVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.enums.CommandStatus;
import io.choerodon.devops.infra.enums.CommandType;
import io.choerodon.devops.infra.enums.ObjectType;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.IamServiceClientOperator;
import io.choerodon.devops.infra.gitops.ResourceConvertToYamlHandler;
import io.choerodon.devops.infra.gitops.ResourceFileCheckHandler;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.DevopsConfigMapMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.GitUserNameUtil;
import io.choerodon.devops.infra.util.PageRequestUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import io.kubernetes.client.models.V1ConfigMap;
import io.kubernetes.client.models.V1ObjectMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DevopsConfigMapServiceImpl implements DevopsConfigMapService {

    public static final String CREATE_TYPE = "create";
    public static final String UPDATE_TYPE = "update";
    public static final String DELETE_TYPE = "delete";
    public static final String CONFIGMAP = "ConfigMap";
    public static final String CONFIG_MAP_PREFIX = "configMap-";
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
    private DevopsApplicationResourceService devopsApplicationResourceService;
    @Autowired
    private DevopsEnvFileResourceService devopsEnvFileResourceService;
    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator;
    @Autowired
    private IamServiceClientOperator iamServiceClientOperator;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOrUpdate(Long projectId, Boolean sync, DevopsConfigMapVO devopsConfigMapVO) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsConfigMapVO.getEnvId());
        UserAttrDTO userAttrDTO = null;
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
        String filePath = clusterConnectionHandler.handDevopsEnvGitRepository(devopsEnvironmentDTO.getProjectId(), devopsEnvironmentDTO.getCode(), devopsEnvironmentDTO.getEnvIdRsa());
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
        DevopsConfigMapDTO devopsConfigMapDTO = baseQueryById(configMapId);
        DevopsConfigMapRespVO devopsConfigMapRespVO = ConvertUtils.convertObject(devopsConfigMapDTO, DevopsConfigMapRespVO.class);

        if (devopsConfigMapRespVO == null) {
            return null;
        }

        devopsConfigMapRespVO.setValue(gson.fromJson(devopsConfigMapDTO.getValue(), new TypeToken<Map<String, String>>() {
        }.getType()));

        if (devopsConfigMapDTO.getCreatedBy() != 0) {
            devopsConfigMapRespVO.setCreatorName(iamServiceClientOperator.queryUserByUserId(devopsConfigMapDTO.getCreatedBy()).getRealName());
        }

        return devopsConfigMapRespVO;
    }

    @Override
    public PageInfo<DevopsConfigMapRespVO> pageByOptions(Long projectId, Long envId, PageRequest pageRequest, String searchParam, Long appServiceId) {

        PageInfo<DevopsConfigMapDTO> devopsConfigMapDTOPageInfo = basePageByEnv(
                envId, pageRequest, searchParam, appServiceId);
        devopsConfigMapDTOPageInfo.getList().forEach(devopsConfigMapRepDTO -> {
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
        devopsApplicationResourceService.baseDeleteByResourceIdAndType(configMapId, ObjectType.CONFIGMAP.getType());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long configMapId) {
        DevopsConfigMapDTO devopsConfigMapDTO = baseQueryById(configMapId);

        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsConfigMapDTO.getEnvId());

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);

        DevopsEnvCommandDTO devopsEnvCommandE = initDevopsEnvCommandDTO(DELETE_TYPE);

        //更新ingress
        devopsEnvCommandE.setObjectId(configMapId);
        devopsConfigMapDTO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandE).getId());
        baseUpdate(devopsConfigMapDTO);


        //判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
        String path = clusterConnectionHandler.handDevopsEnvGitRepository(devopsEnvironmentDTO.getProjectId(), devopsEnvironmentDTO.getCode(), devopsEnvironmentDTO.getEnvIdRsa());

        //查询改对象所在文件中是否含有其它对象
        DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService
                .baseQueryByEnvIdAndResourceId(devopsEnvironmentDTO.getId(), configMapId, CONFIGMAP);
        if (devopsEnvFileResourceDTO == null) {
            baseDelete(configMapId);
            devopsApplicationResourceService.baseDeleteByResourceIdAndType(configMapId, ObjectType.CONFIGMAP.getType());
            if (gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), "master",
                    CONFIG_MAP_PREFIX + devopsConfigMapDTO.getName() + ".yaml")) {
                gitlabServiceClientOperator.deleteFile(
                        TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()),
                        CONFIG_MAP_PREFIX + devopsConfigMapDTO.getName() + ".yaml",
                        "DELETE FILE",
                        TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
            }
            return;
        } else {
            if (!gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), "master",
                    devopsEnvFileResourceDTO.getFilePath())) {
                baseDelete(configMapId);
                devopsApplicationResourceService.baseDeleteByResourceIdAndType(configMapId, ObjectType.CONFIGMAP.getType());
                devopsEnvFileResourceService.baseDeleteById(devopsEnvFileResourceDTO.getId());
                return;
            }
        }
        List<DevopsEnvFileResourceDTO> devopsEnvFileResourceDTOS = devopsEnvFileResourceService.baseQueryByEnvIdAndPath(devopsEnvironmentDTO.getId(), devopsEnvFileResourceDTO.getFilePath());

        //如果对象所在文件只有一个对象，则直接删除文件,否则把对象从文件中去掉，更新文件
        if (devopsEnvFileResourceDTOS.size() == 1) {
            if (gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), "master",
                    devopsEnvFileResourceDTO.getFilePath())) {
                gitlabServiceClientOperator.deleteFile(
                        TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()),
                        devopsEnvFileResourceDTO.getFilePath(),
                        "DELETE FILE",
                        TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
            }
        } else {
            ResourceConvertToYamlHandler<V1ConfigMap> resourceConvertToYamlHandler = new ResourceConvertToYamlHandler<>();
            V1ConfigMap v1ConfigMap = new V1ConfigMap();
            V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
            v1ObjectMeta.setName(devopsConfigMapDTO.getName());
            v1ConfigMap.setMetadata(v1ObjectMeta);
            resourceConvertToYamlHandler.setType(v1ConfigMap);
            Integer projectId = TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId());
            resourceConvertToYamlHandler.operationEnvGitlabFile(
                    null,
                    projectId,
                    DELETE_TYPE,
                    userAttrDTO.getGitlabUserId(),
                    devopsConfigMapDTO.getId(), CONFIGMAP, null, false, devopsEnvironmentDTO.getId(), path);
        }
    }

    @Override
    public void checkName(Long envId, String name) {
        DevopsConfigMapDTO devopsConfigMapDTO = baseQueryByEnvIdAndName(envId, name);
        if (devopsConfigMapDTO != null) {
            throw new CommonException("error.name.exist");
        }
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
            throw new CommonException("error.configMap.create");
        }
        return devopsConfigMapDTO;
    }

    @Override
    public DevopsConfigMapDTO baseUpdate(DevopsConfigMapDTO devopsConfigMapDTO) {
        DevopsConfigMapDTO oldDevopsConfigMapDTO = devopsConfigMapMapper.selectByPrimaryKey(devopsConfigMapDTO.getId());
        devopsConfigMapDTO.setObjectVersionNumber(oldDevopsConfigMapDTO.getObjectVersionNumber());
        if (devopsConfigMapMapper.updateByPrimaryKeySelective(devopsConfigMapDTO) != 1) {
            throw new CommonException("error.configMap.update");
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
    public PageInfo<DevopsConfigMapDTO> basePageByEnv(Long envId, PageRequest pageRequest, String params, Long appServiceId) {
        Map maps = gson.fromJson(params, Map.class);
        PageInfo<DevopsConfigMapDTO> devopsConfigMapDOS = PageHelper
                .startPage(pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() -> devopsConfigMapMapper.listByEnv(envId,
                        TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM)),
                        TypeUtil.cast(maps.get(TypeUtil.PARAMS)),
                        appServiceId));
        return devopsConfigMapDOS;
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
                DevopsApplicationResourceDTO devopsApplicationResourceDTO = new DevopsApplicationResourceDTO();
                devopsApplicationResourceDTO.setAppServiceId(appServiceId);
                devopsApplicationResourceDTO.setResourceType(ObjectType.CONFIGMAP.getType());
                devopsApplicationResourceDTO.setResourceId(configMapId);
                devopsApplicationResourceService.baseCreate(devopsApplicationResourceDTO);
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
}
