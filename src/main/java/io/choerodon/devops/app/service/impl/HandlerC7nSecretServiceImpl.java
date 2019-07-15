package io.choerodon.devops.app.service.impl;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.SecretReqDTO;
import io.choerodon.devops.api.validator.DevopsSecretValidator;
import io.choerodon.devops.app.service.DevopsEnvFileResourceService;
import io.choerodon.devops.app.service.DevopsSecretService;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvCommandVO;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvFileResourceVO;
import io.choerodon.devops.api.vo.iam.entity.DevopsSecretE;
import io.choerodon.devops.infra.exception.GitOpsExplainException;
import io.choerodon.devops.domain.application.repository.DevopsEnvCommandRepository;
import io.choerodon.devops.domain.application.repository.DevopsEnvFileResourceRepository;
import io.choerodon.devops.domain.application.repository.DevopsSecretRepository;
import io.choerodon.devops.app.service.HandlerObjectFileRelationsService;
import io.choerodon.devops.infra.util.GitUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.devops.infra.enums.CommandStatus;
import io.choerodon.devops.infra.enums.CommandType;
import io.choerodon.devops.infra.enums.ObjectType;
import io.kubernetes.client.models.V1Endpoints;
import io.kubernetes.client.models.V1Secret;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public void handlerRelations(Map<String, String> objectPath, List<DevopsEnvFileResourceVO> beforeSync,
                                 List<V1Secret> v1Secrets, List<V1Endpoints> v1Endpoints, Long envId, Long projectId, String path, Long userId) {
        List<String> beforSecret = beforeSync.stream()
                .filter(devopsEnvFileResourceE -> devopsEnvFileResourceE.getResourceType().equals(SECRET))
                .map(devopsEnvFileResourceE -> {
                    DevopsSecretE devopsSecretE = devopsSecretRepository
                            .queryBySecretId(devopsEnvFileResourceE.getResourceId());
                    if (devopsSecretE == null) {
                        devopsEnvFileResourceRepository
                                .baseDeleteByEnvIdAndResourceId(envId, devopsEnvFileResourceE.getResourceId(), SECRET);
                        return null;
                    }
                    return devopsSecretE.getName();
                }).collect(Collectors.toList());
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
            DevopsSecretE devopsSecretE = devopsSecretRepository.selectByEnvIdAndName(envId, secretName);
            if (devopsSecretE != null) {
                devopsSecretService.deleteSecretByGitOps(devopsSecretE.getId());
                devopsEnvFileResourceRepository.baseDeleteByEnvIdAndResourceId(envId, devopsSecretE.getId(), SECRET);
            }
        });

        //新增secret
        addSecret(objectPath, envId, addC7nSecret, path, userId);
        //更新secret
        updateSecret(objectPath, envId, projectId, updateC7nSecret, path, userId);
    }

    private void addSecret(Map<String, String> objectPath, Long envId,
                           List<V1Secret> addSecret, String path, Long userId) {
        addSecret.forEach(c7nSecret -> {
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
                DevopsEnvCommandVO devopsEnvCommandE = devopsEnvCommandRepository
                        .query(devopsSecretE.getCommandId());

                devopsEnvCommandE.setSha(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
                devopsEnvCommandRepository.update(devopsEnvCommandE);

                devopsEnvFileResourceService.updateOrCreateFileResource(objectPath, envId, null, c7nSecret.hashCode(), devopsSecretE.getId(),
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
                DevopsSecretE devopsSecretE = devopsSecretRepository
                        .selectByEnvIdAndName(envId, c7nSecret.getMetadata().getName());
                checkSecretName(c7nSecret);
                // 初始化secret对象参数,更新secret并更新文件对象关联关系
                SecretReqDTO secretReqDTO = getSecretReqDTO(c7nSecret, envId, "update");
                secretReqDTO.setId(devopsSecretE.getId());
                if (secretReqDTO.equals(ConvertHelper.convert(devopsSecretE, SecretReqDTO.class))) {
                    isNotChange = true;
                }

                DevopsEnvCommandVO devopsEnvCommandE = devopsEnvCommandRepository.query(devopsSecretE.getCommandId());
                if (!isNotChange) {
                    devopsSecretService
                            .updateDevopsSecretByGitOps(projectId, devopsSecretE.getId(), secretReqDTO, userId);
                    DevopsSecretE newSecretE = devopsSecretRepository
                            .selectByEnvIdAndName(envId, c7nSecret.getMetadata().getName());
                    devopsEnvCommandE = devopsEnvCommandRepository.query(newSecretE.getCommandId());
                }

                devopsEnvCommandE.setSha(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
                devopsEnvCommandRepository.update(devopsEnvCommandE);
                DevopsEnvFileResourceVO devopsEnvFileResourceE = devopsEnvFileResourceRepository
                        .baseQueryByEnvIdAndResourceId(envId, devopsSecretE.getId(), c7nSecret.getKind());
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

    private void checkSecretName(V1Secret v1Secret) {
        try {
            DevopsSecretValidator.checkName(v1Secret.getMetadata().getName());
        } catch (Exception e) {
            throw new CommonException(e.getMessage(), e);
        }
    }

    private SecretReqDTO getSecretReqDTO(V1Secret c7nSecret, Long envId, String type) {
        SecretReqDTO secretReqDTO = new SecretReqDTO();
        secretReqDTO.setName(c7nSecret.getMetadata().getName());
        secretReqDTO.setDescription("");
        secretReqDTO.setType(type);
        secretReqDTO.setEnvId(envId);
        //等待界面支持secret类型之后在区分开
        if(c7nSecret.getType().equals("kubernetes.io/dockerconfigjson")) {
            Map<String,String> map =  new HashMap<>();
            c7nSecret.getData().forEach((key,value)-> {
                try {
                    map.put(key,new String(value,"utf-8"));
                    secretReqDTO.setValue(map);
                } catch (UnsupportedEncodingException e) {
                    logger.info(e.getMessage());
                }
            });
        }else {
            secretReqDTO.setValue(c7nSecret.getStringData());
        }
        return secretReqDTO;
    }

    private DevopsEnvCommandVO createDevopsEnvCommandE(String type) {
        DevopsEnvCommandVO devopsEnvCommandE = new DevopsEnvCommandVO();
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
