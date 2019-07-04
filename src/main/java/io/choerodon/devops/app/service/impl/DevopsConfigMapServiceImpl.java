package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.DevopsConfigMapDTO;
import io.choerodon.devops.api.dto.DevopsConfigMapRepDTO;
import io.choerodon.devops.app.service.DevopsConfigMapService;
import io.choerodon.devops.app.service.DevopsEnvironmentService;
import io.choerodon.devops.app.service.GitlabGroupMemberService;
import io.choerodon.devops.domain.application.entity.*;
import io.choerodon.devops.domain.application.handler.CheckOptionsHandler;
import io.choerodon.devops.domain.application.handler.ObjectOperation;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.infra.common.util.EnvUtil;
import io.choerodon.devops.infra.common.util.GitUserNameUtil;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.common.util.enums.CommandStatus;
import io.choerodon.devops.infra.common.util.enums.CommandType;
import io.choerodon.devops.infra.common.util.enums.ObjectType;
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
    private DevopsEnvUserPermissionRepository devopsEnvUserPermissionRepository;
    @Autowired
    private DevopsEnvironmentRepository devopsEnvironmentRepository;
    @Autowired
    private EnvUtil envUtil;
    @Autowired
    private DevopsEnvCommandRepository devopsEnvCommandRepository;
    @Autowired
    private UserAttrRepository userAttrRepository;
    @Autowired
    private GitlabGroupMemberService gitlabGroupMemberService;
    @Autowired
    private DevopsConfigMapRepository devopsConfigMapRepository;
    @Autowired
    private DevopsEnvFileResourceRepository devopsEnvFileResourceRepository;
    @Autowired
    private GitlabRepository gitlabRepository;
    @Autowired
    private CheckOptionsHandler checkOptionsHandler;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private DevopsAppResourceRepository appResourceRepository;

    @Override
    @Transactional(rollbackFor=Exception.class)
    public void createOrUpdate(Long projectId, Boolean sync, DevopsConfigMapDTO devopsConfigMapDTO) {

        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(devopsConfigMapDTO.getEnvId());

        UserAttrE userAttrE = null;
        if (!sync) {
            userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
            //检验gitops库是否存在，校验操作人是否是有gitops库的权限
            gitlabGroupMemberService.checkEnvProject(devopsEnvironmentE, userAttrE);
        } else {
            userAttrE = new UserAttrE();
            userAttrE.setGitlabUserId(1L);
        }

        //校验用户是否有环境的权限
        if(!sync) {
            //校验环境相关信息
            devopsEnvironmentService.checkEnv(devopsEnvironmentE, userAttrE);
        }

        //初始化ConfigMap对象
        V1ConfigMap v1ConfigMap = initConfigMap(devopsConfigMapDTO);

        //处理创建数据
        DevopsConfigMapE devopsConfigMapE = ConvertHelper.convert(devopsConfigMapDTO, DevopsConfigMapE.class);
        devopsConfigMapE.setValue(gson.toJson(devopsConfigMapDTO.getValue()));
        //更新判断configMap key-value是否改变
        if (devopsConfigMapDTO.getType().equals(UPDATE_TYPE)) {

            //更新configMap的时候校验gitops库文件是否存在,处理部署configMap时，由于没有创gitops文件导致的部署失败
            checkOptionsHandler.check(devopsEnvironmentE, devopsConfigMapDTO.getId(), devopsConfigMapDTO.getName(), CONFIGMAP);

            if (devopsConfigMapDTO.getValue().equals(gson.fromJson(devopsConfigMapRepository.queryById(devopsConfigMapE.getId()).getValue(), Map.class))) {
                devopsConfigMapRepository.update(devopsConfigMapE);
                return;
            }
        }
        DevopsEnvCommandE devopsEnvCommandE = initDevopsEnvCommandE(devopsConfigMapDTO.getType());

        //判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
        String filePath = envUtil.handDevopsEnvGitRepository(devopsEnvironmentE);


        //在gitops库处理ingress文件
        operateEnvGitLabFile(
                TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()), v1ConfigMap, devopsConfigMapDTO.getType().equals(CREATE_TYPE), filePath, devopsConfigMapE, userAttrE, devopsEnvCommandE,devopsConfigMapDTO.getAppId());
    }


    @Override
    public DevopsConfigMapRepDTO createOrUpdateByGitOps(DevopsConfigMapDTO devopsConfigMapDTO, Long userId) {
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(devopsConfigMapDTO.getEnvId());
        //校验环境是否连接
        envUtil.checkEnvConnection(devopsEnvironmentE.getClusterE().getId());

        //处理创建数据
        DevopsConfigMapE devopsConfigMapE = ConvertHelper.convert(devopsConfigMapDTO, DevopsConfigMapE.class);
        devopsConfigMapE.setValue(gson.toJson(devopsConfigMapDTO.getValue()));
        DevopsEnvCommandE devopsEnvCommandE = initDevopsEnvCommandE(devopsConfigMapDTO.getType());
        devopsEnvCommandE.setCreatedBy(userId);

        if (devopsConfigMapDTO.getType().equals(CREATE_TYPE)) {
            Long configMapId = devopsConfigMapRepository.create(devopsConfigMapE).getId();
            devopsEnvCommandE.setObjectId(configMapId);
            devopsConfigMapE.setId(configMapId);
            devopsConfigMapE.initDevopsEnvCommandE(devopsEnvCommandRepository.create(devopsEnvCommandE).getId());
            devopsConfigMapRepository.update(devopsConfigMapE);
        } else {
            devopsEnvCommandE.setObjectId(devopsConfigMapE.getId());
            devopsConfigMapE.initDevopsEnvCommandE(devopsEnvCommandRepository.create(devopsEnvCommandE).getId());
            devopsConfigMapRepository.update(devopsConfigMapE);
        }
        return ConvertHelper.convert(devopsConfigMapE, DevopsConfigMapRepDTO.class);
    }

    @Override
    public DevopsConfigMapRepDTO query(Long configMapId) {
        DevopsConfigMapE devopsConfigMapE = devopsConfigMapRepository.queryById(configMapId);
        DevopsConfigMapRepDTO devopsConfigMapRepDTO = ConvertHelper.convert(devopsConfigMapE, DevopsConfigMapRepDTO.class);
        devopsConfigMapRepDTO.setValue(gson.fromJson(devopsConfigMapE.getValue(), Map.class));
        return devopsConfigMapRepDTO;
    }

    @Override
    public PageInfo<DevopsConfigMapRepDTO> listByEnv(Long projectId, Long envId, PageRequest pageRequest, String searchParam) {
        PageInfo<DevopsConfigMapE> devopsConfigMapES = devopsConfigMapRepository.pageByEnv(
                envId, pageRequest, searchParam);
        devopsConfigMapES.getList().forEach(devopsConfigMapE -> {
            List<String> keys = new ArrayList<>();
            gson.fromJson(devopsConfigMapE.getValue(), Map.class).forEach((key, value) ->
                    keys.add(key.toString()));
            devopsConfigMapE.setKey(keys);
        });
        return ConvertPageHelper.convertPageInfo(devopsConfigMapES, DevopsConfigMapRepDTO.class);
    }


    @Override
    public void deleteByGitOps(Long configMapId) {
        DevopsConfigMapE devopsConfigMapE = devopsConfigMapRepository.queryById(configMapId);
        //校验环境是否链接
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(devopsConfigMapE.getDevopsEnvironmentE().getId());
        envUtil.checkEnvConnection(devopsEnvironmentE.getClusterE().getId());

        devopsEnvCommandRepository.listByObjectAll(ObjectType.CONFIGMAP.getType(), configMapId).forEach(devopsEnvCommandE -> devopsEnvCommandRepository.deleteCommandById(devopsEnvCommandE));
        devopsConfigMapRepository.delete(configMapId);
        appResourceRepository.deleteByResourceIdAndType(configMapId,ObjectType.CONFIGMAP.getType());
    }

    @Override
    @Transactional(rollbackFor=Exception.class)
    public void delete(Long configMapId) {
        DevopsConfigMapE devopsConfigMapE = devopsConfigMapRepository.queryById(configMapId);

        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(devopsConfigMapE.getDevopsEnvironmentE().getId()
        );

        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentE, userAttrE);


        DevopsEnvCommandE devopsEnvCommandE = initDevopsEnvCommandE(DELETE_TYPE);


        //更新ingress
        devopsEnvCommandE.setObjectId(configMapId);
        devopsConfigMapE.initDevopsEnvCommandE(devopsEnvCommandRepository.create(devopsEnvCommandE).getId());
        devopsConfigMapRepository.update(devopsConfigMapE);


        //判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
        String path = envUtil.handDevopsEnvGitRepository(devopsEnvironmentE);

        //查询改对象所在文件中是否含有其它对象
        DevopsEnvFileResourceE devopsEnvFileResourceE = devopsEnvFileResourceRepository
                .queryByEnvIdAndResource(devopsEnvironmentE.getId(), configMapId, CONFIGMAP);
        if (devopsEnvFileResourceE == null) {
            devopsConfigMapRepository.delete(configMapId);
            appResourceRepository.deleteByResourceIdAndType(configMapId,ObjectType.CONFIGMAP.getType());
            if (gitlabRepository.getFile(TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()), "master",
                    CONFIG_MAP_PREFIX + devopsConfigMapE.getName() + ".yaml")) {
                gitlabRepository.deleteFile(
                        TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()),
                        CONFIG_MAP_PREFIX + devopsConfigMapE.getName() + ".yaml",
                        "DELETE FILE",
                        TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
            }
            return;
        } else {
            if (!gitlabRepository.getFile(TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()), "master",
                    devopsEnvFileResourceE.getFilePath())) {
                devopsConfigMapRepository.delete(configMapId);
                appResourceRepository.deleteByResourceIdAndType(configMapId,ObjectType.CONFIGMAP.getType());
                devopsEnvFileResourceRepository.deleteFileResource(devopsEnvFileResourceE.getId());
                return;
            }
        }
        List<DevopsEnvFileResourceE> devopsEnvFileResourceES = devopsEnvFileResourceRepository.queryByEnvIdAndPath(devopsEnvironmentE.getId(), devopsEnvFileResourceE.getFilePath());

        //如果对象所在文件只有一个对象，则直接删除文件,否则把对象从文件中去掉，更新文件
        if (devopsEnvFileResourceES.size() == 1) {
            if (gitlabRepository.getFile(TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()), "master",
                    devopsEnvFileResourceE.getFilePath())) {
                gitlabRepository.deleteFile(
                        TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()),
                        devopsEnvFileResourceE.getFilePath(),
                        "DELETE FILE",
                        TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
            }
        } else {
            ObjectOperation<V1ConfigMap> objectOperation = new ObjectOperation<>();
            V1ConfigMap v1ConfigMap = new V1ConfigMap();
            V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
            v1ObjectMeta.setName(devopsConfigMapE.getName());
            v1ConfigMap.setMetadata(v1ObjectMeta);
            objectOperation.setType(v1ConfigMap);
            Integer projectId = TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId());
            objectOperation.operationEnvGitlabFile(
                    null,
                    projectId,
                    DELETE_TYPE,
                    userAttrE.getGitlabUserId(),
                    devopsConfigMapE.getId(), CONFIGMAP, null, false, devopsEnvironmentE.getId(), path);
        }
    }

    @Override
    public void checkName(Long envId, String name) {
        DevopsConfigMapE devopsConfigMapE = devopsConfigMapRepository.queryByEnvIdAndName(envId, name);
        if (devopsConfigMapE != null) {
            throw new CommonException("error.name.exist");
        }
    }


    private V1ConfigMap initConfigMap(DevopsConfigMapDTO devopsConfigMapDTO) {
        V1ConfigMap v1ConfigMap = new V1ConfigMap();
        v1ConfigMap.setApiVersion("v1");
        v1ConfigMap.setKind(CONFIGMAP);
        V1ObjectMeta metadata = new V1ObjectMeta();
        metadata.setName(devopsConfigMapDTO.getName());
        v1ConfigMap.setMetadata(metadata);
        v1ConfigMap.setData(devopsConfigMapDTO.getValue());
        return v1ConfigMap;
    }


    private void operateEnvGitLabFile(Integer envGitLabProjectId,
                                      V1ConfigMap v1ConfigMap,
                                      Boolean isCreate,
                                      String path,
                                      DevopsConfigMapE devopsConfigMapE,
                                      UserAttrE userAttrE, DevopsEnvCommandE devopsEnvCommandE,Long appId) {


        //操作configMap数据库
        if (isCreate) {
            Long configMapId = devopsConfigMapRepository.create(devopsConfigMapE).getId();
            if (appId != null) {
                DevopsAppResourceE resourceE = new DevopsAppResourceE();
                resourceE.setAppId(appId);
                resourceE.setResourceType(ObjectType.CONFIGMAP.getType());
                resourceE.setResourceId(configMapId);
                appResourceRepository.insert(resourceE);
            }
            devopsEnvCommandE.setObjectId(configMapId);
            devopsConfigMapE.setId(configMapId);
            devopsConfigMapE.initDevopsEnvCommandE(devopsEnvCommandRepository.create(devopsEnvCommandE).getId());
            devopsConfigMapRepository.update(devopsConfigMapE);
        } else {
            devopsEnvCommandE.setObjectId(devopsConfigMapE.getId());
            devopsConfigMapE.initDevopsEnvCommandE(devopsEnvCommandRepository.create(devopsEnvCommandE).getId());
            devopsConfigMapRepository.update(devopsConfigMapE);
        }

        ObjectOperation<V1ConfigMap> objectOperation = new ObjectOperation<>();
        objectOperation.setType(v1ConfigMap);
        objectOperation.operationEnvGitlabFile(CONFIG_MAP_PREFIX + devopsConfigMapE.getName(), envGitLabProjectId, isCreate ? CREATE_TYPE : UPDATE_TYPE,
                userAttrE.getGitlabUserId(), devopsConfigMapE.getId(), CONFIGMAP, null, false, devopsConfigMapE.getDevopsEnvironmentE().getId(), path);


    }


    private DevopsEnvCommandE initDevopsEnvCommandE(String type) {
        DevopsEnvCommandE devopsEnvCommandE = new DevopsEnvCommandE();
        if (type.equals(CREATE_TYPE)) {
            devopsEnvCommandE.setCommandType(CommandType.CREATE.getType());
        } else if (type.equals(UPDATE_TYPE)) {
            devopsEnvCommandE.setCommandType(CommandType.UPDATE.getType());
        } else {
            devopsEnvCommandE.setCommandType(CommandType.DELETE.getType());
        }
        devopsEnvCommandE.setObject(ObjectType.CONFIGMAP.getType());
        devopsEnvCommandE.setStatus(CommandStatus.OPERATING.getStatus());
        return devopsEnvCommandE;
    }
}
