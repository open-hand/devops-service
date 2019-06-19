package io.choerodon.devops.app.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.devops.api.dto.SecretRepDTO;
import io.choerodon.devops.api.dto.SecretReqDTO;
import io.choerodon.devops.api.validator.DevopsSecretValidator;
import io.choerodon.devops.app.service.DeployMsgHandlerService;
import io.choerodon.devops.app.service.DevopsSecretService;
import io.choerodon.devops.domain.application.entity.*;
import io.choerodon.devops.domain.application.handler.CheckOptionsHandler;
import io.choerodon.devops.domain.application.handler.ObjectOperation;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.infra.common.util.Base64Util;
import io.choerodon.devops.infra.common.util.EnvUtil;
import io.choerodon.devops.infra.common.util.GitUserNameUtil;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.common.util.enums.CommandStatus;
import io.choerodon.devops.infra.common.util.enums.HelmObjectKind;
import io.choerodon.devops.infra.common.util.enums.ObjectType;
import io.choerodon.devops.infra.common.util.enums.SecretStatus;
import io.kubernetes.client.models.V1ObjectMeta;
import io.kubernetes.client.models.V1Secret;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    private static final String SECRET_KIND = "secret";

    @Autowired
    private DevopsSecretRepository devopsSecretRepository;
    @Autowired
    private DevopsEnvironmentRepository devopsEnvironmentRepository;
    @Autowired
    private EnvUtil envUtil;
    @Autowired
    private DevopsEnvCommandRepository devopsEnvCommandRepository;
    @Autowired
    private UserAttrRepository userAttrRepository;
    @Autowired
    private DevopsEnvUserPermissionRepository devopsEnvUserPermissionRepository;
    @Autowired
    private DevopsEnvFileResourceRepository devopsEnvFileResourceRepository;
    @Autowired
    private GitlabRepository gitlabRepository;
    @Autowired
    private CheckOptionsHandler checkOptionsHandler;
    @Autowired
    private DeployMsgHandlerService deployMsgHandlerService;

    @Override
    @Transactional(rollbackFor=Exception.class)
    public SecretRepDTO createOrUpdate(SecretReqDTO secretReqDTO) {

        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验用户是否有环境的权限
        devopsEnvUserPermissionRepository.checkEnvDeployPermission(TypeUtil.objToLong(GitUserNameUtil.getUserId()),
                secretReqDTO.getEnvId());
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(secretReqDTO.getEnvId());
        //校验环境是否链接
        envUtil.checkEnvConnection(devopsEnvironmentE.getClusterE().getId());

        // 处理secret对象
        DevopsSecretE devopsSecretE = handleSecret(secretReqDTO);
        // 初始化V1Secret对象
        V1Secret v1Secret = initV1Secret(devopsSecretE);

        // 更新操作如果key-value没有改变
        if (UPDATE.equals(secretReqDTO.getType())) {

            //更新secret的时候校验gitops库文件是否存在,处理部署secret时，由于没有创gitops文件导致的部署失败
            checkOptionsHandler.check(devopsEnvironmentE, secretReqDTO.getId(), secretReqDTO.getName(), SECRET);

            DevopsSecretE oldSecretE = devopsSecretRepository
                    .selectByEnvIdAndName(secretReqDTO.getEnvId(), secretReqDTO.getName());
            Map<String, String> oldMap = new HashMap<>();
            for (Map.Entry<String, String> e : oldSecretE.getValue().entrySet()) {
                oldMap.put(e.getKey(), Base64Util.getBase64DecodedString(e.getValue()));
            }
            if (oldMap.equals(secretReqDTO.getValue())) {
                devopsSecretRepository.update(devopsSecretE);
                return ConvertHelper.convert(devopsSecretE, SecretRepDTO.class);
            }
        }
        DevopsEnvCommandE devopsEnvCommandE = initDevopsEnvCommandE(secretReqDTO.getType());

        // 在gitops库处理secret文件
        operateEnvGitLabFile(TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()), v1Secret, devopsSecretE,
                devopsEnvCommandE, CREATE.equals(secretReqDTO.getType()), userAttrE);

        return ConvertHelper.convert(devopsSecretRepository.queryBySecretId(devopsSecretE.getId()), SecretRepDTO.class);
    }

    private DevopsSecretE handleSecret(SecretReqDTO secretReqDTO) {
        if (CREATE.equals(secretReqDTO.getType())) {
            // 校验secret名字合法性和环境下唯一性
            DevopsSecretValidator.checkName(secretReqDTO.getName());
            devopsSecretRepository.checkName(secretReqDTO.getName(), secretReqDTO.getEnvId());
        }
        // 校验key-name
        if (!secretReqDTO.getType().equals("kubernetes.io/dockerconfigjson")) {
            DevopsSecretValidator.checkKeyName(secretReqDTO.getValue().keySet());
        }

        DevopsSecretE devopsSecretE = ConvertHelper.convert(secretReqDTO, DevopsSecretE.class);
        devopsSecretE.setStatus(SecretStatus.OPERATING.getStatus());

        return devopsSecretE;
    }

    private V1Secret initV1Secret(DevopsSecretE devopsSecretE) {
        V1Secret secret = new V1Secret();
        secret.setApiVersion("v1");
        secret.setKind(SECRET);
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

        //操作secret数据库
        if (isCreate) {
            Long secretId = devopsSecretRepository.create(devopsSecretE).getId();
            devopsEnvCommandE.setObjectId(secretId);
            devopsSecretE.setId(secretId);
            devopsSecretE.setCommandId(devopsEnvCommandRepository.create(devopsEnvCommandE).getId());
            devopsSecretRepository.update(devopsSecretE);
        } else {
            devopsEnvCommandE.setObjectId(devopsSecretE.getId());
            devopsSecretE.setCommandId(devopsEnvCommandRepository.create(devopsEnvCommandE).getId());
            devopsSecretRepository.update(devopsSecretE);
        }

        // 判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
        String path = envUtil.handDevopsEnvGitRepository(devopsEnvironmentE);

        ObjectOperation<V1Secret> objectOperation = new ObjectOperation<>();
        objectOperation.setType(v1Secret);
        objectOperation.operationEnvGitlabFile("sct-" + devopsSecretE.getName(), gitlabEnvGroupProjectId,
                isCreate ? CREATE : UPDATE, userAttrE.getGitlabUserId(), devopsSecretE.getId(), SECRET, null, false,
                devopsSecretE.getEnvId(), path);
    }

    @Override
    @Transactional(rollbackFor=Exception.class)
    public Boolean deleteSecret(Long envId, Long secretId) {
        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验用户是否有环境的权限
        devopsEnvUserPermissionRepository
                .checkEnvDeployPermission(TypeUtil.objToLong(GitUserNameUtil.getUserId()), envId);
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(envId);
        //校验环境是否链接
        envUtil.checkEnvConnection(devopsEnvironmentE.getClusterE().getId());

        DevopsEnvCommandE devopsEnvCommandE = initDevopsEnvCommandE(DELETE);

        // 更新secret
        devopsEnvCommandE.setObjectId(secretId);
        DevopsSecretE devopsSecretE = devopsSecretRepository.queryBySecretId(secretId);
        devopsSecretE.setCommandId(devopsEnvCommandRepository.create(devopsEnvCommandE).getId());
        devopsSecretE.setStatus(SecretStatus.OPERATING.getStatus());
        devopsSecretRepository.update(devopsSecretE);

        //判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
        String path = envUtil.handDevopsEnvGitRepository(devopsEnvironmentE);

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
                    SECRET, null, false, devopsEnvironmentE.getId(), path);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSecretByGitOps(Long secretId) {
        DevopsSecretE devopsSecretE = devopsSecretRepository.queryBySecretId(secretId);
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(devopsSecretE.getEnvId());
        envUtil.checkEnvConnection(devopsEnvironmentE.getClusterE().getId());
        DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository.query(devopsSecretE.getCommandId());
        devopsEnvCommandE.setStatus(CommandStatus.SUCCESS.getStatus());
        devopsEnvCommandRepository.update(devopsEnvCommandE);
        devopsEnvCommandRepository.listByObjectAll(HelmObjectKind.SECRET.toValue(), devopsSecretE.getId()).forEach(t -> deployMsgHandlerService.deleteCommandById(t));
        devopsSecretRepository.deleteSecret(secretId);
    }

    @Override
    public void addSecretByGitOps(SecretReqDTO secretReqDTO, Long userId) {
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(secretReqDTO.getEnvId());
        //校验环境是否链接
        envUtil.checkEnvConnection(devopsEnvironmentE.getClusterE().getId());

        // 处理secret对象
        DevopsSecretE devopsSecretE = handleSecret(secretReqDTO);

        // 创建secret
        Long secretId = devopsSecretRepository.create(devopsSecretE).getId();
        DevopsEnvCommandE devopsEnvCommandE = new DevopsEnvCommandE();
        devopsEnvCommandE.setCommandType(CREATE);
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
        envUtil.checkEnvConnection(devopsEnvironmentE.getClusterE().getId());

        DevopsSecretE oldSecretE = devopsSecretRepository
                .selectByEnvIdAndName(secretReqDTO.getEnvId(), secretReqDTO.getName());
        if (oldSecretE.getValue().equals(secretReqDTO.getValue())) {
            return;
        }

        // 处理secret对象
        DevopsSecretE devopsSecretE = handleSecret(secretReqDTO);

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
    public PageInfo<SecretRepDTO> listByOption(Long envId, PageRequest pageRequest, String params) {
        return ConvertPageHelper
                .convertPageInfo(devopsSecretRepository.listByOption(envId, pageRequest, params), SecretRepDTO.class);
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

}
