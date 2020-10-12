package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import io.kubernetes.client.models.V1Endpoints;
import io.kubernetes.client.models.V1Secret;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.validator.DevopsSecretValidator;
import io.choerodon.devops.api.vo.SecretReqVO;
import io.choerodon.devops.app.service.DevopsEnvCommandService;
import io.choerodon.devops.app.service.DevopsEnvFileResourceService;
import io.choerodon.devops.app.service.DevopsSecretService;
import io.choerodon.devops.app.service.HandlerObjectFileRelationsService;
import io.choerodon.devops.infra.dto.DevopsEnvCommandDTO;
import io.choerodon.devops.infra.dto.DevopsEnvFileResourceDTO;
import io.choerodon.devops.infra.dto.DevopsSecretDTO;
import io.choerodon.devops.infra.exception.GitOpsExplainException;
import io.choerodon.devops.infra.util.Base64Util;
import io.choerodon.devops.infra.util.GitUtil;
import io.choerodon.devops.infra.util.TypeUtil;

/**
 * Created by n!Ck
 * Date: 18-12-4
 * Time: 下午8:00
 * Description:
 */

@Service
public class HandlerC7nSecretServiceImpl implements HandlerObjectFileRelationsService<V1Secret> {

    private static final Logger logger = LoggerFactory.getLogger(HandlerC7nSecretServiceImpl.class);


    private static final String CREATE = "create";

    private static final String SECRET = "Secret";
    private static final String GIT_SUFFIX = "/.git";
    private static final String DOCKER_REGISTRY_SECRET_TYPE = "kubernetes.io/dockerconfigjson";

    @Autowired
    private DevopsEnvCommandService devopsEnvCommandService;
    @Autowired
    private DevopsSecretService devopsSecretService;
    @Autowired
    private DevopsEnvFileResourceService devopsEnvFileResourceService;


    @Override
    public void handlerRelations(Map<String, String> objectPath, List<DevopsEnvFileResourceDTO> beforeSync,
                                 List<V1Secret> v1Secrets, List<V1Endpoints> v1Endpoints, Long envId, Long projectId, String path, Long userId) {
        List<String> beforSecret = beforeSync.stream()
                .filter(devopsEnvFileResourceE -> devopsEnvFileResourceE.getResourceType().equals(SECRET))
                .map(devopsEnvFileResourceE -> {
                    DevopsSecretDTO devopsSecretDTO = devopsSecretService
                            .baseQuery(devopsEnvFileResourceE.getResourceId());
                    if (devopsSecretDTO == null) {
                        devopsEnvFileResourceService
                                .baseDeleteByEnvIdAndResourceId(envId, devopsEnvFileResourceE.getResourceId(), SECRET);
                        return null;
                    }
                    return devopsSecretDTO.getName();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        // 比较已存在的秘钥和新增要处理的秘钥,获取新增秘钥，更新秘钥，删除秘钥
        List<V1Secret> addC7nSecret = new ArrayList<>();
        List<V1Secret> updateC7nSecret = new ArrayList<>();
        v1Secrets.forEach(v1Secret -> {
            if (beforSecret.contains(v1Secret.getMetadata().getName())) {
                updateC7nSecret.add(v1Secret);
                beforSecret.remove(v1Secret.getMetadata().getName());
            } else {
                addC7nSecret.add(v1Secret);
            }
        });
        //删除secret,删除文件对象关联关系
        beforSecret.forEach(secretName -> {
            DevopsSecretDTO devopsSecretDTO = devopsSecretService.baseQueryByEnvIdAndName(envId, secretName);
            if (devopsSecretDTO != null) {
                devopsSecretService.deleteSecretByGitOps(devopsSecretDTO.getId());
                devopsEnvFileResourceService.baseDeleteByEnvIdAndResourceId(envId, devopsSecretDTO.getId(), SECRET);
            }
        });

        //新增secret
        addSecret(objectPath, envId, addC7nSecret, path, userId);
        //更新secret
        updateSecret(objectPath, envId, projectId, updateC7nSecret, path, userId);
    }

    @Override
    public Class<V1Secret> getTarget() {
        return V1Secret.class;
    }

    private void addSecret(Map<String, String> objectPath, Long envId,
                           List<V1Secret> addSecret, String path, Long userId) {
        addSecret.forEach(c7nSecret -> {
            String filePath = "";
            try {
                filePath = objectPath.get(TypeUtil.objToString(c7nSecret.hashCode()));

                checkSecretName(c7nSecret);
                DevopsSecretDTO devopsSecretDTO = devopsSecretService
                        .baseQueryByEnvIdAndName(envId, c7nSecret.getMetadata().getName());
                SecretReqVO secretReqVO;
                // 初始化secret对象参数，存在secret则直接创建文件对象关联关系
                if (devopsSecretDTO == null) {
                    secretReqVO = getSecretReqDTO(c7nSecret, envId, CREATE);
                    devopsSecretService.addSecretByGitOps(secretReqVO, userId);
                    devopsSecretDTO = devopsSecretService.baseQueryByEnvIdAndName(envId, secretReqVO.getName());
                }
                DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService
                        .baseQuery(devopsSecretDTO.getCommandId());

                devopsEnvCommandDTO.setSha(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
                devopsEnvCommandService.baseUpdateSha(devopsEnvCommandDTO.getId(), devopsEnvCommandDTO.getSha());

                devopsEnvFileResourceService.updateOrCreateFileResource(objectPath, envId, null, c7nSecret.hashCode(), devopsSecretDTO.getId(),
                        c7nSecret.getKind());
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
                              List<V1Secret> updateSecret, String path, Long userId) {
        updateSecret.forEach(c7nSecret -> {
            String filePath = "";
            try {
                boolean isNotChange = false;
                filePath = objectPath.get(TypeUtil.objToString(c7nSecret.hashCode()));
                DevopsSecretDTO devopsSecretDTO = devopsSecretService
                        .baseQueryByEnvIdAndName(envId, c7nSecret.getMetadata().getName());
                checkSecretName(c7nSecret);
                // 初始化secret对象参数,更新secret并更新文件对象关联关系
                SecretReqVO secretReqVO = getSecretReqDTO(c7nSecret, envId, "update");
                secretReqVO.setId(devopsSecretDTO.getId());
                if (secretReqVO.getValue().equals(devopsSecretService.dtoToReqVo(devopsSecretDTO).getValue())) {
                    isNotChange = true;
                }

                DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(devopsSecretDTO.getCommandId());
                if (!isNotChange) {
                    devopsSecretService
                            .updateDevopsSecretByGitOps(projectId, devopsSecretDTO.getId(), secretReqVO, userId);
                    DevopsSecretDTO newSecretDTO = devopsSecretService
                            .baseQueryByEnvIdAndName(envId, c7nSecret.getMetadata().getName());
                    devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(newSecretDTO.getCommandId());
                }

                devopsEnvCommandDTO.setSha(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
                devopsEnvCommandService.baseUpdateSha(devopsEnvCommandDTO.getId(), devopsEnvCommandDTO.getSha());
                DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService
                        .baseQueryByEnvIdAndResourceId(envId, devopsSecretDTO.getId(), c7nSecret.getKind());
                devopsEnvFileResourceService.updateOrCreateFileResource(objectPath, envId, devopsEnvFileResourceDTO,
                        c7nSecret.hashCode(), devopsSecretDTO.getId(), c7nSecret.getKind());
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

    private void checkSecretName(V1Secret v1Secret) {
        try {
            DevopsSecretValidator.checkName(v1Secret.getMetadata().getName());
        } catch (Exception e) {
            throw new CommonException(e.getMessage(), e);
        }
    }

    private SecretReqVO getSecretReqDTO(V1Secret c7nSecret, Long envId, String type) {
        SecretReqVO secretReqVO = new SecretReqVO();
        secretReqVO.setName(c7nSecret.getMetadata().getName());
        secretReqVO.setDescription("");
        secretReqVO.setType(type);
        secretReqVO.setEnvId(envId);
        //等待界面支持secret类型之后在区分开
        // 支持另外一种类型secret
        // 这两种其实是一样的处理方式, 都从data里面取数据(2020/10/10)
        if (DOCKER_REGISTRY_SECRET_TYPE.equals(c7nSecret.getType())) {
            Map<String, String> map = new HashMap<>();
            c7nSecret.getData().forEach(map::put);
            secretReqVO.setValue(map);
        } else {
            secretReqVO.setValue(mergeSecretData(c7nSecret.getData(), c7nSecret.getStringData()));
        }
        return secretReqVO;
    }

    private Map<String, String> mergeSecretData(Map<String, String> data, Map<String, String> stringData) {
        // k8s处理stringData和data的逻辑是将stringData的value进行base64加密后, 与data合并,
        // 如果键相同, stringData覆盖data的值
        Map<String, String> converted = new LinkedHashMap<>();
        // 加密stringData的数据
        if (stringData != null) {
            stringData.forEach((k, v) -> converted.put(k, Base64Util.getBase64EncodedString(v)));
        }
        if (data != null) {
            data.forEach(converted::putIfAbsent);
        }
        return converted;
    }
}
