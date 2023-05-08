package io.choerodon.devops.app.service.impl;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.validator.DevopsHostAdditionalCheckValidator;
import io.choerodon.devops.api.vo.PipelineInstanceReferenceVO;
import io.choerodon.devops.api.vo.deploy.*;
import io.choerodon.devops.api.vo.harbor.ExternalImageInfo;
import io.choerodon.devops.api.vo.harbor.ProdImageInfoVO;
import io.choerodon.devops.api.vo.host.DevopsDockerInstanceVO;
import io.choerodon.devops.api.vo.host.DevopsHostAppVO;
import io.choerodon.devops.api.vo.host.DockerProcessInfoVO;
import io.choerodon.devops.api.vo.host.HostAgentMsgVO;
import io.choerodon.devops.api.vo.market.JarReleaseConfigVO;
import io.choerodon.devops.api.vo.market.MarketDeployObjectInfoVO;
import io.choerodon.devops.api.vo.market.MarketMavenConfigVO;
import io.choerodon.devops.api.vo.market.MarketServiceDeployObjectVO;
import io.choerodon.devops.api.vo.rdupm.ProdJarInfoVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.DevopsHostConstants;
import io.choerodon.devops.infra.constant.MiscConstants;
import io.choerodon.devops.infra.constant.ResourceCheckConstant;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.dto.repo.C7nNexusComponentDTO;
import io.choerodon.devops.infra.dto.repo.InstanceDeployOptions;
import io.choerodon.devops.infra.dto.repo.JarPullInfoDTO;
import io.choerodon.devops.infra.dto.repo.NexusMavenRepoDTO;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.enums.deploy.DeployModeEnum;
import io.choerodon.devops.infra.enums.deploy.DeployObjectTypeEnum;
import io.choerodon.devops.infra.enums.deploy.OperationTypeEnum;
import io.choerodon.devops.infra.enums.deploy.RdupmTypeEnum;
import io.choerodon.devops.infra.enums.host.DevopsHostDeployType;
import io.choerodon.devops.infra.enums.host.HostCommandEnum;
import io.choerodon.devops.infra.enums.host.HostCommandStatusEnum;
import io.choerodon.devops.infra.enums.host.HostResourceType;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.MarketServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.RdupmClientOperator;
import io.choerodon.devops.infra.handler.HostConnectionHandler;
import io.choerodon.devops.infra.mapper.DevopsDockerInstanceMapper;
import io.choerodon.devops.infra.mapper.DevopsHostAppMapper;
import io.choerodon.devops.infra.mapper.DevopsHostCommandMapper;
import io.choerodon.devops.infra.util.*;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.hzero.core.base.BaseConstants;
import org.hzero.websocket.helper.KeySocketSendHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.hzero.core.base.BaseConstants.Symbol.SLASH;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/7/1 9:25
 */
@Service
public class DevopsHostAppServiceImpl implements DevopsHostAppService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsHostAppServiceImpl.class);

    private static final String ERROR_UPDATE_JAVA_INSTANCE_FAILED = "devops.update.java.instance.failed";
    private static final String ERROR_HOST_INSTANCE_KILL_COMMAND_EXIST = "devops.host.instance.kill.command.exist";

    private static final String CONNECTED = "connected";
    private static final String DISCONNECTED = "disconnected";

    private static final String DEFAULT_WORK_DIR_TEMPLATE = "/var/choerodon/%s";

    @Lazy
    @Autowired
    private DevopsHostAdditionalCheckValidator devopsHostAdditionalCheckValidator;
    @Autowired
    private DevopsHostAppMapper devopsHostAppMapper;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    @Lazy
    private DevopsHostService devopsHostService;
    @Autowired
    private RdupmClientOperator rdupmClientOperator;
    @Autowired
    @Lazy
    private DevopsHostCommandService devopsHostCommandService;
    @Autowired
    private DevopsDeployRecordService devopsDeployRecordService;
    @Autowired
    private KeySocketSendHelper webSocketHelper;
    @Autowired
    private MarketServiceClientOperator marketServiceClientOperator;
    @Autowired
    private DevopsHostCommandMapper devopsHostCommandMapper;
    @Autowired
    private DevopsHostUserPermissionService devopsHostUserPermissionService;
    @Autowired
    private PermissionHelper permissionHelper;
    @Autowired
    private DevopsHostAppInstanceService devopsHostAppInstanceService;
    @Autowired
    private DevopsMiddlewareService devopsMiddlewareService;
    @Autowired
    private DevopsDockerInstanceMapper devopsDockerInstanceMapper;
    @Autowired
    private DevopsCiHostDeployInfoService devopsCiHostDeployInfoService;
    @Autowired
    private HostConnectionHandler hostConnectionHandler;
    @Autowired
    private DockerComposeValueService dockerComposeValueService;
    @Autowired
    private DevopsDockerInstanceService devopsDockerInstanceService;
    @Autowired
    @Lazy
    private DevopsCiJobService devopsCiJobService;
    @Autowired
    private DockerComposeService dockerComposeService;

    @Override
    @Transactional
    public void deployJavaInstance(Long projectId, JarDeployVO jarDeployVO) {
        Long hostId = jarDeployVO.getHostId();
        DevopsHostDTO devopsHostDTO = devopsHostService.baseQuery(hostId);
        // 校验主机权限
        devopsHostUserPermissionService.checkUserOwnUsePermissionOrThrow(projectId, devopsHostDTO, DetailsHelper.getUserDetails().getUserId());
        if (jarDeployVO.getOperation().equals(MiscConstants.CREATE_TYPE)) {
            deployJavaInstance(projectId, devopsHostDTO, null, null, jarDeployVO);
        } else {
            // 根据字段变化执行对应操作
            DevopsHostAppDTO devopsHostAppDTO = queryByHostIdAndCode(hostId, jarDeployVO.getAppCode());
            jarDeployVO.setAppId(devopsHostAppDTO.getId());
            List<DevopsHostAppInstanceDTO> devopsHostAppInstanceDTOS = devopsHostAppInstanceService.listByAppId(devopsHostAppDTO.getId());
            DevopsHostAppInstanceDTO devopsHostAppInstanceDTO = devopsHostAppInstanceDTOS.get(0);
            if (!devopsHostAppInstanceDTO.getPreCommand().equals(Base64Util.getBase64EncodedString(jarDeployVO.getPreCommand())) ||
                    !devopsHostAppInstanceDTO.getRunCommand().equals(Base64Util.getBase64EncodedString(jarDeployVO.getRunCommand())) ||
                    !devopsHostAppInstanceDTO.getPostCommand().equals(Base64Util.getBase64EncodedString(jarDeployVO.getPostCommand())) ||
                    !devopsHostAppInstanceDTO.getSourceConfig().equals(calculateSourceConfig(jarDeployVO))) {
                // 执行操作前，先判断kill命令是否存在，不存在停止执行
                if (!HostDeployUtil.checkKillCommandExist(jarDeployVO.getKillCommand())) {
                    throw new CommonException(ERROR_HOST_INSTANCE_KILL_COMMAND_EXIST);
                }
                deployJavaInstance(projectId, devopsHostDTO, devopsHostAppDTO, devopsHostAppInstanceDTO, jarDeployVO);
            } else if (!devopsHostAppDTO.getName().equals(jarDeployVO.getAppName())) {
                devopsHostAppDTO.setName(jarDeployVO.getAppName());
                devopsHostAppMapper.updateByPrimaryKey(devopsHostAppDTO);
            } else {
                // 更新删除命令
                if (!Objects.equals(devopsHostAppInstanceDTO.getKillCommand(), jarDeployVO.getKillCommand())) {
                    devopsHostAppInstanceService.updateKillCommand(devopsHostAppInstanceDTO.getId(), Base64Util.getBase64EncodedString(jarDeployVO.getKillCommand()));
                }
                // 更新健康探针
                if (!Objects.equals(devopsHostAppInstanceDTO.getHealthProb(), jarDeployVO.getHealthProb())) {
                    devopsHostAppInstanceService.updateHealthProb(devopsHostAppInstanceDTO.getId(), Base64Util.getBase64EncodedString(jarDeployVO.getHealthProb()));
                    // 发送指令给agent
                    InstanceDeployOptions instanceDeployOptions = new InstanceDeployOptions();
                    instanceDeployOptions.setInstanceId(String.valueOf(devopsHostAppInstanceDTO.getId()));
                    instanceDeployOptions.setVersion(devopsHostAppInstanceDTO.getVersion());
                    instanceDeployOptions.setAppCode(devopsHostAppDTO.getCode());
                    HostAgentMsgVO hostAgentMsgVO = new HostAgentMsgVO();
                    hostAgentMsgVO.setHostId(String.valueOf(hostId));
                    hostAgentMsgVO.setType(HostCommandEnum.UPDATE_PROB_COMMAND.value());
                    hostAgentMsgVO.setPayload(JsonHelper.marshalByJackson(instanceDeployOptions));

                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info(">>>>>>>>>>>>>>>>>>>>>> deploy java instance msg is {} <<<<<<<<<<<<<<<<<<<<<<<<", JsonHelper.marshalByJackson(hostAgentMsgVO));
                    }
                    webSocketHelper.sendByGroup(DevopsHostConstants.GROUP + hostId,
                            String.format(DevopsHostConstants.NORMAL_INSTANCE, hostId, devopsHostAppInstanceDTO.getId()),
                            JsonHelper.marshalByJackson(hostAgentMsgVO));
                }
            }
            devopsCiHostDeployInfoService.updateJarDeployInfoFromAppCenter(jarDeployVO);
        }
    }

    public String calculateSourceConfig(JarDeployVO jarDeployVO) {

        if (AppSourceType.CURRENT_PROJECT.getValue().equals(jarDeployVO.getSourceType())) {
            return JsonHelper.marshalByJackson(jarDeployVO.getProdJarInfoVO());
        } else if (AppSourceType.MARKET.getValue().equals(jarDeployVO.getSourceType())
                || AppSourceType.HZERO.getValue().equals(jarDeployVO.getSourceType())) {
            return JsonHelper.marshalByJackson(jarDeployVO.getMarketDeployObjectInfoVO());
        } else if (AppSourceType.UPLOAD.getValue().equals(jarDeployVO.getSourceType())) {
            return JsonHelper.marshalByJackson(jarDeployVO.getFileInfoVO());
        } else if (AppSourceType.CUSTOM_JAR.getValue().equals(jarDeployVO.getSourceType())) {
            return JsonHelper.marshalByJackson(jarDeployVO.getJarPullInfoDTO());
        }
        return null;
    }

    @Override
    public List<DevopsHostAppDTO> listByHostId(Long hostId) {
        Assert.notNull(hostId, ResourceCheckConstant.DEVOPS_HOST_ID_IS_NULL);
        return devopsHostAppMapper.listByHostId(hostId);
    }

    @Override
    @Transactional
    public void baseUpdate(DevopsHostAppDTO devopsHostAppDTO) {
        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsHostAppMapper, devopsHostAppDTO, ERROR_UPDATE_JAVA_INSTANCE_FAILED);
    }

    @Override
    @Transactional
    public void baseDelete(Long id) {
        devopsHostAppMapper.deleteByPrimaryKey(id);
    }

    @Override
    public DevopsHostAppDTO baseQuery(Long id) {
        return devopsHostAppMapper.selectByPrimaryKey(id);
    }

    @Override
    public DevopsHostAppDTO queryByHostIdAndCode(Long hostId, String code) {
        Assert.notNull(hostId, ResourceCheckConstant.DEVOPS_HOST_ID_IS_NULL);
        Assert.notNull(code, ResourceCheckConstant.DEVOPS_JAR_NAME_IS_NULL);
        DevopsHostAppDTO devopsHostAppDTO = new DevopsHostAppDTO(hostId, code);
        return devopsHostAppMapper.selectOne(devopsHostAppDTO);
    }

    @Override
    public Page<DevopsHostAppVO> pagingAppByHost(Long projectId, Long hostId, PageRequest pageRequest, String rdupmType, String operationType, String params, String name, Long appId) {
        Page<DevopsHostAppVO> page;
        if (permissionHelper.isGitlabProjectOwnerOrGitlabAdmin(projectId, DetailsHelper.getUserDetails().getUserId())) {
            page = PageHelper.doPage(pageRequest, () -> devopsHostAppMapper.listBasicInfoByOptions(projectId, hostId, rdupmType, operationType, params, name, appId));
        } else {
            page = PageHelper.doPage(pageRequest, () -> devopsHostAppMapper.listOwnedBasicInfoByOptions(projectId, DetailsHelper.getUserDetails().getUserId(), hostId, rdupmType, operationType, params, name, appId));
        }

        if (CollectionUtils.isEmpty(page.getContent())) {
            return page;
        }
        Set<Long> hostIds = page.getContent().stream().map(DevopsHostAppVO::getHostId).collect(Collectors.toSet());
        List<DevopsHostDTO> devopsHostDTOS = devopsHostService.listByIds(hostIds);
        Map<Long, DevopsHostDTO> hostDTOMap = devopsHostDTOS.stream().collect(Collectors.toMap(DevopsHostDTO::getId, Function.identity()));
        UserDTOFillUtil.fillUserInfo(page.getContent(), "createdBy", "creator");
        Set<Long> appIds = page.getContent().stream()
                .filter(v -> RdupmTypeEnum.JAR.value().equals(v.getRdupmType()) || RdupmTypeEnum.OTHER.value().equals(v.getRdupmType()))
                .map(DevopsHostAppVO::getId)
                .collect(Collectors.toSet());
        Set<Long> dcValueIds = page.getContent().stream()
                .filter(v -> RdupmTypeEnum.DOCKER_COMPOSE.value().equals(v.getRdupmType()))
                .map(DevopsHostAppVO::getEffectValueId)
                .collect(Collectors.toSet());
        Map<Long, List<DevopsHostAppInstanceDTO>> hostAppInstanceDTOMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(appIds)) {
            List<DevopsHostAppInstanceDTO> devopsHostAppInstanceDTOS = devopsHostAppInstanceService.listByAppIds(appIds);
            hostAppInstanceDTOMap = devopsHostAppInstanceDTOS.stream().collect(Collectors.groupingBy(DevopsHostAppInstanceDTO::getAppId));
        }
        Map<Long, DockerComposeValueDTO> dockerComposeValueDTOMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(dcValueIds)) {
            List<DockerComposeValueDTO> dockerComposeValueDTOS = dockerComposeValueService.listByIds(dcValueIds);
            if (!CollectionUtils.isEmpty(dockerComposeValueDTOS)) {
                dockerComposeValueDTOMap = dockerComposeValueDTOS.stream().collect(Collectors.toMap(DockerComposeValueDTO::getId, Function.identity()));
            }
        }

        Map<Long, List<DevopsHostAppInstanceDTO>> finalHostAppInstanceDTOMap = hostAppInstanceDTOMap;
        Map<Long, DockerComposeValueDTO> finalDockerComposeValueDTOMap = dockerComposeValueDTOMap;
        page.getContent().forEach(devopsHostAppVO -> {
            if (RdupmTypeEnum.JAR.value().equals(devopsHostAppVO.getRdupmType()) || RdupmTypeEnum.OTHER.value().equals(devopsHostAppVO.getRdupmType())) {
                List<DevopsHostAppInstanceDTO> devopsHostAppInstanceDTOS = finalHostAppInstanceDTOMap.get(devopsHostAppVO.getId());
                if (!CollectionUtils.isEmpty(devopsHostAppInstanceDTOS)) {
                    DevopsHostAppInstanceDTO devopsHostAppInstanceDTO = devopsHostAppInstanceDTOS.get(0);
                    compoundDevopsHostAppVO(devopsHostAppVO, devopsHostAppInstanceDTO);
                    devopsHostAppVO.setDevopsHostCommandDTO(devopsHostCommandMapper.selectLatestByInstanceIdAndType(devopsHostAppInstanceDTO.getId(), HostResourceType.INSTANCE_PROCESS.value()));
                    devopsHostAppVO.setKillCommandExist(HostDeployUtil.checkKillCommandExist(devopsHostAppInstanceDTO.getKillCommand()));
                    devopsHostAppVO.setHealthProbExist(HostDeployUtil.checkHealthProbExit(devopsHostAppInstanceDTO.getHealthProb()));
                    devopsHostAppVO.setSourceType(devopsHostAppInstanceDTO.getSourceType());
                    devopsHostAppVO.setGroupId(devopsHostAppInstanceDTO.getGroupId());
                    devopsHostAppVO.setArtifactId(devopsHostAppInstanceDTO.getArtifactId());
                    devopsHostAppVO.setReady(devopsHostAppInstanceDTO.getReady());
                    devopsHostAppVO.setPreCommand(Base64Util.decodeBuffer(devopsHostAppInstanceDTO.getPreCommand()));
                    devopsHostAppVO.setRunCommand(Base64Util.decodeBuffer(devopsHostAppInstanceDTO.getRunCommand()));
                    devopsHostAppVO.setPostCommand(Base64Util.decodeBuffer(devopsHostAppInstanceDTO.getPostCommand()));
                    devopsHostAppVO.setKillCommand(Base64Util.decodeBuffer(devopsHostAppInstanceDTO.getKillCommand()));
                    devopsHostAppVO.setHealthProb(Base64Util.decodeBuffer(devopsHostAppInstanceDTO.getHealthProb()));
                    devopsHostAppVO.setKillCommandExist(HostDeployUtil.checkKillCommandExist(devopsHostAppInstanceDTO.getKillCommand()));
                    devopsHostAppVO.setHealthProbExist(HostDeployUtil.checkHealthProbExit(devopsHostAppInstanceDTO.getHealthProb()));
                    if (ObjectUtils.isEmpty(devopsHostAppVO.getWorkDir())) {
                        devopsHostAppVO.setWorkDir(String.format(DEFAULT_WORK_DIR_TEMPLATE, devopsHostAppVO.getVersion().equals("1") ? devopsHostAppInstanceDTO.getId() : devopsHostAppVO.getCode()));
                    }
                }
            }

            if (RdupmTypeEnum.DOCKER_COMPOSE.value().equals(devopsHostAppVO.getRdupmType())) {
                devopsHostAppVO.setKillCommandExist(true);
                devopsHostAppVO.setRunCommand(devopsHostAppVO.getRunCommand());
                List<DevopsDockerInstanceDTO> devopsDockerInstanceDTOS = devopsDockerInstanceService.listByAppId(devopsHostAppVO.getId());
                calculateStatus(devopsHostAppVO, devopsDockerInstanceDTOS);

                devopsHostAppVO.setDevopsHostCommandDTO(devopsHostCommandService.queryInstanceLatest(devopsHostAppVO.getId(), HostResourceType.DOCKER_COMPOSE.value()));
                devopsHostAppVO.setDockerComposeValueDTO(finalDockerComposeValueDTOMap.get(devopsHostAppVO.getEffectValueId()));
                if (ObjectUtils.isEmpty(devopsHostAppVO.getWorkDir())) {
                    devopsHostAppVO.setWorkDir(String.format(DEFAULT_WORK_DIR_TEMPLATE, devopsHostAppVO.getVersion().equals("1") ? devopsHostAppVO.getId() : devopsHostAppVO.getCode()));
                }
            }

            if (RdupmTypeEnum.DOCKER.value().equals(devopsHostAppVO.getRdupmType())) {
                devopsHostAppVO.setKillCommandExist(true);
                DevopsDockerInstanceDTO devopsDockerInstanceDTO = new DevopsDockerInstanceDTO();
                devopsDockerInstanceDTO.setAppId(devopsHostAppVO.getId());
                List<DevopsDockerInstanceDTO> devopsDockerInstanceDTOS = devopsDockerInstanceMapper.select(devopsDockerInstanceDTO);
                if (!CollectionUtils.isEmpty(devopsDockerInstanceDTOS)) {
                    List<DevopsDockerInstanceDTO> dockerInstanceDTOS = devopsDockerInstanceDTOS.stream().sorted(Comparator.comparing(DevopsDockerInstanceDTO::getId).reversed()).collect(Collectors.toList());
                    devopsHostAppVO.setInstanceId(dockerInstanceDTOS.get(0).getId());
                    devopsHostAppVO.setStatus(dockerInstanceDTOS.get(0).getStatus());
                    devopsHostAppVO.setPorts(dockerInstanceDTOS.get(0).getPorts());
                    DevopsDockerInstanceDTO dockerInstanceDTO = devopsDockerInstanceDTOS.stream().sorted(Comparator.comparing(DevopsDockerInstanceDTO::getId).reversed()).collect(Collectors.toList()).get(0);
                    devopsHostAppVO.setDevopsDockerInstanceVO(ConvertUtils.convertObject(dockerInstanceDTO, DevopsDockerInstanceVO.class));
                    if (ObjectUtils.isEmpty(devopsHostAppVO.getWorkDir())) {
                        devopsHostAppVO.setWorkDir(String.format(DEFAULT_WORK_DIR_TEMPLATE, devopsHostAppVO.getVersion().equals("1") ? dockerInstanceDTOS.get(0).getId() : devopsHostAppVO.getCode()));
                    }
                }
                devopsHostAppVO.setDevopsHostCommandDTO(devopsHostCommandService.queryDockerInstanceLatest(devopsHostAppVO.getId(), HostResourceType.DOCKER_PROCESS.value()));
            }
            DevopsHostDTO devopsHostDTO = hostDTOMap.get(devopsHostAppVO.getHostId());
            devopsHostAppVO.setDevopsHostDTO(devopsHostDTO);
            devopsHostAppVO.setHostStatus(hostConnectionHandler.getHostConnectionStatus(devopsHostAppVO.getHostId()) ? CONNECTED : DISCONNECTED);
        });
        return page;
    }

    @Override
    public DevopsHostAppVO queryAppById(Long projectId, Long id) {
        DevopsHostAppVO devopsHostAppVO = devopsHostAppMapper.queryAppById(id);
        if (ObjectUtils.isEmpty(devopsHostAppVO)) {
            return devopsHostAppVO;
        }
        devopsHostAppVO.setDeployWay(AppCenterDeployWayEnum.HOST.getValue());
        if (org.apache.commons.lang3.StringUtils.equals(devopsHostAppVO.getRdupmType(), RdupmTypeEnum.DOCKER.value())) {
            devopsHostAppVO.setKillCommandExist(true);
            DevopsDockerInstanceDTO devopsDockerInstanceDTO = new DevopsDockerInstanceDTO();
            devopsDockerInstanceDTO.setAppId(devopsHostAppVO.getId());
            List<DevopsDockerInstanceDTO> devopsDockerInstanceDTOS = devopsDockerInstanceMapper.select(devopsDockerInstanceDTO);
            if (!CollectionUtils.isEmpty(devopsDockerInstanceDTOS)) {
                List<DevopsDockerInstanceDTO> dockerInstanceDTOS = devopsDockerInstanceDTOS.stream().sorted(Comparator.comparing(DevopsDockerInstanceDTO::getId).reversed()).collect(Collectors.toList());
                devopsHostAppVO.setInstanceId(dockerInstanceDTOS.get(0).getId());
                devopsHostAppVO.setStatus(dockerInstanceDTOS.get(0).getStatus());
                devopsHostAppVO.setPorts(dockerInstanceDTOS.get(0).getPorts());
                DevopsDockerInstanceDTO dockerInstanceDTO = devopsDockerInstanceDTOS.stream().sorted(Comparator.comparing(DevopsDockerInstanceDTO::getId).reversed()).collect(Collectors.toList()).get(0);
                devopsHostAppVO.setDevopsDockerInstanceVO(ConvertUtils.convertObject(dockerInstanceDTO, DevopsDockerInstanceVO.class));
            }
            devopsHostAppVO.setDevopsHostCommandDTO(devopsHostCommandMapper.selectLatestByInstanceIdAndType(devopsHostAppVO.getInstanceId(), HostResourceType.DOCKER_PROCESS.value()));

        }

        // 表示中间件，需要查询额外字段
        if (RdupmTypeEnum.MIDDLEWARE.value().equals(devopsHostAppVO.getRdupmType())) {
            devopsHostAppVO.setKillCommandExist(true);
            DevopsMiddlewareDTO devopsMiddlewareDTO = devopsMiddlewareService.queryByInstanceId(devopsHostAppVO.getId());
            devopsHostAppVO.setMiddlewareMode(DevopsMiddlewareServiceImpl.MODE_MAP.get(devopsMiddlewareDTO.getMode()));
            devopsHostAppVO.setMiddlewareVersion(devopsMiddlewareDTO.getVersion());
        }
        if (RdupmTypeEnum.JAR.value().equals(devopsHostAppVO.getRdupmType())
                || RdupmTypeEnum.OTHER.value().equals(devopsHostAppVO.getRdupmType())) {
            List<DevopsHostAppInstanceDTO> devopsHostAppInstanceDTOS = devopsHostAppInstanceService.listByAppId(devopsHostAppVO.getId());
            if (!CollectionUtils.isEmpty(devopsHostAppInstanceDTOS)) {
                DevopsHostAppInstanceDTO devopsHostAppInstanceDTO = devopsHostAppInstanceDTOS.get(0);
                compoundDevopsHostAppVO(devopsHostAppVO, devopsHostAppInstanceDTO);
                devopsHostAppVO.setDevopsHostCommandDTO(devopsHostCommandMapper.selectLatestByInstanceIdAndType(devopsHostAppInstanceDTO.getId(), HostResourceType.INSTANCE_PROCESS.value()));
                devopsHostAppVO.setKillCommandExist(HostDeployUtil.checkKillCommandExist(devopsHostAppInstanceDTO.getKillCommand()));
                devopsHostAppVO.setHealthProbExist(HostDeployUtil.checkHealthProbExit(devopsHostAppInstanceDTO.getHealthProb()));
                devopsHostAppVO.setSourceType(devopsHostAppInstanceDTO.getSourceType());
                devopsHostAppVO.setGroupId(devopsHostAppInstanceDTO.getGroupId());
                devopsHostAppVO.setArtifactId(devopsHostAppInstanceDTO.getArtifactId());
                devopsHostAppVO.setVersion(devopsHostAppInstanceDTO.getVersion());
                devopsHostAppVO.setReady(devopsHostAppInstanceDTO.getReady());
                devopsHostAppVO.decodeCommand();
            }

        }

        if (RdupmTypeEnum.DOCKER_COMPOSE.value().equals(devopsHostAppVO.getRdupmType())) {
            devopsHostAppVO.setKillCommandExist(true);
            DevopsHostAppDTO devopsHostAppDTO = baseQuery(id);
            devopsHostAppVO.setRunCommand(devopsHostAppDTO.getRunCommand());
            devopsHostAppVO.setDockerComposeValueDTO(dockerComposeValueService.baseQuery(devopsHostAppDTO.getEffectValueId()));
            devopsHostAppVO.setDevopsHostCommandDTO(devopsHostCommandMapper.selectLatestByInstanceIdAndType(devopsHostAppDTO.getId(), HostResourceType.DOCKER_COMPOSE.value()));
            List<DevopsDockerInstanceDTO> devopsDockerInstanceDTOS = devopsDockerInstanceService.listByAppId(id);
            calculateStatus(devopsHostAppVO, devopsDockerInstanceDTOS);
        }
        // 设置所属主机连接状态
        devopsHostAppVO.setHostStatus(hostConnectionHandler.getHostConnectionStatus(devopsHostAppVO.getHostId()) ? CONNECTED : DISCONNECTED);
        IamUserDTO creator = baseServiceClientOperator.queryUserByUserId(devopsHostAppVO.getCreatedBy());
        IamUserDTO updater = baseServiceClientOperator.queryUserByUserId(devopsHostAppVO.getLastUpdatedBy());

        devopsHostAppVO.setCreator(creator);
        devopsHostAppVO.setUpdater(updater);
        if (devopsHostAppVO.getDevopsHostCommandDTO() == null) {
            DevopsHostCommandDTO defaultDevopsHostCommandDTO = new DevopsHostCommandDTO();
            defaultDevopsHostCommandDTO.setHostId(devopsHostAppVO.getHostId());
            defaultDevopsHostCommandDTO.setStatus("failed");
            defaultDevopsHostCommandDTO.setError("Operation missing");
            devopsHostAppVO.setDevopsHostCommandDTO(defaultDevopsHostCommandDTO);
        }
        if (ObjectUtils.isEmpty(devopsHostAppVO.getWorkDir())) {
            devopsHostAppVO.setWorkDir(String.format("/var/choerodon/%s", devopsHostAppVO.getCode()));
        }
        return devopsHostAppVO;
    }

    private void calculateStatus(DevopsHostAppVO devopsHostAppVO, List<DevopsDockerInstanceDTO> devopsDockerInstanceDTOS) {
        if (CollectionUtils.isEmpty(devopsDockerInstanceDTOS)) {
            devopsHostAppVO.setStatus(DockerComposeStatusEnum.OTHER.getType());
            return;
        }
        if (devopsDockerInstanceDTOS.stream().allMatch(v -> DockerComposeStatusEnum.RUNNING.getType().equals(v.getStatus()))) {
            devopsHostAppVO.setStatus(DockerComposeStatusEnum.RUNNING.getType());
            return;
        }
        if (devopsDockerInstanceDTOS.stream().allMatch(v -> DockerComposeStatusEnum.EXITED.getType().equals(v.getStatus()))) {
            devopsHostAppVO.setStatus(DockerComposeStatusEnum.EXITED.getType());
            return;
        }
        devopsHostAppVO.setStatus(DockerComposeStatusEnum.OTHER.getType());
    }

    @Override
    public void checkNameAndCodeUniqueAndThrow(Long projectId, Long hostId, Long appId, String name, String code) {
        checkNameUniqueAndThrow(projectId, hostId, appId, name);

        checkCodeUniqueAndThrow(projectId, hostId, appId, name);

    }

    public void checkCodeUniqueAndThrow(Long projectId, Long hostId, Long appId, String code) {
        if (Boolean.FALSE.equals(checkNameUnique(projectId, hostId, appId, code))) {
            throw new CommonException("devops.host.app.code.exist");
        }
    }

    public void checkNameUniqueAndThrow(Long projectId, Long hostId, Long appId, String name) {
        if (Boolean.FALSE.equals(checkNameUnique(projectId, hostId, appId, name))) {
            throw new CommonException("devops.host.app.name.exist");
        }
    }

    @Override
    public Boolean checkCodeUnique(Long projectId, Long hostId, Long appId, String code) {
        return devopsHostAppMapper.checkCodeUnique(projectId, hostId, appId, code);
    }

    @Override
    public Boolean checkNameUnique(Long projectId, Long hostId, Long appId, String name) {
        return devopsHostAppMapper.checkNameUnique(projectId, hostId, appId, name);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long projectId, Long hostId, Long appId) {
        // 校验应用是否关联流水线，是则抛出异常，不能删除
        if (!CollectionUtils.isEmpty(queryPipelineReferenceHostApp(projectId, appId))) {
            throw new CommonException(ResourceCheckConstant.DEVOPS_APP_INSTANCE_IS_ASSOCIATED_WITH_PIPELINE);
        }
        // 校验主机是否处于连接状态，未连接则抛出异常，不能删除
        hostConnectionHandler.checkHostConnection(hostId);
        devopsHostAdditionalCheckValidator.validHostIdAndInstanceIdMatch(hostId, appId);

        DevopsHostAppDTO devopsHostAppDTO = baseQuery(appId);
        // 后续可以优化代码结构
        if (RdupmTypeEnum.MIDDLEWARE.value().equals(devopsHostAppDTO.getRdupmType())) {
            // 走中间件删除逻辑
            List<DevopsHostAppInstanceDTO> devopsHostAppInstanceDTOS = devopsHostAppInstanceService.listByAppId(appId);

            DevopsHostAppInstanceDTO devopsHostAppInstanceDTO = devopsHostAppInstanceDTOS.get(0);
            if (ObjectUtils.isEmpty(devopsHostAppInstanceDTO.getKillCommand())) {
                throw new CommonException(ERROR_HOST_INSTANCE_KILL_COMMAND_EXIST);
            }
            devopsMiddlewareService.uninstallMiddleware(projectId, devopsHostAppInstanceDTO);
        } else if (RdupmTypeEnum.DOCKER_COMPOSE.value().equals(devopsHostAppDTO.getRdupmType())) {
            DevopsHostCommandDTO devopsHostCommandDTO = new DevopsHostCommandDTO();
            devopsHostCommandDTO.setCommandType(HostCommandEnum.KILL_DOCKER_COMPOSE.value());
            devopsHostCommandDTO.setHostId(hostId);
            devopsHostCommandDTO.setInstanceType(HostResourceType.DOCKER_COMPOSE.value());
            devopsHostCommandDTO.setInstanceId(appId);
            devopsHostCommandDTO.setStatus(HostCommandStatusEnum.OPERATING.value());
            devopsHostCommandService.baseCreate(devopsHostCommandDTO);


            HostAgentMsgVO hostAgentMsgVO = new HostAgentMsgVO();
            hostAgentMsgVO.setHostId(String.valueOf(hostId));
            hostAgentMsgVO.setType(HostCommandEnum.KILL_DOCKER_COMPOSE.value());
            hostAgentMsgVO.setCommandId(String.valueOf(devopsHostCommandDTO.getId()));

            InstanceDeployOptions instanceDeployOptions = new InstanceDeployOptions();
            instanceDeployOptions.setInstanceId(String.valueOf(appId));
            instanceDeployOptions.setOperation(MiscConstants.DELETE_TYPE);
            instanceDeployOptions.setVersion(devopsHostAppDTO.getVersion());
            instanceDeployOptions.setAppCode(devopsHostAppDTO.getCode());
            hostAgentMsgVO.setPayload(JsonHelper.marshalByJackson(instanceDeployOptions));
            LOGGER.info("Delete docker-compose app msg is {}", JsonHelper.marshalByJackson(hostAgentMsgVO));

            webSocketHelper.sendByGroup(DevopsHostConstants.GROUP + hostId, String.format(DevopsHostConstants.DOCKER_COMPOSE, hostId, appId), JsonHelper.marshalByJackson(hostAgentMsgVO));
        } else if (RdupmTypeEnum.DOCKER.value().equals(devopsHostAppDTO.getRdupmType())) {
            DevopsDockerInstanceDTO devopsDockerInstanceDTO = new DevopsDockerInstanceDTO();
            devopsDockerInstanceDTO.setAppId(appId);
            List<DevopsDockerInstanceDTO> devopsDockerInstanceDTOS = devopsDockerInstanceMapper.select(devopsDockerInstanceDTO);
            if (CollectionUtils.isEmpty(devopsDockerInstanceDTOS)) {
                devopsHostAppMapper.deleteByPrimaryKey(appId);
                return;
            }
            DevopsDockerInstanceDTO dockerInstanceDTO = devopsDockerInstanceDTOS.get(0);
            DevopsHostCommandDTO devopsHostCommandDTO = new DevopsHostCommandDTO();
            devopsHostCommandDTO.setCommandType(HostCommandEnum.REMOVE_DOCKER.value());
            devopsHostCommandDTO.setHostId(hostId);
            devopsHostCommandDTO.setInstanceType(HostResourceType.DOCKER_PROCESS.value());
            devopsHostCommandDTO.setInstanceId(dockerInstanceDTO.getId());
            devopsHostCommandDTO.setStatus(HostCommandStatusEnum.OPERATING.value());
            devopsHostCommandService.baseCreate(devopsHostCommandDTO);
            sendHostDockerAgentMsg(hostId, devopsHostAppDTO, dockerInstanceDTO, devopsHostCommandDTO);
        } else {
            List<DevopsHostAppInstanceDTO> devopsHostAppInstanceDTOS = devopsHostAppInstanceService.listByAppId(appId);

            DevopsHostAppInstanceDTO devopsHostAppInstanceDTO = devopsHostAppInstanceDTOS.get(0);
            if (ObjectUtils.isEmpty(devopsHostAppInstanceDTO.getKillCommand())) {
                throw new CommonException(ERROR_HOST_INSTANCE_KILL_COMMAND_EXIST);
            }

            DevopsHostCommandDTO devopsHostCommandDTO = new DevopsHostCommandDTO();
            devopsHostCommandDTO.setCommandType(HostCommandEnum.KILL_INSTANCE.value());
            devopsHostCommandDTO.setHostId(hostId);
            devopsHostCommandDTO.setInstanceType(HostResourceType.INSTANCE_PROCESS.value());
            devopsHostCommandDTO.setInstanceId(devopsHostAppInstanceDTO.getId());
            devopsHostCommandDTO.setStatus(HostCommandStatusEnum.OPERATING.value());
            devopsHostCommandService.baseCreate(devopsHostCommandDTO);


            HostAgentMsgVO hostAgentMsgVO = new HostAgentMsgVO();
            hostAgentMsgVO.setHostId(String.valueOf(hostId));
            hostAgentMsgVO.setType(HostCommandEnum.OPERATE_INSTANCE.value());
            hostAgentMsgVO.setCommandId(String.valueOf(devopsHostCommandDTO.getId()));

            Map<String, String> params = getRuntimeParams(projectId, devopsHostAppDTO, devopsHostAppInstanceDTO);
            InstanceDeployOptions instanceDeployOptions = new InstanceDeployOptions();
            instanceDeployOptions.setInstanceId(String.valueOf(devopsHostAppInstanceDTO.getId()));
            instanceDeployOptions.setKillCommand(HostDeployUtil.getCommand(params, Base64Util.decodeBuffer(devopsHostAppInstanceDTO.getKillCommand())));
            instanceDeployOptions.setOperation(MiscConstants.DELETE_TYPE);
            instanceDeployOptions.setVersion(devopsHostAppDTO.getVersion());
            instanceDeployOptions.setAppCode(devopsHostAppDTO.getCode());
            hostAgentMsgVO.setPayload(JsonHelper.marshalByJackson(instanceDeployOptions));

            webSocketHelper.sendByGroup(DevopsHostConstants.GROUP + hostId, DevopsHostConstants.GROUP + hostId, JsonHelper.marshalByJackson(hostAgentMsgVO));
        }
    }

    private void sendHostDockerAgentMsg(Long hostId, DevopsHostAppDTO devopsHostAppDTO, DevopsDockerInstanceDTO dockerInstanceDTO, DevopsHostCommandDTO devopsHostCommandDTO) {
        HostAgentMsgVO hostAgentMsgVO = getHostAgentMsgVO(hostId, devopsHostCommandDTO);

        DockerProcessInfoVO dockerProcessInfoVO = new DockerProcessInfoVO();
        dockerProcessInfoVO.setContainerId(dockerInstanceDTO.getContainerId());
        dockerProcessInfoVO.setInstanceId(String.valueOf(dockerInstanceDTO.getId()));
        dockerProcessInfoVO.setVersion(devopsHostAppDTO.getVersion());
        dockerProcessInfoVO.setAppCode(devopsHostAppDTO.getCode());

        InstanceDeployOptions instanceDeployOptions = new InstanceDeployOptions();
        instanceDeployOptions.setInstanceId(String.valueOf(dockerInstanceDTO.getId()));
        instanceDeployOptions.setOperation(MiscConstants.DELETE_TYPE);
        hostAgentMsgVO.setPayload(JsonHelper.marshalByJackson(dockerProcessInfoVO));

        webSocketHelper.sendByGroup(DevopsHostConstants.GROUP + hostId, DevopsHostConstants.GROUP + hostId, JsonHelper.marshalByJackson(hostAgentMsgVO));
    }

    private HostAgentMsgVO getHostAgentMsgVO(Long hostId, DevopsHostCommandDTO devopsHostCommandDTO) {
        HostAgentMsgVO hostAgentMsgVO = new HostAgentMsgVO();
        hostAgentMsgVO.setHostId(String.valueOf(hostId));
        hostAgentMsgVO.setType(HostCommandEnum.REMOVE_DOCKER.value());
        hostAgentMsgVO.setCommandId(String.valueOf(devopsHostCommandDTO.getId()));
        return hostAgentMsgVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseCreate(DevopsHostAppDTO devopsHostAppDTO, String errorCode) {
        MapperUtil.resultJudgedInsertSelective(devopsHostAppMapper, devopsHostAppDTO, errorCode);
    }

    @Override
    @Transactional
    public void deployCustomInstance(Long projectId, CustomDeployVO customDeployVO) {
        Long hostId = customDeployVO.getHostId();
        DevopsHostDTO devopsHostDTO = devopsHostService.baseQuery(hostId);
        // 校验主机权限
        devopsHostUserPermissionService.checkUserOwnUsePermissionOrThrow(projectId, devopsHostDTO, DetailsHelper.getUserDetails().getUserId());
        if (customDeployVO.getOperation().equals(MiscConstants.CREATE_TYPE)) {
            deployCustomInstance(projectId, devopsHostDTO, null, null, customDeployVO);
        } else {
            // 根据字段变化执行对应操作
            DevopsHostAppDTO devopsHostAppDTO = queryByHostIdAndCode(hostId, customDeployVO.getAppCode());
            customDeployVO.setAppId(devopsHostAppDTO.getId());
            List<DevopsHostAppInstanceDTO> devopsHostAppInstanceDTOS = devopsHostAppInstanceService.listByAppId(devopsHostAppDTO.getId());
            DevopsHostAppInstanceDTO devopsHostAppInstanceDTO = devopsHostAppInstanceDTOS.get(0);
            if (!devopsHostAppInstanceDTO.getPreCommand().equals(customDeployVO.getPreCommand()) ||
                    !devopsHostAppInstanceDTO.getRunCommand().equals(customDeployVO.getRunCommand()) ||
                    !devopsHostAppInstanceDTO.getPostCommand().equals(customDeployVO.getPostCommand())) {
                if (!HostDeployUtil.checkKillCommandExist(customDeployVO.getKillCommand())) {
                    throw new CommonException(ERROR_HOST_INSTANCE_KILL_COMMAND_EXIST);
                }
                deployCustomInstance(projectId, devopsHostDTO, devopsHostAppDTO, devopsHostAppInstanceDTO, customDeployVO);
            } else if (!devopsHostAppDTO.getName().equals(customDeployVO.getAppName())) {
                devopsHostAppDTO.setName(customDeployVO.getAppName());
                devopsHostAppMapper.updateByPrimaryKey(devopsHostAppDTO);
            } else {
                // 更新删除命令
                if (!Objects.equals(devopsHostAppInstanceDTO.getKillCommand(), customDeployVO.getKillCommand())) {
                    devopsHostAppInstanceService.updateKillCommand(devopsHostAppInstanceDTO.getId(), Base64Util.getBase64EncodedString(customDeployVO.getKillCommand()));
                }
                // 更新健康探针
                if (!Objects.equals(devopsHostAppInstanceDTO.getHealthProb(), customDeployVO.getHealthProb())) {
                    devopsHostAppInstanceDTO.setHealthProb(customDeployVO.getHealthProb());
                    devopsHostAppInstanceService.updateHealthProb(devopsHostAppInstanceDTO.getId(), Base64Util.getBase64EncodedString(customDeployVO.getHealthProb()));
                    // 发送指令给agent
                    InstanceDeployOptions instanceDeployOptions = new InstanceDeployOptions();
                    instanceDeployOptions.setInstanceId(String.valueOf(devopsHostAppInstanceDTO.getId()));
                    instanceDeployOptions.setVersion(devopsHostAppDTO.getVersion());
                    instanceDeployOptions.setAppCode(devopsHostAppDTO.getCode());
                    HostAgentMsgVO hostAgentMsgVO = new HostAgentMsgVO();
                    hostAgentMsgVO.setHostId(String.valueOf(hostId));
                    hostAgentMsgVO.setType(HostCommandEnum.UPDATE_PROB_COMMAND.value());
                    hostAgentMsgVO.setPayload(JsonHelper.marshalByJackson(instanceDeployOptions));

                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info(">>>>>>>>>>>>>>>>>>>>>> deploy custom instance msg is {} <<<<<<<<<<<<<<<<<<<<<<<<", JsonHelper.marshalByJackson(hostAgentMsgVO));
                    }

                    webSocketHelper.sendByGroup(DevopsHostConstants.GROUP + hostId,
                            String.format(DevopsHostConstants.NORMAL_INSTANCE, hostId, devopsHostAppInstanceDTO.getId()),
                            JsonHelper.marshalByJackson(hostAgentMsgVO));
                }
            }
            devopsCiHostDeployInfoService.updateCustomDeployInfoFromAppCenter(customDeployVO);
        }
    }

    @Override
    public List<PipelineInstanceReferenceVO> queryPipelineReferenceHostApp(Long projectId, Long appId) {
        return devopsCiJobService.queryPipelineReferenceHostApp(projectId, appId);
    }

    @Override
    public void restart(Long projectId, Long hostId, Long appId) {
        DevopsHostDTO devopsHostDTO = devopsHostService.baseQuery(hostId);
        DevopsHostAppDTO devopsHostAppDTO = devopsHostAppMapper.selectByPrimaryKey(appId);
        DevopsHostAppInstanceDTO devopsHostAppInstanceDTO;
        switch (RdupmTypeEnum.valueOf(devopsHostAppDTO.getRdupmType().toUpperCase())) {
            case DOCKER:
                DevopsDockerInstanceVO devopsDockerInstanceVO = devopsDockerInstanceMapper.listByAppId(appId, null, null)
                        .get(0);
                DockerDeployVO dockerDeployVO = new DockerDeployVO();
                dockerDeployVO.setHostId(hostId)
                        .setHostAppId(appId)
                        .setOperation(MiscConstants.UPDATE_TYPE)
                        .setContainerName(devopsDockerInstanceVO.getName()).setName(devopsHostAppDTO.getName())
                        .setRepoType(devopsDockerInstanceVO.getRepoType())
                        .setSourceType(devopsDockerInstanceVO.getSourceType())
                        .setValue(devopsDockerInstanceVO.getDockerCommand());
                if (DevopsHostDeployType.DEFAULT.value().equals(dockerDeployVO.getRepoType())) {
                    ProdImageInfoVO prodImageInfoVO = new ProdImageInfoVO(devopsDockerInstanceVO.getRepoName(),
                            devopsDockerInstanceVO.getRepoType(),
                            devopsDockerInstanceVO.getRepoId(),
                            devopsDockerInstanceVO.getImageName(),
                            devopsDockerInstanceVO.getTag(),
                            devopsDockerInstanceVO.getPrivateRepository(),
                            devopsDockerInstanceVO.getImage());
                    dockerDeployVO.setImageInfo(prodImageInfoVO);
                } else {
                    ExternalImageInfo externalImageInfo = new ExternalImageInfo(devopsDockerInstanceVO.getImage(),
                            devopsDockerInstanceVO.getUserName(),
                            devopsDockerInstanceVO.getPassWord(),
                            devopsDockerInstanceVO.getPrivateRepository());
                    dockerDeployVO.setExternalImageInfo(externalImageInfo);
                }
                devopsDockerInstanceService.deployDockerInstance(projectId, dockerDeployVO);
                break;
            case DOCKER_COMPOSE:
                dockerComposeService.restartDockerComposeApp(projectId, appId);
                break;
            case JAR:
                devopsHostAppInstanceDTO = devopsHostAppInstanceService.listByAppId(appId).get(0);
                JarDeployVO jarDeployVO = new JarDeployVO();
                jarDeployVO.setHostId(hostId)
                        .setAppName(devopsHostAppDTO.getName())
                        .setAppCode(devopsHostAppDTO.getCode())
                        .setSourceType(devopsHostAppInstanceDTO.getSourceType())
                        .setOperation(MiscConstants.UPDATE_TYPE)
                        .setAppId(appId)
                        .setPreCommand(Base64Util.decodeBuffer(devopsHostAppInstanceDTO.getPreCommand()))
                        .setRunCommand(Base64Util.decodeBuffer(devopsHostAppInstanceDTO.getRunCommand()))
                        .setPostCommand(Base64Util.decodeBuffer(devopsHostAppInstanceDTO.getPostCommand()))
                        .setKillCommand(Base64Util.decodeBuffer(devopsHostAppInstanceDTO.getKillCommand()))
                        .setHealthProb(Base64Util.decodeBuffer(devopsHostAppInstanceDTO.getHealthProb()));
                if (AppSourceType.CURRENT_PROJECT.getValue().equals(devopsHostAppInstanceDTO.getSourceType())) {
                    jarDeployVO.setProdJarInfoVO(JsonHelper.unmarshalByJackson(devopsHostAppInstanceDTO.getSourceConfig(), ProdJarInfoVO.class));
                } else if (AppSourceType.UPLOAD.getValue().equals(devopsHostAppInstanceDTO.getSourceType())) {
                    jarDeployVO.setFileInfoVO(JsonHelper.unmarshalByJackson(devopsHostAppInstanceDTO.getSourceConfig(), FileInfoVO.class));
                } else if (AppSourceType.CUSTOM_JAR.getValue().equals(devopsHostAppInstanceDTO.getSourceType())) {
                    jarDeployVO.setJarPullInfoDTO(JsonHelper.unmarshalByJackson(devopsHostAppInstanceDTO.getSourceConfig(), JarPullInfoDTO.class));
                }
                deployJavaInstance(projectId, devopsHostDTO, devopsHostAppDTO, devopsHostAppInstanceDTO, jarDeployVO);
                break;
            case OTHER:
                devopsHostAppInstanceDTO = devopsHostAppInstanceService.listByAppId(appId).get(0);
                CustomDeployVO customDeployVO = new CustomDeployVO();
                customDeployVO.setHostId(hostId)
                        .setAppName(devopsHostAppDTO.getName())
                        .setAppCode(devopsHostAppDTO.getCode())
                        .setSourceType(devopsHostAppInstanceDTO.getSourceType())
                        .setOperation(MiscConstants.UPDATE_TYPE)
                        .setAppId(appId)
                        .setPreCommand(Base64Util.decodeBuffer(devopsHostAppInstanceDTO.getPreCommand()))
                        .setRunCommand(Base64Util.decodeBuffer(devopsHostAppInstanceDTO.getRunCommand()))
                        .setPostCommand(Base64Util.decodeBuffer(devopsHostAppInstanceDTO.getPostCommand()))
                        .setKillCommand(Base64Util.decodeBuffer(devopsHostAppInstanceDTO.getKillCommand()))
                        .setHealthProb(Base64Util.decodeBuffer(devopsHostAppInstanceDTO.getHealthProb()));
                customDeployVO.setFileInfoVO(JsonHelper.unmarshalByJackson(devopsHostAppInstanceDTO.getSourceConfig(), FileInfoVO.class));
                deployCustomInstance(projectId, devopsHostDTO, devopsHostAppDTO, devopsHostAppInstanceDTO, customDeployVO);
                break;
        }
    }

    @Override
    public Set<String> listWorkDirs(Long projectId, Long hostId) {
        List<DevopsHostAppVO> devopsHostAppVOS = devopsHostAppMapper.listWorkDirsByHostId(hostId);
        return devopsHostAppVOS.stream()
                .map(a -> {
                    if (ObjectUtils.isEmpty(a.getWorkDir())) {
                        return "/var/choerodon/" + a.getCode();
                    } else {
                        return a.getWorkDir();
                    }
                }).collect(Collectors.toSet());
    }

    private void compoundDevopsHostAppVO(DevopsHostAppVO devopsHostAppVO, DevopsHostAppInstanceDTO devopsHostAppInstanceDTO) {
        if (!RdupmTypeEnum.DOCKER.value().equals(devopsHostAppVO.getRdupmType())) {
            if (AppSourceType.CURRENT_PROJECT.getValue().equals(devopsHostAppInstanceDTO.getSourceType())) {
                devopsHostAppVO.setProdJarInfoVO(JsonHelper.unmarshalByJackson(devopsHostAppInstanceDTO.getSourceConfig(), ProdJarInfoVO.class));
            } else if (AppSourceType.MARKET.getValue().equals(devopsHostAppInstanceDTO.getSourceType())
                    || AppSourceType.HZERO.getValue().equals(devopsHostAppInstanceDTO.getSourceType())) {
                devopsHostAppVO.setMarketDeployObjectInfoVO(JsonHelper.unmarshalByJackson(devopsHostAppInstanceDTO.getSourceConfig(), MarketDeployObjectInfoVO.class));
            } else if (AppSourceType.UPLOAD.getValue().equals(devopsHostAppInstanceDTO.getSourceType())) {
                devopsHostAppVO.setFileInfoVO(JsonHelper.unmarshalByJackson(devopsHostAppInstanceDTO.getSourceConfig(), FileInfoVO.class));
            } else if (AppSourceType.CUSTOM_JAR.getValue().equals(devopsHostAppInstanceDTO.getSourceType())) {
                devopsHostAppVO.setJarPullInfoDTO(JsonHelper.unmarshalByJackson(devopsHostAppInstanceDTO.getSourceConfig(), JarPullInfoDTO.class));
            }
        }
    }

    private void deployCustomInstance(Long projectId, DevopsHostDTO devopsHostDTO, DevopsHostAppDTO devopsHostAppDTO, DevopsHostAppInstanceDTO devopsHostAppInstanceDTO, CustomDeployVO customDeployVO) {
        Long hostId = customDeployVO.getHostId();
        // 校验主机已连接
        hostConnectionHandler.checkHostConnection(hostId);
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);

        DeploySourceVO deploySourceVO = new DeploySourceVO();
        deploySourceVO.setType(customDeployVO.getSourceType());
        deploySourceVO.setProjectName(projectDTO.getName());
        if (devopsHostAppDTO == null) {
            devopsHostAppDTO = new DevopsHostAppDTO(projectId,
                    hostId,
                    customDeployVO.getAppName(),
                    customDeployVO.getAppCode(),
                    RdupmTypeEnum.OTHER.value(),
                    OperationTypeEnum.CREATE_APP.value(),
                    customDeployVO.getWorkDir());
            MapperUtil.resultJudgedInsertSelective(devopsHostAppMapper, devopsHostAppDTO, DevopsHostConstants.ERROR_SAVE_CUSTOM_INSTANCE_FAILED);

            devopsHostAppInstanceDTO = new DevopsHostAppInstanceDTO(projectId,
                    hostId,
                    devopsHostAppDTO.getId(),
                    customDeployVO.getAppCode() + "-" + GenerateUUID.generateRandomString(),
                    customDeployVO.getSourceType(),
                    AppSourceType.UPLOAD.getValue().equals(customDeployVO.getSourceType()) ? JsonHelper.marshalByJackson(customDeployVO.getFileInfoVO()) : null,
                    Base64Util.getBase64EncodedString(customDeployVO.getPreCommand()),
                    Base64Util.getBase64EncodedString(customDeployVO.getRunCommand()),
                    Base64Util.getBase64EncodedString(customDeployVO.getPostCommand()),
                    Base64Util.getBase64EncodedString(customDeployVO.getKillCommand()),
                    Base64Util.getBase64EncodedString(customDeployVO.getHealthProb()));

            devopsHostAppInstanceService.baseCreate(devopsHostAppInstanceDTO);
        } else {
            devopsHostAppDTO.setName(customDeployVO.getAppName());
            MapperUtil.resultJudgedUpdateByPrimaryKey(devopsHostAppMapper, devopsHostAppDTO, DevopsHostConstants.ERROR_UPDATE_JAVA_INSTANCE_FAILED);

            devopsHostAppInstanceDTO.setPreCommand(Base64Util.getBase64EncodedString(customDeployVO.getPreCommand()));
            devopsHostAppInstanceDTO.setRunCommand(Base64Util.getBase64EncodedString(customDeployVO.getRunCommand()));
            devopsHostAppInstanceDTO.setPostCommand(Base64Util.getBase64EncodedString(customDeployVO.getPostCommand()));
            devopsHostAppInstanceDTO.setKillCommand(Base64Util.getBase64EncodedString(customDeployVO.getKillCommand()));
            devopsHostAppInstanceDTO.setHealthProb(Base64Util.getBase64EncodedString(customDeployVO.getHealthProb()));
            devopsHostAppInstanceDTO.setSourceType(customDeployVO.getSourceType());
            devopsHostAppInstanceDTO.setSourceConfig(AppSourceType.UPLOAD.getValue().equals(customDeployVO.getSourceType()) ? JsonHelper.marshalByJackson(customDeployVO.getFileInfoVO()) : null);
            devopsHostAppInstanceService.baseUpdate(devopsHostAppInstanceDTO);
        }

        Map<String, String> params = new HashMap<>();
        String workDir = ObjectUtils.isEmpty(devopsHostAppDTO.getWorkDir()) ? HostDeployUtil.getWorkingDir(devopsHostAppInstanceDTO.getId(), devopsHostAppDTO.getCode(), devopsHostAppDTO.getVersion()) : devopsHostAppDTO.getWorkDir();
        if (customDeployVO.getFileInfoVO().getFileName() == null) {
            customDeployVO.getFileInfoVO().setFileName("");
        }
        String appFileName = customDeployVO.getFileInfoVO().getFileName();
        String appFile = workDir + SLASH + appFileName;
        params.put("{{ WORK_DIR }}", workDir);
        params.put("{{ APP_FILE_NAME }}", appFileName);
        params.put("{{ APP_FILE }}", appFile);

        String downloadCommand = null;
        if (AppSourceType.UPLOAD.getValue().equals(customDeployVO.getSourceType())) {
            downloadCommand = HostDeployUtil.getDownloadCommand(null,
                    null,
                    customDeployVO.getFileInfoVO().getUploadUrl(),
                    appFile);
        }

        InstanceDeployOptions instanceDeployOptions = new InstanceDeployOptions(
                customDeployVO.getAppCode(),
                String.valueOf(devopsHostAppInstanceDTO.getId()),
                downloadCommand,
                ObjectUtils.isEmpty(customDeployVO.getPreCommand()) ? "" : HostDeployUtil.getCommand(params, customDeployVO.getPreCommand()),
                ObjectUtils.isEmpty(customDeployVO.getRunCommand()) ? "" : HostDeployUtil.getCommand(params, customDeployVO.getRunCommand()),
                ObjectUtils.isEmpty(customDeployVO.getPostCommand()) ? "" : HostDeployUtil.getCommand(params, customDeployVO.getPostCommand()),
                ObjectUtils.isEmpty(customDeployVO.getKillCommand()) ? "" : HostDeployUtil.getCommand(params, customDeployVO.getKillCommand()),
                ObjectUtils.isEmpty(customDeployVO.getHealthProb()) ? "" : HostDeployUtil.getCommand(params, customDeployVO.getHealthProb()),
                customDeployVO.getOperation(),
                devopsHostAppDTO.getCode(),
                devopsHostAppDTO.getVersion(),
                devopsHostAppDTO.getWorkDir());

        DevopsHostCommandDTO devopsHostCommandDTO = new DevopsHostCommandDTO();
        devopsHostCommandDTO.setCommandType(HostCommandEnum.OPERATE_INSTANCE.value());
        devopsHostCommandDTO.setHostId(hostId);
        devopsHostCommandDTO.setInstanceType(HostResourceType.INSTANCE_PROCESS.value());
        devopsHostCommandDTO.setInstanceId(devopsHostAppInstanceDTO.getId());
        devopsHostCommandDTO.setStatus(HostCommandStatusEnum.OPERATING.value());
        devopsHostCommandService.baseCreate(devopsHostCommandDTO);

        // 保存执行记录
        devopsDeployRecordService.saveRecord(
                projectId,
                DeployType.MANUAL,
                devopsHostCommandDTO.getId(),
                DeployModeEnum.HOST,
                devopsHostDTO.getId(),
                devopsHostDTO.getName(),
                PipelineStatus.SUCCESS.toValue(),
                DeployObjectTypeEnum.OTHER,
                "其他制品",
                null,
                devopsHostAppDTO.getName(),
                devopsHostAppDTO.getCode(),
                devopsHostAppDTO.getId(),
                deploySourceVO);

        HostAgentMsgVO hostAgentMsgVO = new HostAgentMsgVO();
        hostAgentMsgVO.setHostId(String.valueOf(hostId));
        hostAgentMsgVO.setType(HostCommandEnum.OPERATE_INSTANCE.value());
        hostAgentMsgVO.setCommandId(String.valueOf(devopsHostCommandDTO.getId()));
        hostAgentMsgVO.setPayload(JsonHelper.marshalByJackson(instanceDeployOptions));

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(">>>>>>>>>>>>>>>>>>>>>> deploy custom instance msg is {} <<<<<<<<<<<<<<<<<<<<<<<<", JsonHelper.marshalByJackson(hostAgentMsgVO));
        }

        webSocketHelper.sendByGroup(DevopsHostConstants.GROUP + hostId,
                String.format(DevopsHostConstants.NORMAL_INSTANCE, hostId, devopsHostAppInstanceDTO.getId()),
                JsonHelper.marshalByJackson(hostAgentMsgVO));
    }

    private void deployJavaInstance(Long projectId, DevopsHostDTO devopsHostDTO, DevopsHostAppDTO devopsHostAppDTO, DevopsHostAppInstanceDTO devopsHostAppInstanceDTO, JarDeployVO jarDeployVO) {
        Long hostId = jarDeployVO.getHostId();
        String groupId = null;
        String artifactId = null;
        String version = null;
        // 校验主机已连接
//        hostConnectionHandler.checkHostConnection(devopsHostDTO.getId());

        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);

        DeploySourceVO deploySourceVO = new DeploySourceVO();
        deploySourceVO.setType(jarDeployVO.getSourceType());
        deploySourceVO.setProjectName(projectDTO.getName());

        String deployObjectName = null;
        String deployVersion = null;
        String packaging = "jar";

        // 获取并记录信息
        List<C7nNexusComponentDTO> nexusComponentDTOList = new ArrayList<>();
        List<NexusMavenRepoDTO> mavenRepoDTOList = new ArrayList<>();

        // 标识部署对象
        if (StringUtils.endsWithIgnoreCase(AppSourceType.MARKET.getValue(), jarDeployVO.getSourceType())
                || StringUtils.endsWithIgnoreCase(AppSourceType.HZERO.getValue(), jarDeployVO.getSourceType())) {
            MarketServiceDeployObjectVO marketServiceDeployObjectVO = marketServiceClientOperator.queryDeployObject(Objects.requireNonNull(projectId), Objects.requireNonNull(jarDeployVO.getMarketDeployObjectInfoVO().getMktDeployObjectId()));
            JarReleaseConfigVO jarReleaseConfigVO = JsonHelper.unmarshalByJackson(marketServiceDeployObjectVO.getMarketJarLocation(), JarReleaseConfigVO.class);
            if (Objects.isNull(marketServiceDeployObjectVO.getMarketMavenConfigVO())) {
                throw new CommonException("devops.maven.deploy.object.not.exist");
            }

            deployObjectName = marketServiceDeployObjectVO.getMarketServiceName();

            MarketMavenConfigVO marketMavenConfigVO = marketServiceDeployObjectVO.getMarketMavenConfigVO();
            C7nNexusComponentDTO nNexusComponentDTO = new C7nNexusComponentDTO();

            deployVersion = jarReleaseConfigVO.getVersion();
            nNexusComponentDTO.setName(jarReleaseConfigVO.getArtifactId());
            nNexusComponentDTO.setVersion(jarReleaseConfigVO.getVersion());
            nNexusComponentDTO.setGroup(jarReleaseConfigVO.getGroupId());
            nNexusComponentDTO.setDownloadUrl(MavenUtil.getDownloadUrl(jarReleaseConfigVO));
            nexusComponentDTOList.add(nNexusComponentDTO);

            groupId = jarReleaseConfigVO.getGroupId();
            artifactId = jarReleaseConfigVO.getArtifactId();
            version = jarReleaseConfigVO.getVersion();

            jarDeployVO.setProdJarInfoVO(new ProdJarInfoVO(jarReleaseConfigVO.getGroupId(),
                    jarReleaseConfigVO.getArtifactId(),
                    jarReleaseConfigVO.getVersion()));

            NexusMavenRepoDTO nexusMavenRepoDTO = new NexusMavenRepoDTO();
            nexusMavenRepoDTO.setNePullUserId(marketMavenConfigVO.getPullUserName());
            nexusMavenRepoDTO.setNePullUserPassword(marketMavenConfigVO.getPullPassword());
            mavenRepoDTOList.add(nexusMavenRepoDTO);

            deploySourceVO.setMarketAppName(marketServiceDeployObjectVO.getMarketAppName() + BaseConstants.Symbol.MIDDLE_LINE + marketServiceDeployObjectVO.getMarketAppVersion());
            deploySourceVO.setMarketServiceName(marketServiceDeployObjectVO.getMarketServiceName() + BaseConstants.Symbol.MIDDLE_LINE + marketServiceDeployObjectVO.getMarketServiceVersion());

            //如果是市场部署将部署人员添加为应用的订阅人员
            marketServiceClientOperator.subscribeApplication(marketServiceDeployObjectVO.getMarketAppId(), DetailsHelper.getUserDetails().getUserId());

            deploySourceVO.setDeployObjectId(jarDeployVO.getMarketDeployObjectInfoVO().getMktDeployObjectId());

        } else if (AppSourceType.CURRENT_PROJECT.getValue().equals(jarDeployVO.getSourceType())) {
            // 0.2 从制品库获取仓库信息
            Long nexusRepoId = jarDeployVO.getProdJarInfoVO().getRepositoryId();
            groupId = jarDeployVO.getProdJarInfoVO().getGroupId();
            artifactId = jarDeployVO.getProdJarInfoVO().getArtifactId();
            version = jarDeployVO.getProdJarInfoVO().getVersion();
            nexusComponentDTOList = rdupmClientOperator.listMavenComponents(projectDTO.getOrganizationId(), projectId, nexusRepoId, groupId, artifactId, null, version);
            mavenRepoDTOList = rdupmClientOperator.getRepoUserByProject(projectDTO.getOrganizationId(), projectId, Collections.singleton(nexusRepoId));
            deployObjectName = nexusComponentDTOList.get(0).getName();
            deployVersion = nexusComponentDTOList.get(0).getVersion();
            packaging = nexusComponentDTOList.get(0).getExtension();
        }

        if (devopsHostAppDTO == null) {
            devopsHostAppDTO = new DevopsHostAppDTO(projectId,
                    hostId,
                    jarDeployVO.getAppName(),
                    jarDeployVO.getAppCode(),
                    RdupmTypeEnum.JAR.value(),
                    OperationTypeEnum.CREATE_APP.value(),
                    jarDeployVO.getWorkDir());
            MapperUtil.resultJudgedInsertSelective(devopsHostAppMapper, devopsHostAppDTO, DevopsHostConstants.ERROR_SAVE_JAVA_INSTANCE_FAILED);
            devopsHostAppInstanceDTO = new DevopsHostAppInstanceDTO(projectId,
                    hostId,
                    devopsHostAppDTO.getId(),
                    jarDeployVO.getAppCode() + "-" + GenerateUUID.generateRandomString(),
                    jarDeployVO.getSourceType(),
                    calculateSourceConfig(jarDeployVO),
                    Base64Util.getBase64EncodedString(jarDeployVO.getPreCommand()),
                    Base64Util.getBase64EncodedString(jarDeployVO.getRunCommand()),
                    Base64Util.getBase64EncodedString(jarDeployVO.getPostCommand()),
                    Base64Util.getBase64EncodedString(jarDeployVO.getKillCommand()),
                    Base64Util.getBase64EncodedString(jarDeployVO.getHealthProb()));
            devopsHostAppInstanceDTO.setGroupId(groupId);
            devopsHostAppInstanceDTO.setArtifactId(artifactId);
            devopsHostAppInstanceDTO.setVersion(version);

            devopsHostAppInstanceService.baseCreate(devopsHostAppInstanceDTO);
        } else {
            devopsHostAppDTO.setName(jarDeployVO.getAppName());
            MapperUtil.resultJudgedUpdateByPrimaryKey(devopsHostAppMapper, devopsHostAppDTO, DevopsHostConstants.ERROR_UPDATE_JAVA_INSTANCE_FAILED);

            List<DevopsHostAppInstanceDTO> devopsHostAppInstanceDTOS = devopsHostAppInstanceService.listByAppId(devopsHostAppDTO.getId());
            devopsHostAppInstanceDTO = devopsHostAppInstanceDTOS.get(0);

            devopsHostAppInstanceDTO.setPreCommand(Base64Util.getBase64EncodedString(jarDeployVO.getPreCommand()));
            devopsHostAppInstanceDTO.setRunCommand(Base64Util.getBase64EncodedString(jarDeployVO.getRunCommand()));
            devopsHostAppInstanceDTO.setPostCommand(Base64Util.getBase64EncodedString(jarDeployVO.getPostCommand()));
            devopsHostAppInstanceDTO.setKillCommand(Base64Util.getBase64EncodedString(jarDeployVO.getKillCommand()));
            devopsHostAppInstanceDTO.setHealthProb(Base64Util.getBase64EncodedString(jarDeployVO.getHealthProb()));
            devopsHostAppInstanceDTO.setSourceType(jarDeployVO.getSourceType());
            devopsHostAppInstanceDTO.setSourceConfig(calculateSourceConfig(jarDeployVO));
            devopsHostAppInstanceDTO.setVersion(version);
            devopsHostAppInstanceService.baseUpdate(devopsHostAppInstanceDTO);
        }

        Map<String, String> params = new HashMap<>();
        String workDir = ObjectUtils.isEmpty(devopsHostAppDTO.getWorkDir()) ? HostDeployUtil.getWorkingDir(devopsHostAppInstanceDTO.getId(), devopsHostAppDTO.getCode(), devopsHostAppDTO.getVersion()) : devopsHostAppDTO.getWorkDir();
        params.put("{{ WORK_DIR }}", workDir);
        String downloadCommand;
        String appFile;
        String appFileName;
        if (AppSourceType.UPLOAD.getValue().equals(jarDeployVO.getSourceType())) {
            appFileName = jarDeployVO.getFileInfoVO().getFileName();
            appFileName = appFileName.endsWith(".jar") ? appFileName : appFileName + ".jar";
            appFile = workDir + SLASH + appFileName;
            downloadCommand = HostDeployUtil.getDownloadCommand(null,
                    null,
                    jarDeployVO.getFileInfoVO().getUploadUrl(),
                    appFile);
        } else if (AppSourceType.CUSTOM_JAR.getValue().equals(jarDeployVO.getSourceType())) {
            String downloadUrl = jarDeployVO.getJarPullInfoDTO().getDownloadUrl();

            appFileName = downloadUrl.substring(downloadUrl.lastIndexOf("/") + 1);
            appFileName = appFileName.endsWith(".jar") ? appFileName : appFileName + ".jar";
            appFile = workDir + SLASH + appFileName;
            downloadCommand = HostDeployUtil.getDownloadCommand(jarDeployVO.getJarPullInfoDTO().getPullUserId(),
                    jarDeployVO.getJarPullInfoDTO().getPullUserPassword(),
                    downloadUrl,
                    appFile);
        } else {
            appFileName = nexusComponentDTOList.get(0).getName();
            appFileName = appFileName + "." + packaging;
            appFile = workDir + SLASH + appFileName;
            downloadCommand = HostDeployUtil.getDownloadCommand(mavenRepoDTOList.get(0).getNePullUserId(),
                    mavenRepoDTOList.get(0).getNePullUserPassword(),
                    nexusComponentDTOList.get(0).getDownloadUrl(),
                    appFile);
        }
        params.put("{{ APP_FILE_NAME }}", appFileName);
        params.put("{{ APP_FILE }}", appFile);

        InstanceDeployOptions instanceDeployOptions = new InstanceDeployOptions(
                jarDeployVO.getAppCode(),
                String.valueOf(devopsHostAppInstanceDTO.getId()),
                downloadCommand,
                ObjectUtils.isEmpty(jarDeployVO.getPreCommand()) ? "" : HostDeployUtil.getCommand(params, jarDeployVO.getPreCommand()),
                ObjectUtils.isEmpty(jarDeployVO.getRunCommand()) ? "" : HostDeployUtil.getCommand(params, jarDeployVO.getRunCommand()),
                ObjectUtils.isEmpty(jarDeployVO.getPostCommand()) ? "" : HostDeployUtil.getCommand(params, jarDeployVO.getPostCommand()),
                ObjectUtils.isEmpty(jarDeployVO.getKillCommand()) ? "" : HostDeployUtil.getCommand(params, jarDeployVO.getKillCommand()),
                ObjectUtils.isEmpty(jarDeployVO.getHealthProb()) ? "" : HostDeployUtil.getCommand(params, jarDeployVO.getHealthProb()),
                jarDeployVO.getOperation(),
                devopsHostAppDTO.getCode(),
                devopsHostAppDTO.getVersion(),
                devopsHostAppDTO.getWorkDir());

        DevopsHostCommandDTO devopsHostCommandDTO = new DevopsHostCommandDTO();
        devopsHostCommandDTO.setCommandType(HostCommandEnum.OPERATE_INSTANCE.value());
        devopsHostCommandDTO.setHostId(hostId);
        devopsHostCommandDTO.setInstanceType(HostResourceType.INSTANCE_PROCESS.value());
        devopsHostCommandDTO.setInstanceId(devopsHostAppInstanceDTO.getId());
        devopsHostCommandDTO.setStatus(HostCommandStatusEnum.OPERATING.value());
        devopsHostCommandService.baseCreate(devopsHostCommandDTO);

        // 保存执行记录
        devopsDeployRecordService.saveRecord(
                projectId,
                DeployType.MANUAL,
                devopsHostCommandDTO.getId(),
                DeployModeEnum.HOST,
                devopsHostDTO.getId(),
                devopsHostDTO.getName(),
                PipelineStatus.SUCCESS.toValue(),
                DeployObjectTypeEnum.JAR,
                deployObjectName,
                deployVersion,
                devopsHostAppDTO.getName(),
                devopsHostAppDTO.getCode(),
                devopsHostAppDTO.getId(),
                deploySourceVO);

        // 发送部署指令给agent
        HostAgentMsgVO hostAgentMsgVO = new HostAgentMsgVO();
        hostAgentMsgVO.setHostId(String.valueOf(hostId));
        hostAgentMsgVO.setType(HostCommandEnum.OPERATE_INSTANCE.value());
        hostAgentMsgVO.setCommandId(String.valueOf(devopsHostCommandDTO.getId()));
        hostAgentMsgVO.setPayload(JsonHelper.marshalByJackson(instanceDeployOptions));

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(">>>>>>>>>>>>>>>>>>>>>> deploy jar instance msg is {} <<<<<<<<<<<<<<<<<<<<<<<<", JsonHelper.marshalByJackson(hostAgentMsgVO));
        }
        webSocketHelper.sendByGroup(DevopsHostConstants.GROUP + hostId, String.format(DevopsHostConstants.NORMAL_INSTANCE, hostId, devopsHostAppInstanceDTO.getId()), JsonHelper.marshalByJackson(hostAgentMsgVO));
    }

    private Map<String, String> getRuntimeParams(Long projectId, DevopsHostAppDTO devopsHostAppDTO, DevopsHostAppInstanceDTO devopsHostAppInstanceDTO) {
        Map<String, String> params = new HashMap<>();
        String workDir = ObjectUtils.isEmpty(devopsHostAppDTO.getWorkDir()) ? HostDeployUtil.getWorkingDir(devopsHostAppInstanceDTO.getId(), devopsHostAppDTO.getCode(), devopsHostAppDTO.getVersion()) : devopsHostAppDTO.getWorkDir();
        String appFileName;
        if (ObjectUtils.isEmpty(devopsHostAppInstanceDTO.getSourceType())) {
            appFileName = devopsHostAppInstanceDTO.getCode();
        } else {
            AppSourceType appSourceType = AppSourceType.find(devopsHostAppInstanceDTO.getSourceType());
            switch (appSourceType) {
                case CURRENT_PROJECT:
                    ProdJarInfoVO prodJarInfoVO = JsonHelper.unmarshalByJackson(devopsHostAppInstanceDTO.getSourceConfig(), ProdJarInfoVO.class);
                    Long nexusRepoId = prodJarInfoVO.getRepositoryId();
                    // 从制品库获取仓库信息
                    // 获取并记录信息
                    List<C7nNexusComponentDTO> nexusComponentDTOList;
                    ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);
                    String groupId = prodJarInfoVO.getGroupId();
                    String artifactId = prodJarInfoVO.getArtifactId();
                    String jarVersion = prodJarInfoVO.getVersion();
                    nexusComponentDTOList = rdupmClientOperator.listMavenComponents(projectDTO.getOrganizationId(), projectId, nexusRepoId, groupId, artifactId, null, jarVersion);
                    if (CollectionUtils.isEmpty(nexusComponentDTOList)) {
                        appFileName = devopsHostAppInstanceDTO.getCode();
                    } else {
                        appFileName = nexusComponentDTOList.get(0).getName();
                    }
                    break;
                case CUSTOM_JAR:
                    JarPullInfoDTO jarPullInfoDTO = JsonHelper.unmarshalByJackson(devopsHostAppInstanceDTO.getSourceConfig(), JarPullInfoDTO.class);
                    String downloadUrl = jarPullInfoDTO.getDownloadUrl();
                    appFileName = downloadUrl.substring(downloadUrl.lastIndexOf("/") + 1);
                    break;
                case UPLOAD:
                    FileInfoVO fileInfoVO = JsonHelper.unmarshalByJackson(devopsHostAppInstanceDTO.getSourceConfig(), FileInfoVO.class);
                    appFileName = fileInfoVO.getFileName();
                    break;
                default:
                    appFileName = devopsHostAppInstanceDTO.getCode();
            }
        }

        if (devopsHostAppDTO.getRdupmType().equals("jar")) {
            appFileName = appFileName.endsWith(".jar") ? appFileName : appFileName + ".jar";
        }

        String appFile = workDir + SLASH + appFileName;

        params.put("{{ WORK_DIR }}", workDir);
        params.put("{{ APP_FILE_NAME }}", appFileName);
        params.put("{{ APP_FILE }}", appFile);

        return params;
    }
}
