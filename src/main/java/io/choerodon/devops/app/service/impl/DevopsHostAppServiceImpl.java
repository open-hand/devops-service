package io.choerodon.devops.app.service.impl;

import static org.hzero.core.base.BaseConstants.Symbol.SLASH;

import java.util.*;
import java.util.stream.Collectors;

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

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.validator.DevopsHostAdditionalCheckValidator;
import io.choerodon.devops.api.vo.PipelineInstanceReferenceVO;
import io.choerodon.devops.api.vo.deploy.CustomDeployVO;
import io.choerodon.devops.api.vo.deploy.DeploySourceVO;
import io.choerodon.devops.api.vo.deploy.FileInfoVO;
import io.choerodon.devops.api.vo.deploy.JarDeployVO;
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
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.dto.repo.C7nNexusComponentDTO;
import io.choerodon.devops.infra.dto.repo.InstanceDeployOptions;
import io.choerodon.devops.infra.dto.repo.JarPullInfoDTO;
import io.choerodon.devops.infra.dto.repo.NexusMavenRepoDTO;
import io.choerodon.devops.infra.enums.AppCenterDeployWayEnum;
import io.choerodon.devops.infra.enums.AppSourceType;
import io.choerodon.devops.infra.enums.DeployType;
import io.choerodon.devops.infra.enums.PipelineStatus;
import io.choerodon.devops.infra.enums.deploy.DeployModeEnum;
import io.choerodon.devops.infra.enums.deploy.DeployObjectTypeEnum;
import io.choerodon.devops.infra.enums.deploy.OperationTypeEnum;
import io.choerodon.devops.infra.enums.deploy.RdupmTypeEnum;
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

    private static final String ERROR_UPDATE_JAVA_INSTANCE_FAILED = "error.update.java.instance.failed";

    private static final String CONNECTED = "connected";
    private static final String DISCONNECTED = "disconnected";

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
    @Lazy
    private DevopsCdPipelineService devopsCdPipelineService;
    @Autowired
    private DevopsDockerInstanceMapper devopsDockerInstanceMapper;
    @Autowired
    private DevopsCdHostDeployInfoService devopsCdHostDeployInfoService;
    @Autowired
    private HostConnectionHandler hostConnectionHandler;
    @Autowired
    private DockerComposeValueService dockerComposeValueService;

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
            if (!devopsHostAppInstanceDTO.getPreCommand().equals(jarDeployVO.getPreCommand()) ||
                    !devopsHostAppInstanceDTO.getRunCommand().equals(jarDeployVO.getRunCommand()) ||
                    !devopsHostAppInstanceDTO.getPostCommand().equals(jarDeployVO.getPostCommand()) ||
                    !devopsHostAppInstanceDTO.getSourceConfig().equals(calculateSourceConfig(jarDeployVO))) {
                // 执行操作前，先判断kill命令是否存在，不存在停止执行
                if (!HostDeployUtil.checkKillCommandExist(jarDeployVO.getKillCommand())) {
                    throw new CommonException("error.host.instance.kill.command.exist");
                }
                deployJavaInstance(projectId, devopsHostDTO, devopsHostAppDTO, devopsHostAppInstanceDTO, jarDeployVO);
            } else if (!devopsHostAppDTO.getName().equals(jarDeployVO.getAppName())) {
                devopsHostAppDTO.setName(jarDeployVO.getAppName());
                devopsHostAppMapper.updateByPrimaryKey(devopsHostAppDTO);
            } else {
                // 更新删除命令
                if (!Objects.equals(devopsHostAppInstanceDTO.getKillCommand(), jarDeployVO.getKillCommand())) {
                    devopsHostAppInstanceService.updateKillCommand(devopsHostAppInstanceDTO.getId(), jarDeployVO.getKillCommand());
                }
                // 更新健康探针
                if (!Objects.equals(devopsHostAppInstanceDTO.getHealthProb(), jarDeployVO.getHealthProb())) {
                    devopsHostAppInstanceService.updateHealthProb(devopsHostAppInstanceDTO.getId(), jarDeployVO.getHealthProb());
                    // 发送指令给agent
                    InstanceDeployOptions instanceDeployOptions = new InstanceDeployOptions();
                    instanceDeployOptions.setInstanceId(String.valueOf(devopsHostAppInstanceDTO.getId()));
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
            devopsCdHostDeployInfoService.updateJarDeployInfoFromAppCenter(jarDeployVO);
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
        Assert.notNull(hostId, ResourceCheckConstant.ERROR_HOST_ID_IS_NULL);
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
        Assert.notNull(hostId, ResourceCheckConstant.ERROR_HOST_ID_IS_NULL);
        Assert.notNull(code, ResourceCheckConstant.ERROR_JAR_NAME_IS_NULL);
        DevopsHostAppDTO devopsHostAppDTO = new DevopsHostAppDTO(hostId, code);
        return devopsHostAppMapper.selectOne(devopsHostAppDTO);
    }

    @Override
    public Page<DevopsHostAppVO> pagingAppByHost(Long projectId, Long hostId, PageRequest pageRequest, String rdupmType, String operationType, String params) {
        Page<DevopsHostAppVO> page;
        if (permissionHelper.isGitlabProjectOwnerOrGitlabAdmin(projectId, DetailsHelper.getUserDetails().getUserId())) {
            page = PageHelper.doPage(pageRequest, () -> devopsHostAppMapper.listBasicInfoByOptions(projectId, hostId, rdupmType, operationType, params));
        } else {
            page = PageHelper.doPage(pageRequest, () -> devopsHostAppMapper.listOwnedBasicInfoByOptions(projectId, DetailsHelper.getUserDetails().getUserId(), hostId, rdupmType, operationType, params));
        }

        if (CollectionUtils.isEmpty(page.getContent())) {
            return page;
        }
        UserDTOFillUtil.fillUserInfo(page.getContent(), "createdBy", "creator");
        Set<Long> appIds = page.getContent().stream()
                .filter(v -> RdupmTypeEnum.JAR.value().equals(v.getRdupmType()) || RdupmTypeEnum.OTHER.value().equals(v.getRdupmType()))
                .map(DevopsHostAppVO::getId)
                .collect(Collectors.toSet());
        Map<Long, List<DevopsHostAppInstanceDTO>> hostAppInstanceDTOMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(appIds)) {
            List<DevopsHostAppInstanceDTO> devopsHostAppInstanceDTOS = devopsHostAppInstanceService.listByAppIds(appIds);
            hostAppInstanceDTOMap = devopsHostAppInstanceDTOS.stream().collect(Collectors.groupingBy(DevopsHostAppInstanceDTO::getAppId));
        }

        Map<Long, List<DevopsHostAppInstanceDTO>> finalHostAppInstanceDTOMap = hostAppInstanceDTOMap;
        page.getContent().forEach(devopsHostAppVO -> {
            compoundDevopsHostAppVO(devopsHostAppVO);
            if (RdupmTypeEnum.JAR.value().equals(devopsHostAppVO.getRdupmType()) || RdupmTypeEnum.OTHER.value().equals(devopsHostAppVO.getRdupmType())) {
                List<DevopsHostAppInstanceDTO> devopsHostAppInstanceDTOS = finalHostAppInstanceDTOMap.get(devopsHostAppVO.getId());
                if (!CollectionUtils.isEmpty(devopsHostAppInstanceDTOS)) {
                    DevopsHostAppInstanceDTO devopsHostAppInstanceDTO = devopsHostAppInstanceDTOS.get(0);
                    devopsHostAppVO.setKillCommandExist(HostDeployUtil.checkKillCommandExist(devopsHostAppInstanceDTO.getKillCommand()));
                    devopsHostAppVO.setHealthProbExist(HostDeployUtil.checkHealthProbExit(devopsHostAppInstanceDTO.getHealthProb()));
                    devopsHostAppVO.setDevopsHostCommandDTO(devopsHostCommandService.queryInstanceLatest(devopsHostAppInstanceDTO.getId()));
                }
            }
            if (RdupmTypeEnum.DOCKER_COMPOSE.value().equals(devopsHostAppVO.getRdupmType())) {
                devopsHostAppVO.setDevopsHostCommandDTO(devopsHostCommandService.queryInstanceLatest(devopsHostAppVO.getId()));
            }

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
        List<DevopsHostAppVO> devopsHostAppVOS = new ArrayList<>();
        devopsHostAppVOS.add(devopsHostAppVO);
        UserDTOFillUtil.fillUserInfo(devopsHostAppVOS, "createdBy", "creator");
        UserDTOFillUtil.fillUserInfo(devopsHostAppVOS, "lastUpdatedBy", "updater");
        devopsHostAppVO = devopsHostAppVOS.get(0);
        compoundDevopsHostAppVO(devopsHostAppVO);
        devopsHostAppVO.setDeployWay(AppCenterDeployWayEnum.HOST.getValue());
        if (org.apache.commons.lang3.StringUtils.equals(devopsHostAppVO.getRdupmType(), RdupmTypeEnum.DOCKER.value())) {
            DevopsDockerInstanceDTO devopsDockerInstanceDTO = new DevopsDockerInstanceDTO();
            devopsDockerInstanceDTO.setAppId(devopsHostAppVO.getId());
            List<DevopsDockerInstanceDTO> devopsDockerInstanceDTOS = devopsDockerInstanceMapper.select(devopsDockerInstanceDTO);
            if (!CollectionUtils.isEmpty(devopsDockerInstanceDTOS)) {
                List<DevopsDockerInstanceDTO> dockerInstanceDTOS = devopsDockerInstanceDTOS.stream().sorted(Comparator.comparing(DevopsDockerInstanceDTO::getId).reversed()).collect(Collectors.toList());
                devopsHostAppVO.setInstanceId(dockerInstanceDTOS.get(0).getId());
                devopsHostAppVO.setStatus(dockerInstanceDTOS.get(0).getStatus());
                devopsHostAppVO.setPorts(dockerInstanceDTOS.get(0).getPorts());
            }
        }
        devopsHostAppVO.setDevopsHostCommandDTO(devopsHostCommandMapper.selectLatestByInstanceId(devopsHostAppVO.getInstanceId()));
        devopsHostAppVO.setKillCommandExist(HostDeployUtil.checkKillCommandExist(devopsHostAppVO.getKillCommand()));
        devopsHostAppVO.setHealthProbExist(HostDeployUtil.checkHealthProbExit(devopsHostAppVO.getHealthProb()));
        // 表示中间件，需要查询额外字段
        if (RdupmTypeEnum.MIDDLEWARE.value().equals(devopsHostAppVO.getRdupmType())) {
            DevopsMiddlewareDTO devopsMiddlewareDTO = devopsMiddlewareService.queryByInstanceId(devopsHostAppVO.getInstanceId());
            devopsHostAppVO.setMiddlewareMode(DevopsMiddlewareServiceImpl.MODE_MAP.get(devopsMiddlewareDTO.getMode()));
            devopsHostAppVO.setMiddlewareVersion(devopsMiddlewareDTO.getVersion());
        }

        if (RdupmTypeEnum.DOCKER_COMPOSE.value().equals(devopsHostAppVO.getRdupmType())) {
            DevopsHostAppDTO devopsHostAppDTO = baseQuery(id);
            devopsHostAppVO.setRunCommand(devopsHostAppDTO.getRunCommand());
            devopsHostAppVO.setDockerComposeValueDTO(dockerComposeValueService.baseQuery(devopsHostAppDTO.getEffectValueId()));
        }

        DevopsDockerInstanceDTO devopsDockerInstanceDTO = new DevopsDockerInstanceDTO();
        devopsDockerInstanceDTO.setAppId(devopsHostAppVO.getId());
        List<DevopsDockerInstanceDTO> devopsDockerInstanceDTOS = devopsDockerInstanceMapper.select(devopsDockerInstanceDTO);
        if (!CollectionUtils.isEmpty(devopsDockerInstanceDTOS)) {
            DevopsDockerInstanceDTO dockerInstanceDTO = devopsDockerInstanceDTOS.stream().sorted(Comparator.comparing(DevopsDockerInstanceDTO::getId).reversed()).collect(Collectors.toList()).get(0);
            devopsHostAppVO.setDevopsDockerInstanceVO(ConvertUtils.convertObject(dockerInstanceDTO, DevopsDockerInstanceVO.class));
        }
        // 设置所属主机连接状态
        devopsHostAppVO.setHostStatus(hostConnectionHandler.getHostConnectionStatus(devopsHostAppVO.getHostId()) ? CONNECTED : DISCONNECTED);
        return devopsHostAppVO;
    }

    @Override
    public void checkNameAndCodeUniqueAndThrow(Long projectId, Long appId, String name, String code) {
        checkNameUniqueAndThrow(projectId, appId, name);

        checkCodeUniqueAndThrow(projectId, appId, name);

    }

    public void checkCodeUniqueAndThrow(Long projectId, Long appId, String code) {
        if (Boolean.FALSE.equals(checkNameUnique(projectId, appId, code))) {
            throw new CommonException("error.host.app.code.exist");
        }
    }

    public void checkNameUniqueAndThrow(Long projectId, Long appId, String name) {
        if (Boolean.FALSE.equals(checkNameUnique(projectId, appId, name))) {
            throw new CommonException("error.host.app.name.exist");
        }
    }

    @Override
    public Boolean checkCodeUnique(Long projectId, Long appId, String code) {
        return devopsHostAppMapper.checkCodeUnique(projectId, appId, code);
    }

    @Override
    public Boolean checkNameUnique(Long projectId, Long appId, String name) {
        return devopsHostAppMapper.checkNameUnique(projectId, appId, name);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long projectId, Long hostId, Long appId) {
        // 校验应用是否关联流水线，是则抛出异常，不能删除
        if (queryPipelineReferenceHostApp(projectId, appId) != null) {
            throw new CommonException(ResourceCheckConstant.ERROR_APP_INSTANCE_IS_ASSOCIATED_WITH_PIPELINE);
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
                throw new CommonException("error.host.instance.kill.command.exist");
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
            hostAgentMsgVO.setType(HostCommandEnum.OPERATE_INSTANCE.value());
            hostAgentMsgVO.setCommandId(String.valueOf(devopsHostCommandDTO.getId()));

            InstanceDeployOptions instanceDeployOptions = new InstanceDeployOptions();
            instanceDeployOptions.setInstanceId(String.valueOf(appId));
            instanceDeployOptions.setOperation(MiscConstants.DELETE_TYPE);
            hostAgentMsgVO.setPayload(JsonHelper.marshalByJackson(instanceDeployOptions));
            LOGGER.info("Delete docker-compose app msg is {}", JsonHelper.marshalByJackson(hostAgentMsgVO));

            webSocketHelper.sendByGroup(DevopsHostConstants.GROUP + hostId, String.format(DevopsHostConstants.DOCKER_COMPOSE, hostId, appId), JsonHelper.marshalByJackson(hostAgentMsgVO));
        } else if (RdupmTypeEnum.DOCKER.value().equals(devopsHostAppDTO.getRdupmType())) {
            DevopsDockerInstanceDTO devopsDockerInstanceDTO = new DevopsDockerInstanceDTO();
            devopsDockerInstanceDTO.setAppId(appId);
            List<DevopsDockerInstanceDTO> devopsDockerInstanceDTOS = devopsDockerInstanceMapper.select(devopsDockerInstanceDTO);
            if (CollectionUtils.isEmpty(devopsDockerInstanceDTOS)) {
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
            sendHostDockerAgentMsg(hostId, dockerInstanceDTO, devopsHostCommandDTO);
        } else {
            List<DevopsHostAppInstanceDTO> devopsHostAppInstanceDTOS = devopsHostAppInstanceService.listByAppId(appId);

            DevopsHostAppInstanceDTO devopsHostAppInstanceDTO = devopsHostAppInstanceDTOS.get(0);
            if (ObjectUtils.isEmpty(devopsHostAppInstanceDTO.getKillCommand())) {
                throw new CommonException("error.host.instance.kill.command.exist");
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

            InstanceDeployOptions instanceDeployOptions = new InstanceDeployOptions();
            instanceDeployOptions.setInstanceId(String.valueOf(devopsHostAppInstanceDTO.getId()));
            instanceDeployOptions.setKillCommand(Base64Util.decodeBuffer(devopsHostAppInstanceDTO.getKillCommand()));
            instanceDeployOptions.setOperation(MiscConstants.DELETE_TYPE);
            hostAgentMsgVO.setPayload(JsonHelper.marshalByJackson(instanceDeployOptions));

            webSocketHelper.sendByGroup(DevopsHostConstants.GROUP + hostId, DevopsHostConstants.GROUP + hostId, JsonHelper.marshalByJackson(hostAgentMsgVO));
        }
    }

    private void sendHostDockerAgentMsg(Long hostId, DevopsDockerInstanceDTO dockerInstanceDTO, DevopsHostCommandDTO devopsHostCommandDTO) {
        HostAgentMsgVO hostAgentMsgVO = getHostAgentMsgVO(hostId, devopsHostCommandDTO);

        DockerProcessInfoVO dockerProcessInfoVO = new DockerProcessInfoVO();
        dockerProcessInfoVO.setContainerId(dockerInstanceDTO.getContainerId());
        dockerProcessInfoVO.setInstanceId(String.valueOf(dockerInstanceDTO.getId()));

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
                    throw new CommonException("error.host.instance.kill.command.exist");
                }
                deployCustomInstance(projectId, devopsHostDTO, devopsHostAppDTO, devopsHostAppInstanceDTO, customDeployVO);
            } else if (!devopsHostAppDTO.getName().equals(customDeployVO.getAppName())) {
                devopsHostAppDTO.setName(customDeployVO.getAppName());
                devopsHostAppMapper.updateByPrimaryKey(devopsHostAppDTO);
            } else {
                // 更新删除命令
                if (!Objects.equals(devopsHostAppInstanceDTO.getKillCommand(), customDeployVO.getKillCommand())) {
                    devopsHostAppInstanceService.updateKillCommand(devopsHostAppInstanceDTO.getId(), customDeployVO.getKillCommand());
                }
                // 更新健康探针
                if (!Objects.equals(devopsHostAppInstanceDTO.getHealthProb(), customDeployVO.getHealthProb())) {
                    devopsHostAppInstanceDTO.setHealthProb(customDeployVO.getHealthProb());
                    devopsHostAppInstanceService.updateHealthProb(devopsHostAppInstanceDTO.getId(), customDeployVO.getHealthProb());
                    // 发送指令给agent
                    InstanceDeployOptions instanceDeployOptions = new InstanceDeployOptions();
                    instanceDeployOptions.setInstanceId(String.valueOf(devopsHostAppInstanceDTO.getId()));
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
            devopsCdHostDeployInfoService.updateCustomDeployInfoFromAppCenter(customDeployVO);
        }
    }

    @Override
    public PipelineInstanceReferenceVO queryPipelineReferenceHostApp(Long projectId, Long appId) {
        return devopsCdPipelineService.queryPipelineReferenceHostApp(projectId, appId);
    }

    private void compoundDevopsHostAppVO(DevopsHostAppVO devopsHostAppVO) {
        if (!RdupmTypeEnum.DOCKER.value().equals(devopsHostAppVO.getRdupmType())) {
            if (AppSourceType.CURRENT_PROJECT.getValue().equals(devopsHostAppVO.getSourceType())) {
                devopsHostAppVO.setProdJarInfoVO(JsonHelper.unmarshalByJackson(devopsHostAppVO.getSourceConfig(), ProdJarInfoVO.class));
            } else if (AppSourceType.MARKET.getValue().equals(devopsHostAppVO.getSourceType())
                    || AppSourceType.HZERO.getValue().equals(devopsHostAppVO.getSourceType())) {
                devopsHostAppVO.setMarketDeployObjectInfoVO(JsonHelper.unmarshalByJackson(devopsHostAppVO.getSourceConfig(), MarketDeployObjectInfoVO.class));
            } else if (AppSourceType.UPLOAD.getValue().equals(devopsHostAppVO.getSourceType())) {
                devopsHostAppVO.setFileInfoVO(JsonHelper.unmarshalByJackson(devopsHostAppVO.getSourceConfig(), FileInfoVO.class));
            } else if (AppSourceType.CUSTOM_JAR.getValue().equals(devopsHostAppVO.getSourceType())) {
                devopsHostAppVO.setJarPullInfoDTO(JsonHelper.unmarshalByJackson(devopsHostAppVO.getSourceConfig(), JarPullInfoDTO.class));
            }
        }
    }

    private void deployCustomInstance(Long projectId, DevopsHostDTO devopsHostDTO, DevopsHostAppDTO devopsHostAppDTO, DevopsHostAppInstanceDTO devopsHostAppInstanceDTO, CustomDeployVO customDeployVO) {
        Long hostId = customDeployVO.getHostId();
        // 校验主机已连接
        hostConnectionHandler.checkHostConnection(hostId);
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);

        DeploySourceVO deploySourceVO = new DeploySourceVO();
        deploySourceVO.setType(customDeployVO.getSourceType());
        deploySourceVO.setProjectName(projectDTO.getName());
        if (devopsHostAppDTO == null) {
            devopsHostAppDTO = new DevopsHostAppDTO(projectId,
                    hostId,
                    customDeployVO.getAppName(),
                    customDeployVO.getAppCode(),
                    RdupmTypeEnum.OTHER.value(),
                    OperationTypeEnum.CREATE_APP.value());
            MapperUtil.resultJudgedInsertSelective(devopsHostAppMapper, devopsHostAppDTO, DevopsHostConstants.ERROR_SAVE_CUSTOM_INSTANCE_FAILED);

            devopsHostAppInstanceDTO = new DevopsHostAppInstanceDTO(projectId,
                    hostId,
                    devopsHostAppDTO.getId(),
                    customDeployVO.getAppCode() + "-" + GenerateUUID.generateRandomString(),
                    customDeployVO.getSourceType(),
                    AppSourceType.UPLOAD.getValue().equals(customDeployVO.getSourceType()) ? JsonHelper.marshalByJackson(customDeployVO.getFileInfoVO()) : null,
                    customDeployVO.getPreCommand(),
                    customDeployVO.getRunCommand(),
                    customDeployVO.getPostCommand(),
                    customDeployVO.getKillCommand(),
                    customDeployVO.getHealthProb());

            devopsHostAppInstanceService.baseCreate(devopsHostAppInstanceDTO);
        } else {
            devopsHostAppDTO.setName(customDeployVO.getAppName());
            MapperUtil.resultJudgedUpdateByPrimaryKey(devopsHostAppMapper, devopsHostAppDTO, DevopsHostConstants.ERROR_UPDATE_JAVA_INSTANCE_FAILED);

            devopsHostAppInstanceDTO.setPreCommand(customDeployVO.getPreCommand());
            devopsHostAppInstanceDTO.setRunCommand(customDeployVO.getRunCommand());
            devopsHostAppInstanceDTO.setPostCommand(customDeployVO.getPostCommand());
            devopsHostAppInstanceDTO.setKillCommand(customDeployVO.getKillCommand());
            devopsHostAppInstanceDTO.setHealthProb(customDeployVO.getHealthProb());
            devopsHostAppInstanceDTO.setSourceType(customDeployVO.getSourceType());
            devopsHostAppInstanceDTO.setSourceConfig(AppSourceType.UPLOAD.getValue().equals(customDeployVO.getSourceType()) ? JsonHelper.marshalByJackson(customDeployVO.getFileInfoVO()) : null);
            devopsHostAppInstanceService.baseUpdate(devopsHostAppInstanceDTO);
        }

        Map<String, String> params = new HashMap<>();
        String workDir = HostDeployUtil.getWorkingDir(devopsHostAppInstanceDTO.getId());
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
                ObjectUtils.isEmpty(customDeployVO.getPreCommand()) ? "" : HostDeployUtil.getCommand(params, Base64Util.decodeBuffer(customDeployVO.getPreCommand())),
                ObjectUtils.isEmpty(customDeployVO.getRunCommand()) ? "" : HostDeployUtil.getCommand(params, Base64Util.decodeBuffer(customDeployVO.getRunCommand())),
                ObjectUtils.isEmpty(customDeployVO.getPostCommand()) ? "" : HostDeployUtil.getCommand(params, Base64Util.decodeBuffer(customDeployVO.getPostCommand())),
                ObjectUtils.isEmpty(customDeployVO.getKillCommand()) ? "" : HostDeployUtil.getCommand(params, Base64Util.decodeBuffer(customDeployVO.getKillCommand())),
                ObjectUtils.isEmpty(customDeployVO.getHealthProb()) ? "" : HostDeployUtil.getCommand(params, Base64Util.decodeBuffer(customDeployVO.getHealthProb())),
                customDeployVO.getOperation());

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
        hostConnectionHandler.checkHostConnection(devopsHostDTO.getId());

        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);

        DeploySourceVO deploySourceVO = new DeploySourceVO();
        deploySourceVO.setType(jarDeployVO.getSourceType());
        deploySourceVO.setProjectName(projectDTO.getName());

        String deployObjectName = null;
        String deployVersion = null;

        // 获取并记录信息
        List<C7nNexusComponentDTO> nexusComponentDTOList = new ArrayList<>();
        List<NexusMavenRepoDTO> mavenRepoDTOList = new ArrayList<>();

        // 标识部署对象
        if (StringUtils.endsWithIgnoreCase(AppSourceType.MARKET.getValue(), jarDeployVO.getSourceType())
                || StringUtils.endsWithIgnoreCase(AppSourceType.HZERO.getValue(), jarDeployVO.getSourceType())) {
            MarketServiceDeployObjectVO marketServiceDeployObjectVO = marketServiceClientOperator.queryDeployObject(Objects.requireNonNull(projectId), Objects.requireNonNull(jarDeployVO.getMarketDeployObjectInfoVO().getMktDeployObjectId()));
            JarReleaseConfigVO jarReleaseConfigVO = JsonHelper.unmarshalByJackson(marketServiceDeployObjectVO.getMarketJarLocation(), JarReleaseConfigVO.class);
            if (Objects.isNull(marketServiceDeployObjectVO.getMarketMavenConfigVO())) {
                throw new CommonException("error.maven.deploy.object.not.exist");
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
            nexusComponentDTOList = rdupmClientOperator.listMavenComponents(projectDTO.getOrganizationId(), projectId, nexusRepoId, groupId, artifactId, version);
            mavenRepoDTOList = rdupmClientOperator.getRepoUserByProject(projectDTO.getOrganizationId(), projectId, Collections.singleton(nexusRepoId));
            deployObjectName = nexusComponentDTOList.get(0).getName();
            deployVersion = nexusComponentDTOList.get(0).getVersion();
        }

        if (devopsHostAppDTO == null) {
            devopsHostAppDTO = new DevopsHostAppDTO(projectId,
                    hostId,
                    jarDeployVO.getAppName(),
                    jarDeployVO.getAppCode(),
                    RdupmTypeEnum.JAR.value(),
                    OperationTypeEnum.CREATE_APP.value());
            MapperUtil.resultJudgedInsertSelective(devopsHostAppMapper, devopsHostAppDTO, DevopsHostConstants.ERROR_SAVE_JAVA_INSTANCE_FAILED);
            devopsHostAppInstanceDTO = new DevopsHostAppInstanceDTO(projectId,
                    hostId,
                    devopsHostAppDTO.getId(),
                    jarDeployVO.getAppCode() + "-" + GenerateUUID.generateRandomString(),
                    jarDeployVO.getSourceType(),
                    calculateSourceConfig(jarDeployVO),
                    jarDeployVO.getPreCommand(),
                    jarDeployVO.getRunCommand(),
                    jarDeployVO.getPostCommand(),
                    jarDeployVO.getKillCommand(),
                    jarDeployVO.getHealthProb());
            devopsHostAppInstanceDTO.setGroupId(groupId);
            devopsHostAppInstanceDTO.setArtifactId(artifactId);
            devopsHostAppInstanceDTO.setVersion(version);

            devopsHostAppInstanceService.baseCreate(devopsHostAppInstanceDTO);
        } else {
            devopsHostAppDTO.setName(jarDeployVO.getAppName());
            MapperUtil.resultJudgedUpdateByPrimaryKey(devopsHostAppMapper, devopsHostAppDTO, DevopsHostConstants.ERROR_UPDATE_JAVA_INSTANCE_FAILED);

            List<DevopsHostAppInstanceDTO> devopsHostAppInstanceDTOS = devopsHostAppInstanceService.listByAppId(devopsHostAppDTO.getId());
            devopsHostAppInstanceDTO = devopsHostAppInstanceDTOS.get(0);

            devopsHostAppInstanceDTO.setPreCommand(jarDeployVO.getPreCommand());
            devopsHostAppInstanceDTO.setRunCommand(jarDeployVO.getRunCommand());
            devopsHostAppInstanceDTO.setPostCommand(jarDeployVO.getPostCommand());
            devopsHostAppInstanceDTO.setKillCommand(jarDeployVO.getKillCommand());
            devopsHostAppInstanceDTO.setHealthProb(jarDeployVO.getHealthProb());
            devopsHostAppInstanceDTO.setSourceType(jarDeployVO.getSourceType());
            devopsHostAppInstanceDTO.setSourceConfig(calculateSourceConfig(jarDeployVO));
            devopsHostAppInstanceDTO.setVersion(version);
            devopsHostAppInstanceService.baseUpdate(devopsHostAppInstanceDTO);
        }

        Map<String, String> params = new HashMap<>();
        String workDir = HostDeployUtil.getWorkingDir(devopsHostAppInstanceDTO.getId());
        params.put("{{ WORK_DIR }}", workDir);
        String downloadCommand;
        String appFile;
        String appFileName;
        if (AppSourceType.UPLOAD.getValue().equals(jarDeployVO.getSourceType())) {
            appFileName = jarDeployVO.getFileInfoVO().getFileName();
            appFile = workDir + SLASH + appFileName;
            downloadCommand = HostDeployUtil.getDownloadCommand(null,
                    null,
                    jarDeployVO.getFileInfoVO().getUploadUrl(),
                    appFile);
        } else if (AppSourceType.CUSTOM_JAR.getValue().equals(jarDeployVO.getSourceType())) {
            String downloadUrl = jarDeployVO.getJarPullInfoDTO().getDownloadUrl();

            appFileName = downloadUrl.substring(downloadUrl.lastIndexOf("/") + 1);
            appFile = workDir + SLASH + appFileName;
            downloadCommand = HostDeployUtil.getDownloadCommand(jarDeployVO.getJarPullInfoDTO().getPullUserId(),
                    jarDeployVO.getJarPullInfoDTO().getPullUserPassword(),
                    downloadUrl,
                    appFile);
        } else {
            appFileName = nexusComponentDTOList.get(0).getName();
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
                ObjectUtils.isEmpty(jarDeployVO.getPreCommand()) ? "" : HostDeployUtil.getCommand(params, Base64Util.decodeBuffer(jarDeployVO.getPreCommand())),
                ObjectUtils.isEmpty(jarDeployVO.getRunCommand()) ? "" : HostDeployUtil.getCommand(params, Base64Util.decodeBuffer(jarDeployVO.getRunCommand())),
                ObjectUtils.isEmpty(jarDeployVO.getPostCommand()) ? "" : HostDeployUtil.getCommand(params, Base64Util.decodeBuffer(jarDeployVO.getPostCommand())),
                ObjectUtils.isEmpty(jarDeployVO.getKillCommand()) ? "" : HostDeployUtil.getCommand(params, Base64Util.decodeBuffer(jarDeployVO.getKillCommand())),
                ObjectUtils.isEmpty(jarDeployVO.getHealthProb()) ? "" : HostDeployUtil.getCommand(params, Base64Util.decodeBuffer(jarDeployVO.getHealthProb())),
                jarDeployVO.getOperation());

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
        webSocketHelper.sendByGroup(DevopsHostConstants.GROUP + hostId,
                String.format(DevopsHostConstants.NORMAL_INSTANCE, hostId, devopsHostAppInstanceDTO.getId()),
                JsonHelper.marshalByJackson(hostAgentMsgVO));
    }
}
