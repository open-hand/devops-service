package io.choerodon.devops.app.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.kubernetes.client.models.V1ObjectMeta;
import io.kubernetes.client.models.V1Secret;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.SecretRepDTO;
import io.choerodon.devops.api.dto.SecretReqDTO;
import io.choerodon.devops.api.validator.DevopsSecretValidator;
import io.choerodon.devops.app.service.DevopsEnvironmentService;
import io.choerodon.devops.app.service.DevopsSecretService;
import io.choerodon.devops.domain.application.entity.*;
import io.choerodon.devops.domain.application.handler.ObjectOperation;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.infra.common.util.Base64Util;
import io.choerodon.devops.infra.common.util.EnvUtil;
import io.choerodon.devops.infra.common.util.GitUserNameUtil;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.common.util.enums.CommandStatus;
import io.choerodon.devops.infra.common.util.enums.ObjectType;
import io.choerodon.devops.infra.common.util.enums.SecretStatus;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.websocket.helper.EnvListener;

/**
 * Created by n!Ck
 * Date: 18-12-4
 * Time: 上午9:52
 * Description:
 */

@Component
public class DevopsSecretServiceImpl implements DevopsSecretService {

    private static final String SECRET = "Secret";
    private static final String CREATE = "create";
    private static final String UPDATE = "update";
    private static final String DELETE = "delete";

    private DevopsSecretRepository devopsSecretRepository;
    private DevopsEnvironmentRepository devopsEnvironmentRepository;
    private EnvUtil envUtil;
    private EnvListener envListener;
    private DevopsEnvCommandRepository devopsEnvCommandRepository;
    private UserAttrRepository userAttrRepository;
    private DevopsEnvironmentService devopsEnvironmentService;
    private DevopsEnvUserPermissionRepository devopsEnvUserPermissionRepository;
    private DevopsEnvFileResourceRepository devopsEnvFileResourceRepository;
    private GitlabRepository gitlabRepository;

    @Autowired
    public DevopsSecretServiceImpl(DevopsSecretRepository devopsSecretRepository,
                                   DevopsEnvironmentRepository devopsEnvironmentRepository,
                                   EnvUtil envUtil, EnvListener envListener,
                                   DevopsEnvCommandRepository devopsEnvCommandRepository,
                                   UserAttrRepository userAttrRepository,
                                   DevopsEnvironmentService devopsEnvironmentService,
                                   DevopsEnvUserPermissionRepository devopsEnvUserPermissionRepository,
                                   DevopsEnvFileResourceRepository devopsEnvFileResourceRepository,
                                   GitlabRepository gitlabRepository) {
        this.devopsSecretRepository = devopsSecretRepository;
        this.devopsEnvironmentRepository = devopsEnvironmentRepository;
        this.envUtil = envUtil;
        this.envListener = envListener;
        this.devopsEnvCommandRepository = devopsEnvCommandRepository;
        this.userAttrRepository = userAttrRepository;
        this.devopsEnvironmentService = devopsEnvironmentService;
        this.devopsEnvUserPermissionRepository = devopsEnvUserPermissionRepository;
        this.devopsEnvFileResourceRepository = devopsEnvFileResourceRepository;
        this.gitlabRepository = gitlabRepository;
    }

    @Override
    public SecretRepDTO createOrUpdate(SecretReqDTO secretReqDTO) {

        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验用户是否有环境的权限
        devopsEnvUserPermissionRepository.checkEnvDeployPermission(TypeUtil.objToLong(GitUserNameUtil.getUserId()),
                secretReqDTO.getEnvId());
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(secretReqDTO.getEnvId());
        //校验环境是否链接
        envUtil.checkEnvConnection(devopsEnvironmentE.getClusterE().getId(), envListener);

        // 处理secret对象
        DevopsSecretE devopsSecretE = handleSecret(secretReqDTO);
        // 初始化V1Secret对象
        V1Secret v1Secret = initV1Secret(devopsSecretE);

        // 更新操作如果key-value没有改变
        if ("update".equals(secretReqDTO.getType())) {
            DevopsSecretE oldSecretE = devopsSecretRepository
                    .selectByEnvIdAndName(secretReqDTO.getEnvId(), secretReqDTO.getName());
            Map<String, String> oldMap = new HashMap<>();
            for (Map.Entry<String, String> e : oldSecretE.getValue().entrySet()) {
                oldMap.put(e.getKey(), Base64Util.getBase64DecodedString(e.getValue()));
            }
            if (oldMap.equals(secretReqDTO.getValue())) {
                return ConvertHelper.convert(oldSecretE, SecretRepDTO.class);
            }
        }
        DevopsEnvCommandE devopsEnvCommandE = initDevopsEnvCommandE(secretReqDTO.getType());

        // 在gitops库处理secret文件
        operateEnvGitLabFile(TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()), v1Secret, devopsSecretE,
                devopsEnvCommandE, "create".equals(secretReqDTO.getType()), userAttrE);

        return ConvertHelper.convert(devopsSecretRepository.queryBySecretId(devopsSecretE.getId()), SecretRepDTO.class);
    }

    private DevopsSecretE handleSecret(SecretReqDTO secretReqDTO) {
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(secretReqDTO.getEnvId());

        if ("create".equals(secretReqDTO.getType())) {
            // 校验secret名字合法性和环境下唯一性
            DevopsSecretValidator.checkName(secretReqDTO.getName());
            devopsSecretRepository.checkName(secretReqDTO.getName(), secretReqDTO.getEnvId());
        }
        // 校验key-name
        DevopsSecretValidator.checkKeyName(secretReqDTO.getValue().keySet());

        DevopsSecretE devopsSecretE = ConvertHelper.convert(secretReqDTO, DevopsSecretE.class);
        devopsSecretE.setStatus(SecretStatus.OPERATING.getStatus());

        return devopsSecretE;
    }

    private V1Secret initV1Secret(DevopsSecretE devopsSecretE) {
        V1Secret secret = new V1Secret();
        secret.setApiVersion("v1");
        secret.setKind("Secret");
        V1ObjectMeta metadata = new V1ObjectMeta();
        metadata.setName(devopsSecretE.getName());
        secret.setMetadata(metadata);
        secret.setType("Opaque");
        secret.setStringData(devopsSecretE.getValue());
        return secret;
    }

    private void operateEnvGitLabFile(Integer gitlabEnvGroupProjectId, V1Secret v1Secret, DevopsSecretE devopsSecretE,
                                      DevopsEnvCommandE devopsEnvCommandE, Boolean isCreate, UserAttrE userAttrE) {

        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(devopsSecretE.getEnvId());

        DevopsSecretE beforeDevopsSecretE = devopsSecretRepository
                .selectByEnvIdAndName(devopsSecretE.getEnvId(), devopsSecretE.getName());
        DevopsEnvCommandE beforeDevopsEnvCommandE = new DevopsEnvCommandE();
        if (beforeDevopsSecretE != null) {
            beforeDevopsEnvCommandE = devopsEnvCommandRepository.query(beforeDevopsSecretE.getCommandId());
        }

        // 判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
        String path = devopsEnvironmentService.handDevopsEnvGitRepository(devopsEnvironmentE);

        ObjectOperation<V1Secret> objectOperation = new ObjectOperation<>();
        objectOperation.setType(v1Secret);
        objectOperation.operationEnvGitlabFile("sct-" + devopsSecretE.getName(), gitlabEnvGroupProjectId,
                isCreate ? "create" : "update", userAttrE.getGitlabUserId(), devopsSecretE.getId(), SECRET,
                devopsSecretE.getEnvId(), path);

        DevopsSecretE afterDevopsSecretE = devopsSecretRepository
                .selectByEnvIdAndName(devopsSecretE.getEnvId(), devopsSecretE.getName());
        DevopsEnvCommandE afterDevopsEnvCommandE = new DevopsEnvCommandE();
        if (afterDevopsSecretE != null) {
            afterDevopsEnvCommandE = devopsEnvCommandRepository.query(afterDevopsSecretE.getCommandId());
        }

        if (isCreate && afterDevopsSecretE == null) {
            Long secretId = devopsSecretRepository.create(devopsSecretE).getId();
            devopsEnvCommandE.setObjectId(secretId);
            devopsSecretE.setId(secretId);
            devopsSecretE.setCommandId(devopsEnvCommandRepository.create(devopsEnvCommandE).getId());
            devopsSecretRepository.update(devopsSecretE);
        } else if (beforeDevopsEnvCommandE.getId().equals(afterDevopsEnvCommandE.getId())) {
            devopsEnvCommandE.setObjectId(devopsSecretE.getId());
            devopsSecretE.setCommandId(devopsEnvCommandRepository.create(devopsEnvCommandE).getId());
            devopsSecretRepository.update(devopsSecretE);
        }
    }

    @Override
    public Boolean deleteSecret(Long envId, Long secretId) {
        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验用户是否有环境的权限
        devopsEnvUserPermissionRepository
                .checkEnvDeployPermission(TypeUtil.objToLong(GitUserNameUtil.getUserId()), envId);
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(envId);
        //校验环境是否链接
        envUtil.checkEnvConnection(devopsEnvironmentE.getClusterE().getId(), envListener);

        DevopsEnvCommandE devopsEnvCommandE = initDevopsEnvCommandE(DELETE);

        // 更新secret
        devopsEnvCommandE.setObjectId(secretId);
        DevopsSecretE devopsSecretE = devopsSecretRepository.queryBySecretId(secretId);
        devopsSecretE.setCommandId(devopsEnvCommandRepository.create(devopsEnvCommandE).getId());
        devopsSecretE.setStatus(SecretStatus.OPERATING.getStatus());
        devopsSecretRepository.update(devopsSecretE);

        //判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
        String path = devopsEnvironmentService.handDevopsEnvGitRepository(devopsEnvironmentE);

        // 查询改对象所在文件中是否含有其它对象
        DevopsEnvFileResourceE devopsEnvFileResourceE = devopsEnvFileResourceRepository
                .queryByEnvIdAndResource(devopsEnvironmentE.getId(), secretId, SECRET);
        if (devopsEnvFileResourceE == null) {
            devopsSecretRepository.deleteSecret(secretId);
            if (gitlabRepository.getFile(TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()), "master",
                    "sct-" + devopsSecretE.getName() + ".yaml")) {
                gitlabRepository.deleteFile(
                        TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()),
                        "sct-" + devopsSecretE.getName() + ".yaml",
                        "DELETE FILE",
                        TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
            }
            return true;
        }
        List<DevopsEnvFileResourceE> devopsEnvFileResourceES = devopsEnvFileResourceRepository
                .queryByEnvIdAndPath(devopsEnvironmentE.getId(), devopsEnvFileResourceE.getFilePath());

        // 如果对象所在文件只有一个对象，则直接删除文件,否则把对象从文件中去掉，更新文件
        if (devopsEnvFileResourceES.size() == 1) {
            if (gitlabRepository.getFile(TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()), "master",
                    devopsEnvFileResourceE.getFilePath())) {
                gitlabRepository.deleteFile(TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()),
                        devopsEnvFileResourceE.getFilePath(), "DELETE FILE",
                        TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
            }
        } else {
            ObjectOperation<V1Secret> objectOperation = new ObjectOperation<>();
            V1Secret v1Secret = new V1Secret();
            V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
            v1ObjectMeta.setName(devopsSecretE.getName());
            v1Secret.setMetadata(v1ObjectMeta);
            objectOperation.setType(v1Secret);
            Integer projectId = TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId());
            objectOperation.operationEnvGitlabFile(null, projectId, DELETE, userAttrE.getGitlabUserId(), secretId,
                    SECRET, devopsEnvironmentE.getId(), path);
        }
        return true;
    }

    @Override
    public void deleteSecretByGitOps(Long secretId) {
        DevopsSecretE devopsSecretE = devopsSecretRepository.queryBySecretId(secretId);
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(devopsSecretE.getEnvId());
        envUtil.checkEnvConnection(devopsEnvironmentE.getClusterE().getId(), envListener);
        DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository.query(devopsSecretE.getCommandId());
        devopsEnvCommandE.setStatus(CommandStatus.SUCCESS.getStatus());
        devopsEnvCommandRepository.update(devopsEnvCommandE);
        devopsSecretRepository.deleteSecret(secretId);
    }

    @Override
    public void addSecretByGitOps(SecretReqDTO secretReqDTO, Long userId) {
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(secretReqDTO.getEnvId());
        //校验环境是否链接
        envUtil.checkEnvConnection(devopsEnvironmentE.getClusterE().getId(), envListener);

        // 处理secret对象
        DevopsSecretE devopsSecretE = handleSecret(secretReqDTO);
        // 初始化V1Secret对象
        V1Secret v1Secret = initV1Secret(devopsSecretE);

        // 创建secret
        Long secretId = devopsSecretRepository.create(devopsSecretE).getId();
        DevopsEnvCommandE devopsEnvCommandE = new DevopsEnvCommandE();
        devopsEnvCommandE.setCommandType("create");
        devopsEnvCommandE.setStatus(devopsSecretE.getStatus());
        devopsEnvCommandE.setObjectId(secretId);
        devopsEnvCommandE.setObject("secret");
        devopsEnvCommandE.setCreatedBy(userId);
        devopsSecretE.setCommandId(devopsEnvCommandRepository.create(devopsEnvCommandE).getId());
        devopsSecretE.setId(secretId);
        devopsSecretE.setEnvId(devopsEnvironmentE.getId());
        devopsSecretRepository.update(devopsSecretE);
    }

    @Override
    public void updateDevopsSecretByGitOps(Long projectId, Long id, SecretReqDTO secretReqDTO, Long userId) {

        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(secretReqDTO.getEnvId());
        //校验环境是否链接
        envUtil.checkEnvConnection(devopsEnvironmentE.getClusterE().getId(), envListener);

        DevopsSecretE oldSecretE = devopsSecretRepository
                .selectByEnvIdAndName(secretReqDTO.getEnvId(), secretReqDTO.getName());
        if (oldSecretE.getValue().equals(secretReqDTO.getValue())) {
            return;
        }

        // 处理secret对象
        DevopsSecretE devopsSecretE = handleSecret(secretReqDTO);
        // 初始化V1Secret对象
        V1Secret v1Secret = initV1Secret(devopsSecretE);

        DevopsEnvCommandE devopsEnvCommandE = initDevopsEnvCommandE(UPDATE);
        // 更新secret对象
        devopsEnvCommandE.setObjectId(id);
        devopsEnvCommandE.setCreatedBy(userId);
        devopsSecretE.setCommandId(devopsEnvCommandRepository.create(devopsEnvCommandE).getId());
        devopsSecretRepository.update(devopsSecretE);
    }

    private DevopsEnvCommandE initDevopsEnvCommandE(String type) {
        DevopsEnvCommandE devopsEnvCommandE = new DevopsEnvCommandE();
        devopsEnvCommandE.setCommandType(type);
        devopsEnvCommandE.setObject(ObjectType.SECRET.getType());
        devopsEnvCommandE.setStatus(CommandStatus.OPERATING.getStatus());
        return devopsEnvCommandE;
    }

    @Override
    public Page<SecretRepDTO> listByOption(Long envId, PageRequest pageRequest, String params) {
        return ConvertPageHelper
                .convertPage(devopsSecretRepository.listByOption(envId, pageRequest, params), SecretRepDTO.class);
    }

    @Override
    public SecretRepDTO querySecret(Long secretId) {
        return ConvertHelper.convert(devopsSecretRepository.queryBySecretId(secretId), SecretRepDTO.class);
    }

    @Override
    public void checkName(Long envId, String name) {
        DevopsSecretValidator.checkName(name);
        devopsSecretRepository.checkName(name, envId);
    }

    public void initMockServer(DevopsEnvironmentService devopsEnvironmentService) {
        this.devopsEnvironmentService = devopsEnvironmentService;
    }
}
