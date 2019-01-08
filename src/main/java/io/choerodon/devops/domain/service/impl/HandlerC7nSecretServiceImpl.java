package io.choerodon.devops.domain.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.kubernetes.client.models.V1Endpoints;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.SecretReqDTO;
import io.choerodon.devops.api.validator.DevopsSecretValidator;
import io.choerodon.devops.app.service.DevopsEnvFileResourceService;
import io.choerodon.devops.app.service.DevopsSecretService;
import io.choerodon.devops.domain.application.entity.DevopsEnvCommandE;
import io.choerodon.devops.domain.application.entity.DevopsEnvFileResourceE;
import io.choerodon.devops.domain.application.entity.DevopsEnvironmentE;
import io.choerodon.devops.domain.application.entity.DevopsSecretE;
import io.choerodon.devops.domain.application.handler.GitOpsExplainException;
import io.choerodon.devops.domain.application.repository.DevopsEnvCommandRepository;
import io.choerodon.devops.domain.application.repository.DevopsEnvFileResourceRepository;
import io.choerodon.devops.domain.application.repository.DevopsSecretRepository;
import io.choerodon.devops.domain.application.valueobject.C7nSecret;
import io.choerodon.devops.domain.service.HandlerObjectFileRelationsService;
import io.choerodon.devops.infra.common.util.GitUtil;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.common.util.enums.CommandStatus;
import io.choerodon.devops.infra.common.util.enums.CommandType;
import io.choerodon.devops.infra.common.util.enums.ObjectType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by n!Ck
 * Date: 18-12-4
 * Time: 下午8:00
 * Description:
 */

@Service
public class HandlerC7nSecretServiceImpl implements HandlerObjectFileRelationsService<C7nSecret> {
    private static final String CREATE = "create";

    private static final String SECRET = "Secret";
    private static final String GIT_SUFFIX = "/.git";

    private final DevopsSecretRepository devopsSecretRepository;
    private final DevopsEnvFileResourceRepository devopsEnvFileResourceRepository;
    private final DevopsEnvCommandRepository devopsEnvCommandRepository;
    private final DevopsSecretService devopsSecretService;
    private final DevopsEnvFileResourceService devopsEnvFileResourceService;

    @Autowired
    public HandlerC7nSecretServiceImpl(DevopsSecretRepository devopsSecretRepository,
                                       DevopsEnvFileResourceRepository devopsEnvFileResourceRepository,
                                       DevopsEnvCommandRepository devopsEnvCommandRepository,
                                       DevopsSecretService devopsSecretService,
                                       DevopsEnvFileResourceService devopsEnvFileResourceService) {
        this.devopsSecretRepository = devopsSecretRepository;
        this.devopsEnvFileResourceRepository = devopsEnvFileResourceRepository;
        this.devopsEnvCommandRepository = devopsEnvCommandRepository;
        this.devopsSecretService = devopsSecretService;
        this.devopsEnvFileResourceService = devopsEnvFileResourceService;
    }

    @Override
    public void handlerRelations(Map<String, String> objectPath, List<DevopsEnvFileResourceE> beforeSync,
                                 List<C7nSecret> c7nSecrets, List<V1Endpoints> v1Endpoints, Long envId, Long projectId, String path, Long userId) {
        List<String> beforSecret = beforeSync.stream()
                .filter(devopsEnvFileResourceE -> devopsEnvFileResourceE.getResourceType().equals(SECRET))
                .map(devopsEnvFileResourceE -> {
                    DevopsSecretE devopsSecretE = devopsSecretRepository
                            .queryBySecretId(devopsEnvFileResourceE.getResourceId());
                    if (devopsSecretE == null) {
                        devopsEnvFileResourceRepository
                                .deleteByEnvIdAndResource(envId, devopsEnvFileResourceE.getResourceId(), SECRET);
                        return null;
                    }
                    return devopsSecretE.getName();
                }).collect(Collectors.toList());
        // 比较已存在的秘钥和新增要处理的秘钥,获取新增秘钥，更新秘钥，删除秘钥
        List<C7nSecret> addC7nSecret = new ArrayList<>();
        List<C7nSecret> updateC7nSecret = new ArrayList<>();
        c7nSecrets.forEach(v1Secret -> {
            if (beforSecret.contains(v1Secret.getMetadata().getName())) {
                updateC7nSecret.add(v1Secret);
                beforSecret.remove(v1Secret.getMetadata().getName());
            } else {
                addC7nSecret.add(v1Secret);
            }
        });
        //删除secret,删除文件对象关联关系
        beforSecret.forEach(secretName -> {
            DevopsSecretE devopsSecretE = devopsSecretRepository.selectByEnvIdAndName(envId, secretName);
            if (devopsSecretE != null) {
                DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository.query(devopsSecretE.getCommandId());
                if (!devopsEnvCommandE.getCommandType().equals(CommandType.DELETE.getType())) {
                    DevopsEnvCommandE devopsEnvCommandE1 = new DevopsEnvCommandE();
                    devopsEnvCommandE1.setCommandType(CommandType.DELETE.getType());
                    devopsEnvCommandE1.setObject(ObjectType.SECRET.getType());
                    devopsEnvCommandE1.setCreatedBy(userId);
                    devopsEnvCommandE1.setStatus(CommandStatus.OPERATING.getStatus());
                    devopsEnvCommandE1.setObjectId(devopsSecretE.getId());
                    DevopsSecretE devopsSecretE1 = devopsSecretRepository.queryBySecretId(devopsSecretE.getId());
                    devopsSecretE1.setCommandId(devopsEnvCommandRepository.create(devopsEnvCommandE1).getId());
                    devopsSecretRepository.update(devopsSecretE);
                }
                devopsSecretService.deleteSecretByGitOps(devopsSecretE.getId());
                devopsEnvFileResourceRepository.deleteByEnvIdAndResource(envId, devopsSecretE.getId(), SECRET);
            }
        });

        //新增secret
        addSecret(objectPath, envId, addC7nSecret, path, userId);
        //更新secret
        updateSecret(objectPath, envId, projectId, updateC7nSecret, path, userId);
    }

    private void addSecret(Map<String, String> objectPath, Long envId,
                           List<C7nSecret> addC7nSecret, String path, Long userId) {
        addC7nSecret.forEach(c7nSecret -> {
            String filePath = "";
            try {
                filePath = objectPath.get(TypeUtil.objToString(c7nSecret.hashCode()));

                checkSecretName(c7nSecret);
                DevopsSecretE devopsSecretE = devopsSecretRepository
                        .selectByEnvIdAndName(envId, c7nSecret.getMetadata().getName());
                SecretReqDTO secretReqDTO;
                // 初始化secret对象参数，存在secret则直接创建文件对象关联关系
                if (devopsSecretE == null) {
                    secretReqDTO = getSecretReqDTO(c7nSecret, envId, CREATE);
                    devopsSecretService.addSecretByGitOps(secretReqDTO, userId);
                    devopsSecretE = devopsSecretRepository.selectByEnvIdAndName(envId, secretReqDTO.getName());
                }
                DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository
                        .query(devopsSecretE.getCommandId());
                if (devopsEnvCommandE == null) {
                    devopsEnvCommandE = createDevopsEnvCommandE(CREATE);
                    devopsEnvCommandE.setObjectId(devopsSecretE.getId());
                    DevopsSecretE devopsSecretE1 = devopsSecretRepository.queryBySecretId(devopsSecretE.getId());
                    devopsSecretE1.setCommandId(devopsEnvCommandE.getId());
                    devopsSecretRepository.update(devopsSecretE1);
                }
                devopsEnvCommandE.setSha(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
                devopsEnvCommandRepository.update(devopsEnvCommandE);
                DevopsEnvFileResourceE devopsEnvFileResourceE = new DevopsEnvFileResourceE();
                devopsEnvFileResourceE.setEnvironment(new DevopsEnvironmentE(envId));
                devopsEnvFileResourceE.setFilePath(objectPath.get(TypeUtil.objToString(c7nSecret.hashCode())));
                devopsEnvFileResourceE.setResourceId(devopsSecretE.getId());
                devopsEnvFileResourceE.setResourceType(SECRET);
                devopsEnvFileResourceRepository.createFileResource(devopsEnvFileResourceE);
            } catch (CommonException e) {
                String errorCode = "";
                if (e instanceof GitOpsExplainException) {
                    errorCode = ((GitOpsExplainException) e)
                            .getErrorCode() == null ? "" : ((GitOpsExplainException) e).getErrorCode();
                }
                throw new GitOpsExplainException(e.getMessage(), filePath, errorCode, e);
            }
        });
    }

    private void updateSecret(Map<String, String> objectPath, Long envId, Long projectId,
                              List<C7nSecret> updateC7nSecret, String path, Long userId) {
        updateC7nSecret.forEach(c7nSecret -> {
            String filePath = "";
            try {
                boolean isNotChange = false;
                filePath = objectPath.get(TypeUtil.objToString(c7nSecret.hashCode()));
                DevopsSecretE devopsSecretE = devopsSecretRepository
                        .selectByEnvIdAndName(envId, c7nSecret.getMetadata().getName());
                checkSecretName(c7nSecret);
                // 初始化secret对象参数,更新secret并更新文件对象关联关系
                SecretReqDTO secretReqDTO = getSecretReqDTO(c7nSecret, envId, "update");
                secretReqDTO.setId(devopsSecretE.getId());
                if (secretReqDTO.equals(ConvertHelper.convert(devopsSecretE, SecretReqDTO.class))) {
                    isNotChange = true;
                }

                DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository.query(devopsSecretE.getCommandId());
                if (!isNotChange) {
                    devopsSecretService
                            .updateDevopsSecretByGitOps(projectId, devopsSecretE.getId(), secretReqDTO, userId);
                    DevopsSecretE newSecretE = devopsSecretRepository
                            .selectByEnvIdAndName(envId, c7nSecret.getMetadata().getName());
                    devopsEnvCommandE = devopsEnvCommandRepository.query(newSecretE.getCommandId());
                }
                if (devopsEnvCommandE == null) {
                    devopsEnvCommandE = createDevopsEnvCommandE("update");
                    devopsEnvCommandE.setObjectId(devopsSecretE.getId());
                    DevopsSecretE devopsSecretE1 = devopsSecretRepository.queryBySecretId(devopsSecretE.getId());
                    devopsSecretE1.setCommandId(devopsEnvCommandE.getId());
                    devopsSecretRepository.update(devopsSecretE1);
                }
                devopsEnvCommandE.setSha(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
                devopsEnvCommandRepository.update(devopsEnvCommandE);
                DevopsEnvFileResourceE devopsEnvFileResourceE = devopsEnvFileResourceRepository
                        .queryByEnvIdAndResource(envId, devopsSecretE.getId(), c7nSecret.getKind());
                devopsEnvFileResourceService.updateOrCreateFileResource(objectPath, envId, devopsEnvFileResourceE,
                        c7nSecret.hashCode(), devopsSecretE.getId(), c7nSecret.getKind());
            } catch (CommonException e) {
                String errorCode = "";
                if (e instanceof GitOpsExplainException) {
                    errorCode = ((GitOpsExplainException) e)
                            .getErrorCode() == null ? "" : ((GitOpsExplainException) e).getErrorCode();
                }
                throw new GitOpsExplainException(e.getMessage(), filePath, errorCode, e);
            }
        });
    }

    private void checkSecretName(C7nSecret v1Secret) {
        try {
            DevopsSecretValidator.checkName(v1Secret.getMetadata().getName());
        } catch (Exception e) {
            throw new CommonException(e.getMessage(), e);
        }
    }

    private SecretReqDTO getSecretReqDTO(C7nSecret c7nSecret, Long envId, String type) {
        SecretReqDTO secretReqDTO = new SecretReqDTO();
        secretReqDTO.setName(c7nSecret.getMetadata().getName());
        secretReqDTO.setDescription("");
        secretReqDTO.setType(type);
        secretReqDTO.setEnvId(envId);
        secretReqDTO.setValue(c7nSecret.getStringData());
        return secretReqDTO;
    }

    private DevopsEnvCommandE createDevopsEnvCommandE(String type) {
        DevopsEnvCommandE devopsEnvCommandE = new DevopsEnvCommandE();
        if (type.equals(CREATE)) {
            devopsEnvCommandE.setCommandType(CommandType.CREATE.getType());
        } else {
            devopsEnvCommandE.setCommandType(CommandType.UPDATE.getType());
        }
        devopsEnvCommandE.setObject(ObjectType.SERVICE.getType());
        devopsEnvCommandE.setStatus(CommandStatus.OPERATING.getStatus());
        return devopsEnvCommandRepository.create(devopsEnvCommandE);
    }
}
