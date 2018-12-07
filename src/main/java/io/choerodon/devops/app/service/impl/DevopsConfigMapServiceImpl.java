package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import io.kubernetes.client.models.V1ConfigMap;
import io.kubernetes.client.models.V1ObjectMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.DevopsConfigMapDTO;
import io.choerodon.devops.api.dto.DevopsConfigMapRepDTO;
import io.choerodon.devops.app.service.DevopsConfigMapService;
import io.choerodon.devops.app.service.DevopsEnvironmentService;
import io.choerodon.devops.app.service.GitlabGroupMemberService;
import io.choerodon.devops.domain.application.entity.*;
import io.choerodon.devops.domain.application.handler.ObjectOperation;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.infra.common.util.EnvUtil;
import io.choerodon.devops.infra.common.util.GitUserNameUtil;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.common.util.enums.CommandStatus;
import io.choerodon.devops.infra.common.util.enums.CommandType;
import io.choerodon.devops.infra.common.util.enums.ObjectType;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.websocket.helper.EnvListener;

@Service
public class DevopsConfigMapServiceImpl implements DevopsConfigMapService {

    public static final String CREATE = "create";
    public static final String UPDATE = "update";
    public static final String DELETE = "delete";
    public static final String CONFIGMAP = "ConfigMap";
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
    private EnvListener envListener;
    @Autowired
    private GitlabGroupMemberService gitlabGroupMemberService;
    @Autowired
    private DevopsConfigMapRepository devopsConfigMapRepository;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private DevopsEnvFileResourceRepository devopsEnvFileResourceRepository;
    @Autowired
    private GitlabRepository gitlabRepository;


    @Override
    public void createOrUpdate(Long projectId, DevopsConfigMapDTO devopsConfigMapDTO) {
        //校验用户是否有环境的权限
        devopsEnvUserPermissionRepository.checkEnvDeployPermission(TypeUtil.objToLong(GitUserNameUtil.getUserId()), devopsConfigMapDTO.getEnvId());

        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(devopsConfigMapDTO.getEnvId());
        //校验环境是否连接
        envUtil.checkEnvConnection(devopsEnvironmentE.getClusterE().getId(), envListener);

        //初始化ConfigMap对象
        V1ConfigMap v1ConfigMap = initConfigMap(devopsConfigMapDTO);

        //处理创建数据
        DevopsConfigMapE devopsConfigMapE = ConvertHelper.convert(devopsConfigMapDTO, DevopsConfigMapE.class);
        devopsConfigMapE.setValue(gson.toJson(devopsConfigMapDTO.getValue()));
        //更新判断configMap key-value是否改变
        if (devopsConfigMapDTO.getType().equals(UPDATE)) {
            if (devopsConfigMapDTO.getValue().equals(gson.fromJson(devopsConfigMapRepository.queryById(devopsConfigMapE.getId()).getValue(), Map.class))) {
                devopsConfigMapRepository.update(devopsConfigMapE);
                return;
            }
        }
        DevopsEnvCommandE devopsEnvCommandE = initDevopsEnvCommandE(devopsConfigMapDTO.getType());

        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
        String filePath = devopsEnvironmentService.handDevopsEnvGitRepository(devopsEnvironmentE);

        //检验gitops库是否存在，校验操作人是否是有gitops库的权限
        gitlabGroupMemberService.checkEnvProject(devopsEnvironmentE, userAttrE);

        //在gitops库处理ingress文件
        operateEnvGitLabFile(
                TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()), v1ConfigMap, devopsConfigMapDTO.getType().equals(CREATE) ? true : false, filePath, devopsConfigMapE, userAttrE, devopsEnvCommandE);
    }


    @Override
    public DevopsConfigMapRepDTO createOrUpdateByGitOps(DevopsConfigMapDTO devopsConfigMapDTO, Long userId) {
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(devopsConfigMapDTO.getEnvId());
        //校验环境是否连接
        envUtil.checkEnvConnection(devopsEnvironmentE.getClusterE().getId(), envListener);

        //处理创建数据
        DevopsConfigMapE devopsConfigMapE = ConvertHelper.convert(devopsConfigMapDTO, DevopsConfigMapE.class);
        devopsConfigMapE.setValue(gson.toJson(devopsConfigMapDTO.getValue()));
        DevopsEnvCommandE devopsEnvCommandE = initDevopsEnvCommandE(devopsConfigMapDTO.getType());
        devopsEnvCommandE.setCreatedBy(userId);

        if (devopsConfigMapDTO.getType().equals(CREATE)) {
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
    public Page<DevopsConfigMapRepDTO> listByEnv(Long projectId, Long envId, PageRequest pageRequest, String searchParam) {
        Page<DevopsConfigMapE> devopsConfigMapES = devopsConfigMapRepository.listByEnv(
                envId, pageRequest, searchParam);
        List<Long> connectedEnvList = envUtil.getConnectedEnvList(envListener);
        List<Long> updatedEnvList = envUtil.getUpdatedEnvList(envListener);
        devopsConfigMapES.stream().forEach(devopsConfigMapE -> {
            List<String> keys = new ArrayList<>();
            gson.fromJson(devopsConfigMapE.getValue(), Map.class).forEach((key, value) ->
                    keys.add(key.toString()));
            devopsConfigMapE.setKey(keys);
            DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(devopsConfigMapE.getDevopsEnvironmentE().getId());
            if (connectedEnvList.contains(devopsEnvironmentE.getClusterE().getId())
                    && updatedEnvList.contains(devopsEnvironmentE.getClusterE().getId())) {
                devopsConfigMapE.setEnvStatus(true);
            }
        });
        return ConvertPageHelper.convertPage(devopsConfigMapES, DevopsConfigMapRepDTO.class);
    }


    @Override
    public void deleteByGitOps(Long configMapId) {
        DevopsConfigMapE devopsConfigMapE = devopsConfigMapRepository.queryById(configMapId);
        //校验环境是否链接
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(devopsConfigMapE.getDevopsEnvironmentE().getId());
        envUtil.checkEnvConnection(devopsEnvironmentE.getClusterE().getId(), envListener);

        DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository.query(devopsConfigMapE.getDevopsEnvCommandE().getId());

        //更新数据
        devopsEnvCommandE.setStatus(CommandStatus.SUCCESS.getStatus());
        devopsEnvCommandRepository.update(devopsEnvCommandE);
        devopsConfigMapRepository.delete(configMapId);
    }

    @Override
    public void delete(Long configMapId) {
        DevopsConfigMapE devopsConfigMapE = devopsConfigMapRepository.queryById(configMapId);

        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(devopsConfigMapE.getDevopsEnvironmentE().getId());

        //校验用户是否有环境的权限
        devopsEnvUserPermissionRepository.checkEnvDeployPermission(TypeUtil.objToLong(GitUserNameUtil.getUserId()), devopsEnvironmentE.getId());
//
//        //校验环境是否连接
        envUtil.checkEnvConnection(devopsEnvironmentE.getClusterE().getId(), envListener);

        DevopsEnvCommandE devopsEnvCommandE = initDevopsEnvCommandE(DELETE);

        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //检验gitops库是否存在，校验操作人是否是有gitops库的权限
        gitlabGroupMemberService.checkEnvProject(devopsEnvironmentE, userAttrE);

        //更新ingress
        devopsEnvCommandE.setObjectId(configMapId);
        devopsConfigMapE.initDevopsEnvCommandE(devopsEnvCommandRepository.create(devopsEnvCommandE).getId());
        devopsConfigMapRepository.update(devopsConfigMapE);


        //判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
        String path = devopsEnvironmentService.handDevopsEnvGitRepository(devopsEnvironmentE);

        //查询改对象所在文件中是否含有其它对象
        DevopsEnvFileResourceE devopsEnvFileResourceE = devopsEnvFileResourceRepository
                .queryByEnvIdAndResource(devopsEnvironmentE.getId(), configMapId, CONFIGMAP);
        if (devopsEnvFileResourceE == null) {
            devopsConfigMapRepository.delete(configMapId);
            return;

        }
        List<DevopsEnvFileResourceE> devopsEnvFileResourceES = devopsEnvFileResourceRepository.queryByEnvIdAndPath(devopsEnvironmentE.getId(), devopsEnvFileResourceE.getFilePath());

        //如果对象所在文件只有一个对象，则直接删除文件,否则把对象从文件中去掉，更新文件
        if (devopsEnvFileResourceES.size() == 1) {
            gitlabRepository.deleteFile(
                    TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()),
                    devopsEnvFileResourceE.getFilePath(),
                    "DELETE FILE",
                    TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
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
                    DELETE,
                    userAttrE.getGitlabUserId(),
                    devopsConfigMapE.getId(), CONFIGMAP, devopsEnvironmentE.getId(), path);
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
                                      UserAttrE userAttrE, DevopsEnvCommandE devopsEnvCommandE) {

        DevopsConfigMapE beforeDevopsConfigMapE = devopsConfigMapRepository.queryByEnvIdAndName(devopsConfigMapE.getDevopsEnvironmentE().getId(), devopsConfigMapE.getName());
        DevopsEnvCommandE beforeDevopsEnvCommandE = new DevopsEnvCommandE();
        if (beforeDevopsConfigMapE != null) {
            beforeDevopsEnvCommandE = devopsEnvCommandRepository.query(beforeDevopsConfigMapE.getDevopsEnvCommandE().getId());
        }


        ObjectOperation<V1ConfigMap> objectOperation = new ObjectOperation<>();
        objectOperation.setType(v1ConfigMap);
        objectOperation.operationEnvGitlabFile("configMap-" + devopsConfigMapE.getName(), envGitLabProjectId, isCreate ? CREATE : UPDATE,
                userAttrE.getGitlabUserId(), devopsConfigMapE.getId(), CONFIGMAP, devopsConfigMapE.getDevopsEnvironmentE().getId(), path);


        DevopsConfigMapE afterDevopsConfigMapE = devopsConfigMapRepository.queryByEnvIdAndName(devopsConfigMapE.getDevopsEnvironmentE().getId(), devopsConfigMapE.getName());
        DevopsEnvCommandE afterDevopsEnvCommandE = new DevopsEnvCommandE();
        if (afterDevopsConfigMapE != null) {
            afterDevopsEnvCommandE = devopsEnvCommandRepository.query(afterDevopsConfigMapE.getDevopsEnvCommandE().getId());
        }

        //创建或更新数据,当集群速度较快时，会导致部署速度快于gitlab创文件的返回速度，从而域名成功的状态会被错误更新为处理中，所以用before和after去区分是否部署成功。成功不再执行域名数据库操作
        if (isCreate && afterDevopsConfigMapE == null) {
            Long configMapId = devopsConfigMapRepository.create(devopsConfigMapE).getId();
            devopsEnvCommandE.setObjectId(configMapId);
            devopsConfigMapE.setId(configMapId);
            devopsConfigMapE.initDevopsEnvCommandE(devopsEnvCommandRepository.create(devopsEnvCommandE).getId());
            devopsConfigMapRepository.update(devopsConfigMapE);
        } else if (beforeDevopsEnvCommandE.getId().equals(afterDevopsEnvCommandE.getId())) {
            devopsEnvCommandE.setObjectId(devopsConfigMapE.getId());
            devopsConfigMapE.initDevopsEnvCommandE(devopsEnvCommandRepository.create(devopsEnvCommandE).getId());
            devopsConfigMapRepository.update(devopsConfigMapE);
        }

    }


    private DevopsEnvCommandE initDevopsEnvCommandE(String type) {
        DevopsEnvCommandE devopsEnvCommandE = new DevopsEnvCommandE();
        if (type.equals(CREATE)) {
            devopsEnvCommandE.setCommandType(CommandType.CREATE.getType());
        } else if (type.equals(UPDATE)) {
            devopsEnvCommandE.setCommandType(CommandType.UPDATE.getType());
        } else {
            devopsEnvCommandE.setCommandType(CommandType.DELETE.getType());
        }
        devopsEnvCommandE.setObject(ObjectType.CONFIGMAP.getType());
        devopsEnvCommandE.setStatus(CommandStatus.OPERATING.getStatus());
        return devopsEnvCommandE;
    }
}
