package io.choerodon.devops.app.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Secret;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.validator.DevopsSecretValidator;
import io.choerodon.devops.api.vo.SecretReqVO;
import io.choerodon.devops.api.vo.SecretRespVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.enums.CommandStatus;
import io.choerodon.devops.infra.enums.ObjectType;
import io.choerodon.devops.infra.enums.SecretStatus;
import io.choerodon.devops.infra.enums.SendSettingEnum;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.gitops.ResourceConvertToYamlHandler;
import io.choerodon.devops.infra.gitops.ResourceFileCheckHandler;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.DevopsSecretMapper;
import io.choerodon.devops.infra.util.*;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;


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
    private static final String DOCKER_CONFIG_JSON = ".dockerconfigjson";
    private static final String MASTER = "master";

    private final Gson gson = new Gson();

    @Autowired
    private ClusterConnectionHandler clusterConnectionHandler;
    @Autowired
    private DevopsEnvCommandService devopsEnvCommandService;
    @Autowired
    private UserAttrService userAttrService;
    @Autowired
    private DevopsEnvFileResourceService devopsEnvFileResourceService;
    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator;
    @Autowired
    private ResourceFileCheckHandler resourceFileCheckHandler;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private DevopsSecretMapper devopsSecretMapper;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private SendNotificationService sendNotificationService;
    @Autowired
    private PermissionHelper permissionHelper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SecretRespVO createOrUpdate(Long projectId, SecretReqVO secretReqVO) {
        if (secretReqVO.getValue() == null || secretReqVO.getValue().size() == 0) {
            throw new CommonException("devops.secret.value.is.null");
        }
        DevopsEnvironmentDTO devopsEnvironmentDTO = permissionHelper.checkEnvBelongToProject(projectId, secretReqVO.getEnvId());
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);
        // 处理secret对象
        DevopsSecretDTO devopsSecretDTO = handleSecret(secretReqVO, false);
        // 初始化V1Secret对象
        V1Secret v1Secret = initV1Secret(devopsSecretDTO);
        // 更新操作如果key-value没有改变
        if (UPDATE.equals(secretReqVO.getType())) {
            //更新secret的时候校验gitops库文件是否存在,处理部署secret时，由于没有创gitops文件导致的部署失败
            resourceFileCheckHandler.check(devopsEnvironmentDTO, secretReqVO.getId(), secretReqVO.getName(), SECRET);

            DevopsSecretDTO oldSecretDTO = baseQueryByEnvIdAndName(secretReqVO.getEnvId(), secretReqVO.getName());
            Map<String, String> secretMaps = gson.fromJson(oldSecretDTO.getValue(), new TypeToken<Map<String, String>>() {
            }.getType());
            oldSecretDTO.setValueMap(secretMaps);
            Map<String, String> oldMap = new HashMap<>();
            for (Map.Entry<String, String> e : oldSecretDTO.getValueMap().entrySet()) {
                oldMap.put(e.getKey(), Base64Util.getBase64DecodedString(e.getValue()));
            }
            if (oldMap.equals(secretReqVO.getValue())) {
                baseUpdate(devopsSecretDTO);
                return dtoToVO(devopsSecretDTO, true);
            }
        }
        DevopsEnvCommandDTO devopsEnvCommandE = initDevopsEnvCommandDTO(secretReqVO.getType());

        // 在gitops库处理secret文件
        operateEnvGitLabFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), v1Secret, devopsSecretDTO,
                devopsEnvCommandE, CREATE.equals(secretReqVO.getType()), userAttrDTO, secretReqVO.getAppServiceId());
        SecretRespVO secretRespVO = ConvertUtils.convertObject(devopsSecretDTO.getId(), SecretRespVO.class);
        if (devopsSecretDTO.getCreatedBy() != null && devopsSecretDTO.getCreatedBy() != 0) {
            secretRespVO.setCreatorName(ResourceCreatorInfoUtil.getOperatorName(baseServiceClientOperator, devopsSecretDTO.getCreatedBy()));
        }
        if (devopsSecretDTO.getLastUpdatedBy() != null && devopsSecretDTO.getLastUpdatedBy() != 0) {
            secretRespVO.setLastUpdaterName(ResourceCreatorInfoUtil.getOperatorName(baseServiceClientOperator, devopsSecretDTO.getLastUpdatedBy()));
        }
        return secretRespVO;
    }

    /**
     * 从GitOps来的VO本身值已经加密过了，不需要再进行加密
     *
     * @param secretReqVO  VO
     * @param isFromGitOps 是从GitOps逻辑调用
     * @return DTO
     */
    private DevopsSecretDTO voToDto(SecretReqVO secretReqVO, boolean isFromGitOps) {
        DevopsSecretDTO devopsSecretDTO = new DevopsSecretDTO();
        BeanUtils.copyProperties(secretReqVO, devopsSecretDTO);
        // 从GitOps库来的, 应该是加密的, 不需要再进行加密了
        if (isFromGitOps) {
            devopsSecretDTO.setValue(gson.toJson(secretReqVO.getValue()));
            devopsSecretDTO.setValueMap(secretReqVO.getValue());
            return devopsSecretDTO;
        }
        Map<String, String> encodedSecretMaps = new HashMap<>();
        if (!secretReqVO.getValue().isEmpty()) {
            for (Map.Entry<String, String> e : secretReqVO.getValue().entrySet()) {
                if (!e.getKey().equals(DOCKER_CONFIG_JSON)) {
                    encodedSecretMaps.put(e.getKey(), Base64Util.getBase64EncodedString(e.getValue()));
                } else {
                    encodedSecretMaps.put(e.getKey(), e.getValue());
                }
            }
            devopsSecretDTO.setValue(gson.toJson(encodedSecretMaps));
        }
        devopsSecretDTO.setValueMap(encodedSecretMaps);
        return devopsSecretDTO;
    }

    @Override
    public SecretReqVO dtoToReqVo(DevopsSecretDTO devopsSecretDTO) {
        SecretReqVO secretReqVO = new SecretReqVO();
        BeanUtils.copyProperties(devopsSecretDTO, secretReqVO);
        Map<String, String> secretMaps = gson
                .fromJson(devopsSecretDTO.getValue(), new TypeToken<Map<String, String>>() {
                }.getType());
        secretMaps.forEach((key, value) -> secretMaps.put(key, Base64Util.getBase64DecodedString(value)));
        secretReqVO.setValue(secretMaps);
        return secretReqVO;
    }

    /**
     * 处理secret vo 到 dto
     *
     * @param secretReqVO  VO
     * @param isFromGitOps 是否从GitOps调用
     * @return dto
     */
    private DevopsSecretDTO handleSecret(SecretReqVO secretReqVO, boolean isFromGitOps) {
        if (CREATE.equals(secretReqVO.getType())) {
            // 校验secret名字合法性和环境下唯一性
            DevopsSecretValidator.checkName(secretReqVO.getName());
            baseCheckName(secretReqVO.getName(), secretReqVO.getEnvId());
        }
        // 校验key-name
        if (!secretReqVO.getType().equals("kubernetes.io/dockerconfigjson")) {
            DevopsSecretValidator.checkKeyName(secretReqVO.getValue().keySet());
        }

        DevopsSecretDTO devopsSecretDTO = voToDto(secretReqVO, isFromGitOps);

        devopsSecretDTO.setCommandStatus(SecretStatus.OPERATING.getStatus());

        return devopsSecretDTO;
    }


    private static V1Secret initV1Secret(DevopsSecretDTO devopsSecretDTO) {
        V1Secret secret = new V1Secret();
        secret.setApiVersion("v1");
        secret.setKind(SECRET);
        V1ObjectMeta metadata = new V1ObjectMeta();
        metadata.setName(devopsSecretDTO.getName());
        secret.setMetadata(metadata);
        secret.setType("Opaque");
        Map<String, byte[]> data = new HashMap<>();
        devopsSecretDTO.getValueMap().forEach((k, v) -> {
            data.put(k, v.getBytes());
        });
        secret.setData(data);
        return secret;
    }

    private void operateEnvGitLabFile(Integer gitlabEnvGroupProjectId, V1Secret v1Secret, DevopsSecretDTO devopsSecretDTO,
                                      DevopsEnvCommandDTO devopsEnvCommandDTO, Boolean isCreate, UserAttrDTO userAttrDTO, Long appServiceId) {

        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsSecretDTO.getEnvId());

        //操作secret数据库
        if (isCreate) {
            Long secretId = baseCreate(devopsSecretDTO).getId();
            //创建应用资源关系
            if (appServiceId != null) {
                devopsSecretDTO.setAppServiceId(appServiceId);
            }
            devopsEnvCommandDTO.setObjectId(secretId);
            devopsSecretDTO.setId(secretId);
            devopsSecretDTO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());
            baseUpdate(devopsSecretDTO);
        } else {
            devopsEnvCommandDTO.setObjectId(devopsSecretDTO.getId());
            devopsSecretDTO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());
            baseUpdate(devopsSecretDTO);
        }

        // 判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
        String path = clusterConnectionHandler.handDevopsEnvGitRepository(devopsEnvironmentDTO.getProjectId(), devopsEnvironmentDTO.getCode(), devopsEnvironmentDTO.getId(), devopsEnvironmentDTO.getEnvIdRsa(), devopsEnvironmentDTO.getType(), devopsEnvironmentDTO.getClusterCode());

        ResourceConvertToYamlHandler<V1Secret> resourceConvertToYamlHandler = new ResourceConvertToYamlHandler<>();
        resourceConvertToYamlHandler.setType(v1Secret);
        resourceConvertToYamlHandler.operationEnvGitlabFile("sct-" + devopsSecretDTO.getName(), gitlabEnvGroupProjectId,
                isCreate ? CREATE : UPDATE, userAttrDTO.getGitlabUserId(), devopsSecretDTO.getId(), SECRET, null, false,
                devopsSecretDTO.getEnvId(), path);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteSecret(Long projectId, Long envId, Long secretId) {
        DevopsSecretDTO devopsSecretDTO = baseQuery(secretId);

        if (devopsSecretDTO == null) {
            return false;
        }

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        DevopsEnvironmentDTO devopsEnvironmentDTO = permissionHelper.checkEnvBelongToProject(projectId, envId);

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);

        DevopsEnvCommandDTO devopsEnvCommandDTO = initDevopsEnvCommandDTO(DELETE);

        // 更新secret
        devopsEnvCommandDTO.setObjectId(secretId);
        devopsSecretDTO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());
        baseUpdate(devopsSecretDTO);

        //判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
        String path = clusterConnectionHandler.handDevopsEnvGitRepository(devopsEnvironmentDTO.getProjectId(), devopsEnvironmentDTO.getCode(), devopsEnvironmentDTO.getId(), devopsEnvironmentDTO.getEnvIdRsa(), devopsEnvironmentDTO.getType(), devopsEnvironmentDTO.getClusterCode());

        // 查询该对象所在文件中是否含有其它对象
        DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService
                .baseQueryByEnvIdAndResourceId(devopsEnvironmentDTO.getId(), secretId, SECRET);
        if (devopsEnvFileResourceDTO == null) {
            baseDelete(secretId);
            if (gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), MASTER,
                    "sct-" + devopsSecretDTO.getName() + ".yaml")) {
                gitlabServiceClientOperator.deleteFile(
                        TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()),
                        "sct-" + devopsSecretDTO.getName() + ".yaml",
                        String.format("delete: %s", "sct-" + devopsSecretDTO.getName() + ".yaml"),
                        TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()), MASTER);
            }
            return true;
        } else {
            if (!gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), MASTER,
                    devopsEnvFileResourceDTO.getFilePath())) {
                baseDelete(secretId);
                devopsEnvFileResourceService.baseDeleteById(devopsEnvFileResourceDTO.getId());
                return true;
            }
        }
        List<DevopsEnvFileResourceDTO> devopsEnvFileResourceDTOS = devopsEnvFileResourceService
                .baseQueryByEnvIdAndPath(devopsEnvironmentDTO.getId(), devopsEnvFileResourceDTO.getFilePath());

        // 如果对象所在文件只有一个对象，则直接删除文件,否则把对象从文件中去掉，更新文件
        if (devopsEnvFileResourceDTOS.size() == 1) {
            if (gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), MASTER,
                    devopsEnvFileResourceDTO.getFilePath())) {
                gitlabServiceClientOperator.deleteFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()),
                        devopsEnvFileResourceDTO.getFilePath(), String.format("delete: %s", devopsEnvFileResourceDTO.getFilePath()),
                        TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()), "master");
            }
        } else {
            ResourceConvertToYamlHandler<V1Secret> resourceConvertToYamlHandler = new ResourceConvertToYamlHandler<>();
            V1Secret v1Secret = new V1Secret();
            V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
            v1ObjectMeta.setName(devopsSecretDTO.getName());
            v1Secret.setMetadata(v1ObjectMeta);
            resourceConvertToYamlHandler.setType(v1Secret);
            Integer gitlabEnvProjectId = TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId());
            resourceConvertToYamlHandler.operationEnvGitlabFile(null, gitlabEnvProjectId, DELETE, userAttrDTO.getGitlabUserId(), secretId,
                    SECRET, null, false, devopsEnvironmentDTO.getId(), path);
        }
        //删除成功发送web hook json
        sendNotificationService.sendWhenSecret(devopsSecretDTO, SendSettingEnum.DELETE_RESOURCE.value());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSecretByGitOps(Long secretId) {
        DevopsSecretDTO devopsSecretDTO = baseQuery(secretId);
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsSecretDTO.getEnvId());
        clusterConnectionHandler.checkEnvConnection(devopsEnvironmentDTO.getClusterId());
        devopsEnvCommandService.baseListByObject(ObjectType.SECRET.getType(), devopsSecretDTO.getId()).forEach(t -> devopsEnvCommandService.baseDeleteByEnvCommandId(t));
        baseDelete(secretId);
    }

    @Override
    public void addSecretByGitOps(SecretReqVO secretReqVO, Long userId) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(secretReqVO.getEnvId());
        //校验环境是否链接
        clusterConnectionHandler.checkEnvConnection(devopsEnvironmentDTO.getClusterId());
        // 处理secret对象
        DevopsSecretDTO devopsSecretDTO = handleSecret(secretReqVO, true);
        // 创建secret
        Long secretId = baseCreate(devopsSecretDTO).getId();
        DevopsEnvCommandDTO devopsEnvCommandDTO = new DevopsEnvCommandDTO();
        devopsEnvCommandDTO.setCommandType(CREATE);
        devopsEnvCommandDTO.setStatus(devopsSecretDTO.getCommandStatus());
        devopsEnvCommandDTO.setObjectId(secretId);
        devopsEnvCommandDTO.setObject(SECRET);
        devopsEnvCommandDTO.setCreatedBy(userId);
        devopsSecretDTO.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());
        devopsSecretDTO.setId(secretId);
        devopsSecretDTO.setEnvId(devopsEnvironmentDTO.getId());
        baseUpdate(devopsSecretDTO);
    }

    @Override
    public void updateDevopsSecretByGitOps(Long projectId, Long id, SecretReqVO secretReqVO, Long userId) {

        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(secretReqVO.getEnvId());
        //校验环境是否链接
        clusterConnectionHandler.checkEnvConnection(devopsEnvironmentDTO.getClusterId());

        DevopsSecretDTO oldSecretDTO = baseQueryByEnvIdAndName(secretReqVO.getEnvId(), secretReqVO.getName());
        oldSecretDTO.setValueMap(gson.fromJson(oldSecretDTO.getValue(), new TypeToken<Map<String, String>>() {
        }.getType()));
        // 利用加密后值比对是否相等，相等则返回
        if (oldSecretDTO.getValueMap().equals(secretReqVO.getValue())) {
            return;
        }
        // 处理secret对象
        DevopsSecretDTO devopsSecretE = handleSecret(secretReqVO, true);
        DevopsEnvCommandDTO devopsEnvCommandDTO = initDevopsEnvCommandDTO(UPDATE);
        // 更新secret对象
        devopsEnvCommandDTO.setObjectId(id);
        devopsEnvCommandDTO.setCreatedBy(userId);
        devopsSecretE.setCommandId(devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId());
        baseUpdate(devopsSecretE);
    }

    private DevopsEnvCommandDTO initDevopsEnvCommandDTO(String type) {
        DevopsEnvCommandDTO devopsEnvCommandDTO = new DevopsEnvCommandDTO();
        devopsEnvCommandDTO.setCommandType(type);
        devopsEnvCommandDTO.setObject(ObjectType.SECRET.getType());
        devopsEnvCommandDTO.setStatus(CommandStatus.OPERATING.getStatus());
        return devopsEnvCommandDTO;
    }

    @Override
    public Page<SecretRespVO> pageByOption(Long envId, PageRequest pageable, String params, Long appServiceId, boolean toDecode) {
        return ConvertUtils.convertPage(basePageByOption(envId, pageable, params, appServiceId), dto -> dtoToVO(dto, toDecode));
    }

    private SecretRespVO dtoToVO(DevopsSecretDTO devopsSecretDTO, boolean toDecode) {
        if (devopsSecretDTO == null) {
            return null;
        }
        SecretRespVO secretRespVO = new SecretRespVO();
        BeanUtils.copyProperties(devopsSecretDTO, secretRespVO);
        Map<String, String> secretMaps = gson.fromJson(devopsSecretDTO.getValue(), new TypeToken<Map<String, String>>() {
        }.getType());

        if (toDecode) {
            secretMaps.forEach((k, v) -> secretMaps.put(k, Base64Util.getBase64DecodedString(v)));
        }

        secretRespVO.setValue(secretMaps);
        if (devopsSecretDTO.getCreatedBy() != null && devopsSecretDTO.getCreatedBy() != 0) {
            secretRespVO.setCreatorName(ResourceCreatorInfoUtil.getOperatorName(baseServiceClientOperator, devopsSecretDTO.getCreatedBy()));
        }
        if (devopsSecretDTO.getLastUpdatedBy() != null && devopsSecretDTO.getLastUpdatedBy() != 0) {
            secretRespVO.setLastUpdaterName(ResourceCreatorInfoUtil.getOperatorName(baseServiceClientOperator, devopsSecretDTO.getLastUpdatedBy()));
        }
        return secretRespVO;
    }

    @Override
    public SecretRespVO querySecret(Long secretId, boolean toDecode) {
        return dtoToVO(baseQuery(secretId), toDecode);
    }

    @Override
    public boolean checkName(Long envId, String name) {
        return DevopsSecretValidator.isNameValid(name) && isNameUnique(envId, name);
    }

    @Override
    public boolean isNameUnique(Long envId, String name) {
        DevopsSecretDTO devopsSecretDTO = new DevopsSecretDTO();
        devopsSecretDTO.setName(name);
        devopsSecretDTO.setEnvId(envId);
        return devopsSecretMapper.selectCount(devopsSecretDTO) == 0;
    }

    @Override
    public DevopsSecretDTO baseCreate(DevopsSecretDTO devopsSecretDTO) {
        if (devopsSecretMapper.insert(devopsSecretDTO) != 1) {
            throw new CommonException("devops.secret.insert");
        }
        return devopsSecretDTO;
    }

    @Override
    public void baseUpdate(DevopsSecretDTO devopsSecretDTO) {
        DevopsSecretDTO oldDevopsSecretDTO = devopsSecretMapper.selectByPrimaryKey(devopsSecretDTO.getId());
        if (oldDevopsSecretDTO == null) {
            throw new CommonException("secret.not.exists");
        }
        devopsSecretDTO.setObjectVersionNumber(oldDevopsSecretDTO.getObjectVersionNumber());
        if (devopsSecretMapper.updateByPrimaryKeySelective(devopsSecretDTO) != 1) {
            throw new CommonException("secret.update.error");
        }
    }

    @Override
    public void baseDelete(Long secretId) {
        devopsSecretMapper.deleteByPrimaryKey(secretId);
    }

    @Override
    public void baseCheckName(String name, Long envId) {
        if (!isNameUnique(envId, name)) {
            throw new CommonException("devops.secret.name.already.exists");
        }
    }

    @Override
    public DevopsSecretDTO baseQuery(Long secretId) {
        return devopsSecretMapper.queryById(secretId);
    }

    @Override
    public DevopsSecretDTO baseQueryByEnvIdAndName(Long envId, String name) {
        DevopsSecretDTO devopsSecretDTO = new DevopsSecretDTO();
        devopsSecretDTO.setEnvId(envId);
        devopsSecretDTO.setName(name);
        return devopsSecretMapper.selectOne(devopsSecretDTO);
    }

    @Override
    public Page<DevopsSecretDTO> basePageByOption(Long envId, PageRequest pageable, String params, Long appServiceId) {
        Map<String, Object> paramsMap = TypeUtil.castMapParams(params);
        Map<String, Object> searchParamMap = TypeUtil.cast(paramsMap.get(TypeUtil.SEARCH_PARAM));
        List<String> paramList = TypeUtil.cast(paramsMap.get(TypeUtil.PARAMS));
        return PageHelper.doPageAndSort(PageRequestUtil.simpleConvertSortForPage(pageable), () -> devopsSecretMapper.listByOption(envId, searchParamMap, paramList, appServiceId));
    }

    @Override
    public List<DevopsSecretDTO> baseListByEnv(Long envId) {
        DevopsSecretDTO devopsSecretDTO = new DevopsSecretDTO();
        devopsSecretDTO.setEnvId(envId);
        return devopsSecretMapper.select(devopsSecretDTO);
    }

    @Override
    public void baseDeleteSecretByEnvId(Long envId) {
        DevopsSecretDTO devopsSecretDTO = new DevopsSecretDTO();
        devopsSecretDTO.setEnvId(envId);
        devopsSecretMapper.delete(devopsSecretDTO);
    }
}
