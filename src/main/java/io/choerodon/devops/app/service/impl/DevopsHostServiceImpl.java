package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.infra.constant.ExceptionConstants.PublicCode.DEVOPS_DELETE_PERMISSION_OF_PROJECT_OWNER;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.gson.Gson;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.hzero.core.util.AssertUtils;
import org.hzero.websocket.helper.KeySocketSendHelper;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.core.utils.PageUtils;
import io.choerodon.devops.api.validator.DevopsHostAdditionalCheckValidator;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.host.*;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.DevopsHostConstants;
import io.choerodon.devops.infra.constant.MiscConstants;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.enums.DevopsHostStatus;
import io.choerodon.devops.infra.enums.DevopsHostType;
import io.choerodon.devops.infra.enums.DevopsHostUserPermissionLabelEnums;
import io.choerodon.devops.infra.enums.LabelType;
import io.choerodon.devops.infra.enums.host.HostCommandEnum;
import io.choerodon.devops.infra.enums.host.HostCommandStatusEnum;
import io.choerodon.devops.infra.enums.host.HostResourceType;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.handler.HostConnectionHandler;
import io.choerodon.devops.infra.mapper.DevopsDockerInstanceMapper;
import io.choerodon.devops.infra.mapper.DevopsHostAppMapper;
import io.choerodon.devops.infra.mapper.DevopsHostCommandMapper;
import io.choerodon.devops.infra.mapper.DevopsHostMapper;
import io.choerodon.devops.infra.util.*;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * @author zmf
 * @since 2020/9/15
 */
@Service
public class DevopsHostServiceImpl implements DevopsHostService {
    public static final String PERMISSION_LABEL = "permissionLabel";

    private static final String ERROR_HOST_NOT_FOUND = "devops.host.not.found";
    private static final String ERROR_HOST_STATUS_IS_NOT_DISCONNECT = "devops.host.status.is.not.disconnect";
    private static final String LOGIN_NAME = "loginName";
    private static final String REAL_NAME = "realName";
    private static final String HOST_AGENT = "curl -Lo host.sh %s/devops/v1/projects/%d/hosts/%d/download_file/%s && sh host.sh %s";
    private static final String HOST_UNINSTALL_SHELL = "sudo systemctl stop c7n-agent";
    private static final String HOST_ACTIVATE_COMMAND_TEMPLATE;
    private static final String HOST_UPGRADE_COMMAND_TEMPLATE;

    static {
        try (InputStream inputStream = DevopsClusterServiceImpl.class.getResourceAsStream("/shell/host.sh")) {
            HOST_ACTIVATE_COMMAND_TEMPLATE = org.apache.commons.io.IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new CommonException("devops.load.host.install.sh");
        }

        try (InputStream inputStream = DevopsClusterServiceImpl.class.getResourceAsStream("/shell/host_upgrade.sh")) {
            HOST_UPGRADE_COMMAND_TEMPLATE = org.apache.commons.io.IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new CommonException("devops.load.host.upgrade.sh");
        }
    }

    private final Gson gson = new Gson();
    @Autowired
    EncryptService encryptService;
    @Value("${services.gateway.url}")
    private String apiHost;
    @Value("${devops.host.binary-download-url}")
    private String binaryDownloadUrl;
    @Value("${agent.serviceUrl}")
    private String agentServiceUrl;
    @Value("${devops.host.agent-version}")
    private String agentVersion;
    @Autowired
    private DevopsHostMapper devopsHostMapper;
    @Lazy
    @Autowired
    private DevopsHostAdditionalCheckValidator devopsHostAdditionalCheckValidator;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private DevopsHostCommandService devopsHostCommandService;
    @Autowired
    private KeySocketSendHelper webSocketHelper;
    @Autowired
    @Lazy
    private DevopsDockerInstanceService devopsDockerInstanceService;
    @Autowired
    @Lazy
    private DevopsHostAppService devopsHostAppService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private DevopsDockerInstanceMapper devopsDockerInstanceMapper;
    @Autowired
    private DevopsHostAppMapper devopsHostAppMapper;
    @Autowired
    private SshUtil sshUtil;
    @Autowired
    private DevopsHostCommandMapper devopsHostCommandMapper;
    @Autowired
    @Lazy
    private DevopsHostUserPermissionService devopsHostUserPermissionService;
    @Autowired
    private UserAttrService userAttrService;
    @Autowired
    private PermissionHelper permissionHelper;
    @Autowired
    @Lazy
    private HostConnectionHandler hostConnectionHandler;

    @NotNull
    private static Supplier<String> handHostCheckMsg(List<CiCdPipelineDTO> ciCdPipelineDTOS) {
        return () -> {
            throw new CommonException("devops.host.linked.pipeline.delete", ciCdPipelineDTOS.get(0) == null ? "" : ciCdPipelineDTOS.get(0).getName());
        };
    }

    /**
     * 获得agent安装命令
     *
     * @return agent安装命令
     */
    @Override
    public String getInstallString(Long projectId, DevopsHostDTO devopsHostDTO) {
        Map<String, String> params = new HashMap<>();
        // 渲染激活环境的命令参数
        params.put("{{ TOKEN }}", devopsHostDTO.getToken());
        params.put("{{ CONNECT }}", agentServiceUrl);
        params.put("{{ HOST_ID }}", devopsHostDTO.getId().toString());
        params.put("{{ BINARY }}", binaryDownloadUrl);
        params.put("{{ VERSION }}", agentVersion);
        return FileUtil.replaceReturnString(HOST_ACTIVATE_COMMAND_TEMPLATE, params);
    }

    @Override
    public String getUpgradeString(Long projectId, DevopsHostDTO devopsHostDTO) {
        Map<String, String> params = new HashMap<>();
        // 渲染激活环境的命令参数
        params.put("{{ TOKEN }}", devopsHostDTO.getToken());
        params.put("{{ CONNECT }}", agentServiceUrl);
        params.put("{{ HOST_ID }}", devopsHostDTO.getId().toString());
        params.put("{{ BINARY }}", binaryDownloadUrl);
        params.put("{{ VERSION }}", agentVersion);
        return FileUtil.replaceReturnString(HOST_UPGRADE_COMMAND_TEMPLATE, params);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public DevopsHostVO createHost(Long projectId, DevopsHostCreateRequestVO devopsHostCreateRequestVO) {
        // 补充校验参数
        devopsHostAdditionalCheckValidator.validNameProjectUnique(projectId, devopsHostCreateRequestVO.getName());
        if (devopsHostAdditionalCheckValidator.validIpAndSshPortComplete(devopsHostCreateRequestVO)) {
            devopsHostAdditionalCheckValidator.validHostInformationMatch(devopsHostCreateRequestVO);
        }
        DevopsHostDTO devopsHostDTO = ConvertUtils.convertObject(devopsHostCreateRequestVO, DevopsHostDTO.class);
        devopsHostDTO.setProjectId(projectId);

        devopsHostDTO.setHostStatus(DevopsHostStatus.DISCONNECT.getValue());
        devopsHostDTO.setToken(GenerateUUID.generateUUID().replaceAll("-", ""));
        return ConvertUtils.convertObject(MapperUtil.resultJudgedInsert(devopsHostMapper, devopsHostDTO, "devops.insert.host"), DevopsHostVO.class);
    }

    @Override
    public DevopsHostVO queryHost(Long projectId, Long hostId) {
        DevopsHostDTO devopsHostDTO = devopsHostMapper.selectByPrimaryKey(hostId);
        devopsHostUserPermissionService.checkUserOwnManagePermissionOrThrow(projectId, devopsHostDTO, DetailsHelper.getUserDetails().getUserId());
        if (devopsHostDTO == null || !projectId.equals(devopsHostDTO.getProjectId())) {
            return null;
        }

        return ConvertUtils.convertObject(devopsHostDTO, DevopsHostVO.class);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteHost(Long projectId, Long hostId) {
        DevopsHostDTO devopsHostDTO = devopsHostMapper.selectByPrimaryKey(hostId);
        if (devopsHostDTO == null) return;
        devopsHostUserPermissionService.checkUserOwnManagePermissionOrThrow(projectId, devopsHostDTO, DetailsHelper.getUserDetails().getUserId());
        //不在校验主机的连接状态
//        checkEnableHostDelete(hostId);
        //校验流水线是否引用了该主机
        List<CiCdPipelineDTO> ciCdPipelineDTOS = devopsHostMapper.selectPipelineByHostId(hostId);
        AssertUtils.isTrue(CollectionUtils.isEmpty(ciCdPipelineDTOS), handHostCheckMsg(ciCdPipelineDTOS));
        CommonExAssertUtil.assertTrue(devopsHostDTO.getProjectId().equals(projectId), MiscConstants.DEVOPS_OPERATING_RESOURCE_IN_OTHER_PROJECT);
        try {
            devopsHostMapper.deleteByPrimaryKey(hostId);
//            devopsDockerInstanceMapper.deleteByHostId(hostId);
            devopsHostCommandMapper.deleteByHostId(hostId);
            devopsHostAppMapper.deleteByHostId(hostId);
        } catch (Exception exception) {
            throw new CommonException("falied to delete host");
        }
    }

    private void checkEnableHostDelete(Long hostId) {
        DevopsHostDTO devopsHostDTO = devopsHostMapper.selectByPrimaryKey(hostId);
        if (devopsHostDTO == null) {
            throw new CommonException(ERROR_HOST_NOT_FOUND);
        }
        if (DevopsHostStatus.CONNECTED.getValue().equals(devopsHostDTO.getHostStatus())) {
            throw new CommonException(ERROR_HOST_STATUS_IS_NOT_DISCONNECT);
        }
    }

    @Override
    public DevopsHostConnectionTestResultVO testConnection(Long projectId, DevopsHostConnectionTestVO
            devopsHostConnectionTestVO) {
        SSHClient sshClient = null;
        try {
            DevopsHostConnectionTestResultVO result = new DevopsHostConnectionTestResultVO();
            sshClient = SshUtil.sshConnect(devopsHostConnectionTestVO.getHostIp(), devopsHostConnectionTestVO.getSshPort(), devopsHostConnectionTestVO.getAuthType(), devopsHostConnectionTestVO.getUsername(), devopsHostConnectionTestVO.getPassword());
            result.setHostStatus(sshClient != null ? DevopsHostStatus.SUCCESS.getValue() : DevopsHostStatus.FAILED.getValue());
            if (sshClient == null) {
                result.setHostCheckError("failed to check ssh, please ensure network and authentication is valid");
            }
            return result;
        } finally {
            IOUtils.closeQuietly(sshClient);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateHost(Long projectId, Long hostId, DevopsHostUpdateRequestVO devopsHostUpdateRequestVO) {
        DevopsHostDTO devopsHostDTO = devopsHostMapper.selectByPrimaryKey(hostId);
        CommonExAssertUtil.assertNotNull(devopsHostDTO, "devops.host.not.exist", hostId);
        CommonExAssertUtil.assertTrue(devopsHostDTO.getProjectId().equals(projectId), MiscConstants.DEVOPS_OPERATING_RESOURCE_IN_OTHER_PROJECT);
        devopsHostUserPermissionService.checkUserOwnManagePermissionOrThrow(projectId, devopsHostDTO, DetailsHelper.getUserDetails().getUserId());
        devopsHostAdditionalCheckValidator.validUsernamePasswordMatch(devopsHostUpdateRequestVO.getUsername(), devopsHostUpdateRequestVO.getPassword());

        // 补充校验参数
        if (!devopsHostDTO.getName().equals(devopsHostUpdateRequestVO.getName())) {
            devopsHostAdditionalCheckValidator.validNameProjectUnique(projectId, devopsHostUpdateRequestVO.getName());
        }

        devopsHostDTO.setName(devopsHostUpdateRequestVO.getName());
        devopsHostDTO.setUsername(devopsHostUpdateRequestVO.getUsername());
        devopsHostDTO.setPassword(devopsHostUpdateRequestVO.getPassword());
        devopsHostDTO.setAuthType(devopsHostUpdateRequestVO.getAuthType());
        devopsHostDTO.setHostIp(devopsHostUpdateRequestVO.getHostIp());
        devopsHostDTO.setSshPort(devopsHostUpdateRequestVO.getSshPort());
        devopsHostDTO.setDescription(devopsHostUpdateRequestVO.getDescription());
        MapperUtil.resultJudgedUpdateByPrimaryKey(devopsHostMapper, devopsHostDTO, "devops.update.host");
    }

    @Override
    public Boolean testConnectionByIdForDeployHost(Long projectId, Long hostId) {
        DevopsHostDTO devopsHostDTO = devopsHostMapper.selectByPrimaryKey(hostId);
        if (devopsHostDTO == null) {
            return false;
        }

        CommonExAssertUtil.assertTrue(projectId.equals(devopsHostDTO.getProjectId()), MiscConstants.DEVOPS_OPERATING_RESOURCE_IN_OTHER_PROJECT);
        CommonExAssertUtil.assertTrue(DevopsHostType.DEPLOY.getValue().equals(devopsHostDTO.getType()), "devops.host.type.invalid");

        return SshUtil.sshConnectForOK(devopsHostDTO.getHostIp(), devopsHostDTO.getSshPort(), devopsHostDTO.getAuthType(), devopsHostDTO.getUsername(), devopsHostDTO.getPassword());
    }

    @Override
    public boolean isNameUnique(Long projectId, String name) {
        DevopsHostDTO condition = new DevopsHostDTO();
        condition.setProjectId(Objects.requireNonNull(projectId));
        condition.setName(Objects.requireNonNull(name));
        return devopsHostMapper.selectCount(condition) == 0;
    }

    @Override
    public boolean isSshIpPortUnique(Long projectId, String ip, Integer sshPort) {
        DevopsHostDTO condition = new DevopsHostDTO();
        condition.setProjectId(Objects.requireNonNull(projectId));
        condition.setHostIp(Objects.requireNonNull(ip));
        condition.setSshPort(Objects.requireNonNull(sshPort));
        return devopsHostMapper.selectCount(condition) == 0;
    }

    @Override
    public boolean hostIdInstanceIdMatch(Long hostId, Long instanceId) {
        DevopsHostAppDTO devopsHostAppDTO = devopsHostAppMapper.selectByPrimaryKey(instanceId);
        return devopsHostAppDTO != null && devopsHostAppDTO.getHostId().equals(hostId);
    }

    @Override
    public boolean hostIdDockerInstanceMatch(Long hostId, Long instanceId) {
        DevopsDockerInstanceDTO devopsDockerInstanceDTO = devopsDockerInstanceMapper.selectByPrimaryKey(instanceId);
        return devopsDockerInstanceDTO != null && devopsDockerInstanceDTO.getHostId().equals(hostId);
    }

    @Override
    public Page<DevopsHostVO> pageByOptions(Long projectId, PageRequest pageRequest, boolean withCreatorInfo,
                                            @Nullable String searchParam, @Nullable String hostStatus, @Nullable Boolean doPage) {
        boolean projectOwnerOrRoot = permissionHelper.isGitlabProjectOwnerOrGitlabAdmin(projectId);
        // 解析查询参数
        Page<DevopsHostVO> page;
        List<DevopsHostVO> devopsHostVOList;
        Map<Long, DevopsHostUserPermissionDTO> hostPermissionMap = new HashMap<>();
        if (projectOwnerOrRoot) {
            devopsHostVOList = devopsHostMapper.listByOptions(projectId, searchParam);
        } else {
            devopsHostVOList = devopsHostMapper.listMemberHostByOptions(projectId, searchParam, DetailsHelper.getUserDetails().getUserId());
            if (CollectionUtils.isEmpty(devopsHostVOList)) {
                return new Page<>();
            }
            hostPermissionMap = devopsHostUserPermissionService.listUserHostPermissionByUserIdAndHostIds(DetailsHelper.getUserDetails().getUserId(), devopsHostVOList.stream().map(DevopsHostVO::getId).collect(Collectors.toList()))
                    .stream()
                    .collect(Collectors.toMap(DevopsHostUserPermissionDTO::getHostId, Function.identity()));
        }

        List<Long> updatedClusterList = hostConnectionHandler.getUpdatedHostList();
        Map<Long, DevopsHostUserPermissionDTO> finalHostPermissionMap = hostPermissionMap;
        devopsHostVOList = devopsHostVOList.stream()
                .peek(h -> h.setHostStatus(updatedClusterList.contains(h.getId()) ? DevopsHostStatus.CONNECTED.getValue() : DevopsHostStatus.DISCONNECT.getValue()))
                .sorted(Comparator.comparing(DevopsHostVO::getHostStatus))
                .peek(h -> {
                    // 如果是项目所有者、root、创建者，设置administrator标签
                    if (projectOwnerOrRoot || h.getCreatedBy().equals(DetailsHelper.getUserDetails().getUserId())) {
                        h.setPermissionLabel(DevopsHostUserPermissionLabelEnums.ADMINISTRATOR.getValue());
                    } else {
                        // 项目成员且为主机创建者，展示权限管理tab和按钮
                        // 仅仅是项目成员，不展示权限管理tab和按钮
                        h.setPermissionLabel(finalHostPermissionMap.get(h.getId()).getPermissionLabel());
                    }
                }).collect(Collectors.toList());
        if (hostStatus != null) {
            devopsHostVOList = devopsHostVOList.stream().filter(v -> v.getHostStatus().equals(hostStatus)).collect(Collectors.toList());
        }
        if (doPage != null && doPage) {
            page = PageUtils.createPageFromList(devopsHostVOList, pageRequest);
        } else {
            page = new Page<>();
            page.setContent(devopsHostVOList);
        }

        // 分页查询
        if (withCreatorInfo) {
            // 填充创建者用户信息
            UserDTOFillUtil.fillUserInfo(page.getContent(), "createdBy", "creatorInfo");
        }
        return page;
    }

    @Override
    public boolean checkHostDelete(Long projectId, Long hostId) {
        DevopsHostDTO devopsHostDTO = devopsHostMapper.selectByPrimaryKey(hostId);
        if (Objects.isNull(devopsHostDTO)) return Boolean.TRUE;
        //主机关联流水线任务不能删除
        List<CiCdPipelineDTO> ciCdPipelineDTOS = devopsHostMapper.selectPipelineByHostId(hostId);
        if (!CollectionUtils.isEmpty(ciCdPipelineDTOS)) return Boolean.FALSE;
        return Boolean.TRUE;
    }

    @Override
    public DevopsHostDTO baseQuery(Long hostId) {
        return devopsHostMapper.selectByPrimaryKey(hostId);
    }

    @Override
    public List<DevopsJavaInstanceVO> listJavaProcessInfo(Long projectId, Long hostId) {
        DevopsHostDTO devopsHostDTO = baseQuery(hostId);
        devopsHostUserPermissionService.checkUserOwnUsePermissionOrThrow(projectId, devopsHostDTO, DetailsHelper.getUserDetails().getUserId());

        List<DevopsHostAppDTO> devopsHostAppDTOList = devopsHostAppService.listByHostId(hostId);
        if (CollectionUtils.isEmpty(devopsHostAppDTOList)) {
            return new ArrayList<>();
        }

        List<DevopsJavaInstanceVO> devopsJavaInstanceVOS = ConvertUtils.convertList(devopsHostAppDTOList, DevopsJavaInstanceVO.class);

        UserDTOFillUtil.fillUserInfo(devopsJavaInstanceVOS, "createdBy", "deployer");
        return devopsJavaInstanceVOS;
    }

    @Override
    public List<DevopsDockerInstanceVO> listDockerProcessInfo(Long projectId, Long hostId) {
        DevopsHostDTO devopsHostDTO = baseQuery(hostId);
        devopsHostUserPermissionService.checkUserOwnUsePermissionOrThrow(projectId, devopsHostDTO, DetailsHelper.getUserDetails().getUserId());

        List<DevopsDockerInstanceDTO> devopsDockerInstanceDTOList = devopsDockerInstanceService.listByHostId(hostId);
        if (CollectionUtils.isEmpty(devopsDockerInstanceDTOList)) {
            return new ArrayList<>();
        }
        List<DevopsDockerInstanceVO> devopsDockerInstanceVOS = ConvertUtils.convertList(devopsDockerInstanceDTOList, DevopsDockerInstanceVO.class);

        devopsDockerInstanceVOS.forEach(devopsDockerInstanceVO -> {
            if (StringUtils.isNoneBlank(devopsDockerInstanceVO.getPorts())) {
                devopsDockerInstanceVO.setPortMappingList(JsonHelper.unmarshalByJackson(devopsDockerInstanceVO.getPorts(), new TypeReference<List<DockerPortMapping>>() {
                }));
            }

        });

        UserDTOFillUtil.fillUserInfo(devopsDockerInstanceVOS, "createdBy", "deployer");
        return devopsDockerInstanceVOS;
    }

    @Override
    @Transactional
    public void deleteDockerProcess(Long projectId, Long hostId, Long instanceId) {
        DevopsHostDTO devopsHostDTO = baseQuery(hostId);
        devopsHostUserPermissionService.checkUserOwnUsePermissionOrThrow(projectId, devopsHostDTO, DetailsHelper.getUserDetails().getUserId());
        devopsHostAdditionalCheckValidator.validHostIdAndDockerInstanceIdMatch(hostId, instanceId);
        DevopsDockerInstanceDTO dockerInstanceDTO = devopsDockerInstanceMapper.selectByPrimaryKey(instanceId);
        if (dockerInstanceDTO.getContainerId() == null) {
            devopsDockerInstanceMapper.deleteByPrimaryKey(instanceId);
            return;
        }
        //删除host app

        DevopsHostAppDTO devopsHostAppDTO = devopsHostAppMapper.selectByPrimaryKey(dockerInstanceDTO.getAppId());

        devopsHostAppMapper.deleteByPrimaryKey(dockerInstanceDTO.getAppId());

        DevopsHostCommandDTO devopsHostCommandDTO = new DevopsHostCommandDTO();
        devopsHostCommandDTO.setCommandType(HostCommandEnum.REMOVE_DOCKER.value());
        devopsHostCommandDTO.setHostId(hostId);
        devopsHostCommandDTO.setInstanceType(HostResourceType.DOCKER_PROCESS.value());
        devopsHostCommandDTO.setInstanceId(instanceId);
        devopsHostCommandDTO.setStatus(HostCommandStatusEnum.OPERATING.value());
        devopsHostCommandService.baseCreate(devopsHostCommandDTO);


        HostAgentMsgVO hostAgentMsgVO = new HostAgentMsgVO();
        hostAgentMsgVO.setHostId(String.valueOf(hostId));
        hostAgentMsgVO.setType(HostCommandEnum.REMOVE_DOCKER.value());
        hostAgentMsgVO.setCommandId(String.valueOf(devopsHostCommandDTO.getId()));


        DockerProcessInfoVO dockerProcessInfoVO = new DockerProcessInfoVO();
        dockerProcessInfoVO.setInstanceId(String.valueOf(instanceId));
        dockerProcessInfoVO.setContainerId(dockerInstanceDTO.getContainerId());
        dockerProcessInfoVO.setVersion(devopsHostAppDTO.getVersion());
        dockerProcessInfoVO.setAppCode(devopsHostAppDTO.getCode());
        hostAgentMsgVO.setPayload(JsonHelper.marshalByJackson(dockerProcessInfoVO));

        webSocketHelper.sendByGroup(DevopsHostConstants.GROUP + hostId, DevopsHostConstants.GROUP + hostId, JsonHelper.marshalByJackson(hostAgentMsgVO));

    }

    @Override
    @Transactional
    public void stopDockerProcess(Long projectId, Long hostId, Long instanceId) {
        DevopsHostDTO devopsHostDTO = baseQuery(hostId);
        devopsHostUserPermissionService.checkUserOwnUsePermissionOrThrow(projectId, devopsHostDTO, DetailsHelper.getUserDetails().getUserId());

        devopsHostAdditionalCheckValidator.validHostIdAndDockerInstanceIdMatch(hostId, instanceId);
        DevopsHostCommandDTO devopsHostCommandDTO = new DevopsHostCommandDTO();
        devopsHostCommandDTO.setCommandType(HostCommandEnum.STOP_DOCKER.value());
        devopsHostCommandDTO.setHostId(hostId);
        devopsHostCommandDTO.setInstanceType(HostResourceType.DOCKER_PROCESS.value());
        devopsHostCommandDTO.setInstanceId(instanceId);
        devopsHostCommandDTO.setStatus(HostCommandStatusEnum.OPERATING.value());
        devopsHostCommandService.baseCreate(devopsHostCommandDTO);


        HostAgentMsgVO hostAgentMsgVO = new HostAgentMsgVO();
        hostAgentMsgVO.setHostId(String.valueOf(hostId));
        hostAgentMsgVO.setType(HostCommandEnum.STOP_DOCKER.value());
        hostAgentMsgVO.setCommandId(String.valueOf(devopsHostCommandDTO.getId()));

        DevopsDockerInstanceDTO devopsDockerInstanceDTO = devopsDockerInstanceMapper.selectByPrimaryKey(instanceId);
        DevopsHostAppDTO devopsHostAppDTO = devopsHostAppMapper.selectByPrimaryKey(devopsDockerInstanceDTO.getAppId());

        DockerProcessInfoVO dockerProcessInfoVO = new DockerProcessInfoVO();
        dockerProcessInfoVO.setInstanceId(String.valueOf(instanceId));
        dockerProcessInfoVO.setContainerId(devopsDockerInstanceDTO.getContainerId());
        dockerProcessInfoVO.setVersion(devopsHostAppDTO.getVersion());
        dockerProcessInfoVO.setAppCode(devopsHostAppDTO.getCode());
        hostAgentMsgVO.setPayload(JsonHelper.marshalByJackson(dockerProcessInfoVO));

        webSocketHelper.sendByGroup(DevopsHostConstants.GROUP + hostId, DevopsHostConstants.GROUP + hostId, JsonHelper.marshalByJackson(hostAgentMsgVO));

    }

    @Override
    @Transactional
    public void restartDockerProcess(Long projectId, Long hostId, Long instanceId) {
        DevopsHostDTO devopsHostDTO = baseQuery(hostId);
        devopsHostUserPermissionService.checkUserOwnUsePermissionOrThrow(projectId, devopsHostDTO, DetailsHelper.getUserDetails().getUserId());

        devopsHostAdditionalCheckValidator.validHostIdAndDockerInstanceIdMatch(hostId, instanceId);
        DevopsHostCommandDTO devopsHostCommandDTO = new DevopsHostCommandDTO();
        devopsHostCommandDTO.setCommandType(HostCommandEnum.RESTART_DOCKER.value());
        devopsHostCommandDTO.setHostId(hostId);
        devopsHostCommandDTO.setInstanceType(HostResourceType.DOCKER_PROCESS.value());
        devopsHostCommandDTO.setInstanceId(instanceId);
        devopsHostCommandDTO.setStatus(HostCommandStatusEnum.OPERATING.value());
        devopsHostCommandService.baseCreate(devopsHostCommandDTO);


        HostAgentMsgVO hostAgentMsgVO = new HostAgentMsgVO();
        hostAgentMsgVO.setHostId(String.valueOf(hostId));
        hostAgentMsgVO.setType(HostCommandEnum.RESTART_DOCKER.value());
        hostAgentMsgVO.setCommandId(String.valueOf(devopsHostCommandDTO.getId()));

        DevopsDockerInstanceDTO devopsDockerInstanceDTO = devopsDockerInstanceMapper.selectByPrimaryKey(instanceId);
        DevopsHostAppDTO devopsHostAppDTO = devopsHostAppMapper.selectByPrimaryKey(devopsDockerInstanceDTO.getAppId());

        DockerProcessInfoVO dockerProcessInfoVO = new DockerProcessInfoVO();
        dockerProcessInfoVO.setInstanceId(String.valueOf(instanceId));
        dockerProcessInfoVO.setContainerId(devopsDockerInstanceDTO.getContainerId());
        dockerProcessInfoVO.setVersion(devopsHostAppDTO.getVersion());
        dockerProcessInfoVO.setAppCode(devopsHostAppDTO.getCode());

        hostAgentMsgVO.setPayload(JsonHelper.marshalByJackson(dockerProcessInfoVO));

        webSocketHelper.sendByGroup(DevopsHostConstants.GROUP + hostId, DevopsHostConstants.GROUP + hostId, JsonHelper.marshalByJackson(hostAgentMsgVO));

    }

    @Override
    @Transactional
    public void startDockerProcess(Long projectId, Long hostId, Long instanceId) {
        DevopsHostDTO devopsHostDTO = baseQuery(hostId);
        devopsHostUserPermissionService.checkUserOwnUsePermissionOrThrow(projectId, devopsHostDTO, DetailsHelper.getUserDetails().getUserId());

        devopsHostAdditionalCheckValidator.validHostIdAndDockerInstanceIdMatch(hostId, instanceId);
        DevopsHostCommandDTO devopsHostCommandDTO = new DevopsHostCommandDTO();
        devopsHostCommandDTO.setCommandType(HostCommandEnum.START_DOCKER.value());
        devopsHostCommandDTO.setHostId(hostId);
        devopsHostCommandDTO.setInstanceType(HostResourceType.DOCKER_PROCESS.value());
        devopsHostCommandDTO.setInstanceId(instanceId);
        devopsHostCommandDTO.setStatus(HostCommandStatusEnum.OPERATING.value());
        devopsHostCommandService.baseCreate(devopsHostCommandDTO);


        HostAgentMsgVO hostAgentMsgVO = new HostAgentMsgVO();
        hostAgentMsgVO.setHostId(String.valueOf(hostId));
        hostAgentMsgVO.setType(HostCommandEnum.START_DOCKER.value());
        hostAgentMsgVO.setCommandId(String.valueOf(devopsHostCommandDTO.getId()));

        DevopsDockerInstanceDTO devopsDockerInstanceDTO = devopsDockerInstanceMapper.selectByPrimaryKey(instanceId);
        DevopsHostAppDTO devopsHostAppDTO = devopsHostAppMapper.selectByPrimaryKey(devopsDockerInstanceDTO.getAppId());

        DockerProcessInfoVO dockerProcessInfoVO = new DockerProcessInfoVO();
        dockerProcessInfoVO.setInstanceId(String.valueOf(instanceId));
        dockerProcessInfoVO.setContainerId(devopsDockerInstanceDTO.getContainerId());
        dockerProcessInfoVO.setVersion(devopsHostAppDTO.getVersion());
        dockerProcessInfoVO.setAppCode(devopsHostAppDTO.getCode());

        hostAgentMsgVO.setPayload(JsonHelper.marshalByJackson(dockerProcessInfoVO));

        webSocketHelper.sendByGroup(DevopsHostConstants.GROUP + hostId, DevopsHostConstants.GROUP + hostId, JsonHelper.marshalByJackson(hostAgentMsgVO));

    }

    @Override
    public String downloadCreateHostFile(Long projectId, Long hostId, String token) {
        DevopsHostDTO devopsHostDTO = new DevopsHostDTO();
        devopsHostDTO.setId(hostId);
        devopsHostDTO = devopsHostMapper.selectByPrimaryKey(devopsHostDTO);
        String response = null;
        if (devopsHostDTO.getToken().equals(token)) {
            response = getInstallString(projectId, devopsHostDTO);
        }
        return response;
    }

    @Override
    public ResourceUsageInfoVO queryResourceUsageInfo(Long projectId, Long hostId) {
        DevopsHostDTO devopsHostDTO = baseQuery(hostId);
        devopsHostUserPermissionService.checkUserOwnUsePermissionOrThrow(projectId, devopsHostDTO, DetailsHelper.getUserDetails().getUserId());

        String resourceInfo = stringRedisTemplate.opsForValue().get(String.format(DevopsHostConstants.HOST_RESOURCE_INFO_KEY, hostId));
        if (StringUtils.isEmpty(resourceInfo)) {
            return new ResourceUsageInfoVO();
        }
        return JsonHelper.unmarshalByJackson(resourceInfo, ResourceUsageInfoVO.class);
    }

    @Override
    public String queryShell(Long projectId, Long hostId, Boolean queryForAutoUpdate, String previousAgentVersion) {
        DevopsHostDTO devopsHostDTO = baseQuery(hostId);
        // 如果是agent自动升级查询shell，那么不进行权限校验
        if (!queryForAutoUpdate) {
            devopsHostUserPermissionService.checkUserOwnManagePermissionOrThrow(projectId, devopsHostDTO, DetailsHelper.getUserDetails().getUserId());
        }
        return String.format(HOST_AGENT, apiHost, projectId, hostId, devopsHostDTO.getToken(), ObjectUtils.isEmpty(previousAgentVersion) ? "" : previousAgentVersion);
    }

    @Override
    public String queryUninstallShell(Long projectId, Long hostId) {
        DevopsHostDTO devopsHostDTO = baseQuery(hostId);
        devopsHostUserPermissionService.checkUserOwnManagePermissionOrThrow(projectId, devopsHostDTO, DetailsHelper.getUserDetails().getUserId());
        return HOST_UNINSTALL_SHELL;
    }

    @Override
    public void connectHost(Long projectId, Long hostId, DevopsHostConnectionVO devopsHostConnectionVO) {
        String redisKey = generateRedisKey(projectId, hostId);
        if (checkRedisStatusOperating(redisKey)) {
            return;
        }
        Map<String, String> map = new HashMap<>();
        String command = queryShell(projectId, hostId, false, "");
        redisTemplate.opsForHash().putAll(redisKey, createMap(map, DevopsHostStatus.OPERATING.getValue(), null));
        automaticHost(devopsHostConnectionVO, map, redisKey, command);
    }

    @Override
    public Map<Object, Object> queryConnectHost(Long projectId, Long hostId) {
        return redisTemplate.opsForHash().entries(generateRedisKey(projectId, hostId));
    }

    @Override
    public Map<String, String> testConnectHost(Long projectId, Long hostId, DevopsHostConnectionVO devopsHostConnectionVO) {
        devopsHostAdditionalCheckValidator.validHostInformationMatch(Objects.requireNonNull(ConvertUtils.convertObject(devopsHostConnectionVO, DevopsHostCreateRequestVO.class)));
        Map<String, String> map = new HashMap<>();
        SSHClient sshClient = null;
        try {
            sshClient = SshUtil.sshConnect(devopsHostConnectionVO.getHostIp(), devopsHostConnectionVO.getSshPort(), devopsHostConnectionVO.getAuthType(), devopsHostConnectionVO.getUsername(), devopsHostConnectionVO.getPassword());
            if (sshClient != null) {
                createMap(map, DevopsHostStatus.SUCCESS.getValue(), null);
            } else {
                createMap(map, DevopsHostStatus.FAILED.getValue(), "failed to check ssh, please ensure network and authentication is valid");
            }
        } finally {
            IOUtils.closeQuietly(sshClient);
        }
        return map;
    }

    @Override
    public String disconnectionHost() {
        return HOST_UNINSTALL_SHELL;
    }

    @Override
    public Page<DevopsHostUserPermissionVO> pageUserPermissionByHostId(Long projectId, PageRequest pageable, String params, Long hostId) {
        DevopsHostDTO devopsHostDTO = devopsHostMapper.selectByPrimaryKey(hostId);
        if (devopsHostDTO == null) {
            return new Page<>();
        }
        devopsHostUserPermissionService.checkUserOwnManagePermissionOrThrow(projectId, devopsHostDTO, DetailsHelper.getUserDetails().getUserId());

        RoleAssignmentSearchVO roleAssignmentSearchVO = new RoleAssignmentSearchVO();
        roleAssignmentSearchVO.setEnabled(true);
        Map<String, Object> searchParamMap = null;
        List<String> paramList = null;
        // 处理搜索参数
        if (!org.springframework.util.StringUtils.isEmpty(params)) {
            Map maps = gson.fromJson(params, Map.class);
            searchParamMap = TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM));
            paramList = TypeUtil.cast(maps.get(TypeUtil.PARAMS));
            roleAssignmentSearchVO.setParam(paramList == null ? null : paramList.toArray(new String[0]));
            if (searchParamMap != null) {
                if (searchParamMap.get(LOGIN_NAME) != null) {
                    String loginName = TypeUtil.objToString(searchParamMap.get(LOGIN_NAME));
                    roleAssignmentSearchVO.setLoginName(loginName);
                }
                if (searchParamMap.get(REAL_NAME) != null) {
                    String realName = TypeUtil.objToString(searchParamMap.get(REAL_NAME));
                    roleAssignmentSearchVO.setRealName(realName);
                }
            }
        }

        // 根据搜索参数查询所有的项目所有者
        List<DevopsUserPermissionVO> projectOwners = ConvertUtils.convertList(baseServiceClientOperator.listUsersWithGitlabLabel(projectId, roleAssignmentSearchVO, LabelType.GITLAB_PROJECT_OWNER.getValue()),
                iamUserDTO -> DevopsUserPermissionVO.iamUserTOUserPermissionVO(iamUserDTO, true));
        List<DevopsUserPermissionVO> projectMembers = ConvertUtils.convertList(baseServiceClientOperator.listUsersWithGitlabLabel(projectId, roleAssignmentSearchVO, LabelType.GITLAB_PROJECT_DEVELOPER.getValue()),
                iamUserDTO -> DevopsUserPermissionVO.iamUserTOUserPermissionVO(iamUserDTO, false));
        // 根据搜索参数查询数据库中所有的主机权限分配数据
        List<DevopsHostUserPermissionDTO> devopsHostUserPermissionDTOList = devopsHostUserPermissionService.listUserHostPermissionByOption(hostId, searchParamMap, paramList);
        Map<Long, DevopsHostUserPermissionDTO> devopsHostUserPermissionDTOMap = devopsHostUserPermissionDTOList
                .stream()
                .collect(Collectors.toMap(DevopsHostUserPermissionDTO::getIamUserId, Function.identity()));
        List<Long> iamUserIdsWithHostPermission = devopsHostUserPermissionDTOList.stream().map(DevopsHostUserPermissionDTO::getIamUserId).collect(Collectors.toList());

        List<DevopsHostUserPermissionVO> projectOwnerHostPermissionVO = ConvertUtils.convertList(projectOwners, DevopsHostUserPermissionVO.class)
                .stream()
                .peek(u -> u.setPermissionLabel(DevopsHostUserPermissionLabelEnums.ADMINISTRATOR.getValue()))
                .collect(Collectors.toList());

        Set<Long> userIds = projectMembers.stream().map(DevopsUserVO::getIamUserId).collect(Collectors.toSet());
        Map<Long, Boolean> userGitlabProjectOwnerMap = baseServiceClientOperator.checkUsersAreGitlabProjectOwner(userIds, projectId);

        List<DevopsHostUserPermissionVO> projectMemberHostPermissionVO = new ArrayList<>();
        projectMembers = projectMembers
                .stream()
                .filter(member -> iamUserIdsWithHostPermission.contains(member.getIamUserId()) || Boolean.TRUE.equals(userGitlabProjectOwnerMap.get(member.getIamUserId())) || member.getIamUserId().equals(devopsHostDTO.getCreatedBy()))
                .collect(Collectors.toList());
        projectMembers.forEach(devopsUserPermissionVO -> {
            if (devopsHostDTO.getCreatedBy().equals(devopsUserPermissionVO.getIamUserId())) {
                DevopsHostUserPermissionVO devopsHostUserPermissionVO = ConvertUtils.convertObject(devopsUserPermissionVO, DevopsHostUserPermissionVO.class);
                devopsHostUserPermissionVO.setPermissionLabel(DevopsHostUserPermissionLabelEnums.ADMINISTRATOR.getValue());
                projectMemberHostPermissionVO.add(devopsHostUserPermissionVO);
            } else if (iamUserIdsWithHostPermission.contains(devopsUserPermissionVO.getIamUserId())) {
                DevopsHostUserPermissionDTO specifiedDevopsHostUserPermissionDTO = devopsHostUserPermissionDTOMap.get(devopsUserPermissionVO.getIamUserId());
                if (specifiedDevopsHostUserPermissionDTO != null) {
                    DevopsHostUserPermissionVO devopsHostUserPermissionVO = ConvertUtils.convertObject(devopsUserPermissionVO, DevopsHostUserPermissionVO.class);
                    devopsHostUserPermissionVO.setPermissionLabel(specifiedDevopsHostUserPermissionDTO.getPermissionLabel());
                    devopsHostUserPermissionVO.setCreationDate(specifiedDevopsHostUserPermissionDTO.getCreationDate());
                    projectMemberHostPermissionVO.add(devopsHostUserPermissionVO);
                }
            }
        });
        return DevopsHostUserPermissionVO.combine(projectMemberHostPermissionVO, projectOwnerHostPermissionVO, projectMembers, pageable, devopsHostDTO.getCreatedBy(), searchParamMap);
    }

    public Set<Object> multiTestConnection(Long projectId, Set<Long> hostIds) {
        List<DevopsHostDTO> devopsHostDTOList = devopsHostMapper.listByProjectIdAndIds(projectId, hostIds);
        CommonExAssertUtil.assertTrue(devopsHostDTOList.size() > 0, "devops.component.host.size");
        Set<Long> connectionFailedHostIds = new HashSet<>();
        devopsHostDTOList.forEach(d -> {
            DevopsHostConnectionTestResultVO devopsHostConnectionTestResultVO = testConnection(projectId, ConvertUtils.convertObject(d, DevopsHostConnectionTestVO.class));
            if (!DevopsHostStatus.SUCCESS.getValue().equals(devopsHostConnectionTestResultVO.getHostStatus())) {
                connectionFailedHostIds.add(d.getId());
            }
        });
        return encryptService.encryptIds(connectionFailedHostIds);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void batchUpdateHostUserPermission(Long projectId, DevopsHostUserPermissionUpdateVO devopsHostUserPermissionUpdateVO) {
        DevopsHostDTO preHostDTO = devopsHostMapper.selectByPrimaryKey(devopsHostUserPermissionUpdateVO.getHostId());
        // 校验主机属于该项目
        CommonExAssertUtil.assertTrue(projectId.equals(preHostDTO.getProjectId()), MiscConstants.DEVOPS_OPERATING_RESOURCE_IN_OTHER_PROJECT);
        devopsHostUserPermissionService.checkUserOwnManagePermissionOrThrow(projectId, preHostDTO, DetailsHelper.getUserDetails().getUserId());

        devopsHostUserPermissionService.deleteByHostIdAndUserIds(devopsHostUserPermissionUpdateVO.getHostId(), devopsHostUserPermissionUpdateVO.getUserIds());
        List<DevopsHostUserPermissionDTO> permissionDTOListToInsert = baseServiceClientOperator.listUsersByIds(devopsHostUserPermissionUpdateVO.getUserIds())
                .stream()
                .map(userDTO -> new DevopsHostUserPermissionDTO(userDTO.getLoginName(), userDTO.getId(), preHostDTO.getId(), userDTO.getRealName(), devopsHostUserPermissionUpdateVO.getPermissionLabel()))
                .collect(Collectors.toList());
        devopsHostUserPermissionService.batchInsert(permissionDTOListToInsert);
    }

    @Override
    public DevopsHostUserPermissionDeleteResultVO deletePermissionOfUser(Long projectId, Long hostId, Long userId) {
        DevopsHostUserPermissionDeleteResultVO deleteResultVO = new DevopsHostUserPermissionDeleteResultVO(true);
        if (hostId == null || userId == null) {
            return deleteResultVO;
        }

        DevopsHostDTO devopsHostDTO = devopsHostMapper.selectByPrimaryKey(hostId);
        devopsHostUserPermissionService.checkUserOwnManagePermissionOrThrow(projectId, devopsHostDTO, DetailsHelper.getUserDetails().getUserId());

        if (devopsHostDTO == null) {
            return deleteResultVO;
        }

        if (userId.equals(devopsHostDTO.getCreatedBy())) {
            throw new CommonException("devops.delete.permission.of.creator");
        }

        CommonExAssertUtil.assertTrue(projectId.equals(devopsHostDTO.getProjectId()), MiscConstants.DEVOPS_OPERATING_RESOURCE_IN_OTHER_PROJECT);

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(userId);

        if (userAttrDTO == null) {
            return deleteResultVO;
        }

        if (baseServiceClientOperator.isGitlabProjectOwner(userAttrDTO.getIamUserId(), projectId)) {
            throw new CommonException(DEVOPS_DELETE_PERMISSION_OF_PROJECT_OWNER);
        }

        // 删除数据库中的纪录
        DevopsHostUserPermissionDTO devopsHostUserPermissionDTO = new DevopsHostUserPermissionDTO();
        devopsHostUserPermissionDTO.setHostId(hostId);
        devopsHostUserPermissionDTO.setIamUserId(userId);
        devopsHostUserPermissionService.baseDelete(devopsHostUserPermissionDTO);

        // 表示用户删除的不是自己的权限，前端不需要刷新整个页面
        if (!userId.equals(DetailsHelper.getUserDetails().getUserId())) {
            deleteResultVO.setRefreshAll(false);
        }
        return deleteResultVO;
    }

    @Override
    public List<DevopsHostDTO> listByIds(Set<Long> ids) {
        return devopsHostMapper.selectByIds(ids.stream().map(Object::toString).collect(Collectors.joining(",")));
    }

    @Override
    public Page<DevopsUserVO> pageNonRelatedMembers(Long projectId, Long hostId, Long selectedIamUserId, PageRequest pageable, String params) {
        DevopsHostDTO devopsHostDTO = baseQuery(hostId);
        devopsHostUserPermissionService.checkUserOwnManagePermissionOrThrow(projectId, devopsHostDTO, DetailsHelper.getUserDetails().getUserId());
        List<IamUserDTO> members = listNonRelatedMembers(projectId, hostId, selectedIamUserId, params);
        Page<IamUserDTO> pageInfo = PageInfoUtil.createPageFromList(members, pageable);
        return ConvertUtils.convertPage(pageInfo, member -> new DevopsUserVO(member.getId(), member.getLdap() ? member.getLoginName() : member.getEmail(), member.getRealName(), member.getImageUrl()));
    }

    @Override
    public List<IamUserDTO> listNonRelatedMembers(Long projectId, Long hostId, Long selectedIamUserId, String params) {
        DevopsHostDTO devopsHostDTO = devopsHostMapper.selectByPrimaryKey(hostId);

        RoleAssignmentSearchVO roleAssignmentSearchVO = new RoleAssignmentSearchVO();
        roleAssignmentSearchVO.setEnabled(true);
        // 处理搜索参数
        if (!org.springframework.util.StringUtils.isEmpty(params)) {
            Map maps = gson.fromJson(params, Map.class);
            Map<String, Object> searchParamMap = TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM));
            List<String> paramList = TypeUtil.cast(maps.get(TypeUtil.PARAMS));

            roleAssignmentSearchVO.setParam(paramList == null ? null : paramList.toArray(new String[0]));
            if (searchParamMap.get(LOGIN_NAME) != null) {
                String loginName = TypeUtil.objToString(searchParamMap.get(LOGIN_NAME));
                roleAssignmentSearchVO.setLoginName(loginName);
            }

            if (searchParamMap.get(REAL_NAME) != null) {
                String realName = TypeUtil.objToString(searchParamMap.get(REAL_NAME));
                roleAssignmentSearchVO.setRealName(realName);
            }
        }

        // 根据参数搜索所有的项目成员
        List<IamUserDTO> allProjectMembers = baseServiceClientOperator.listUsersWithGitlabLabel(projectId, roleAssignmentSearchVO, LabelType.GITLAB_PROJECT_DEVELOPER.getValue());
        if (allProjectMembers.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取项目下所有的项目所有者（带上搜索参数搜索可以获得更精确的结果）
        List<Long> allProjectOwnerIds = baseServiceClientOperator.listUsersWithGitlabLabel(projectId, roleAssignmentSearchVO, LabelType.GITLAB_PROJECT_OWNER.getValue())
                .stream()
                .map(IamUserDTO::getId)
                .collect(Collectors.toList());


        // 数据库中已被分配权限的
        List<Long> assigned = devopsHostUserPermissionService.listUserIdsByHostId(hostId);

        // 过滤项目成员中的项目所有者和已被分配权限的(主机创建者默认有权限)
        List<IamUserDTO> members = allProjectMembers.stream()
                .filter(member -> !allProjectOwnerIds.contains(member.getId()))
                .filter(member -> !assigned.contains(member.getId()))
                .filter(member -> !member.getId().equals(devopsHostDTO.getCreatedBy()))
                .collect(Collectors.toList());

        if (selectedIamUserId != null) {
            IamUserDTO iamUserDTO = baseServiceClientOperator.queryUserByUserId(selectedIamUserId);
            if (!members.isEmpty()) {
                members.remove(iamUserDTO);
                members.add(0, iamUserDTO);
            } else {
                members.add(iamUserDTO);
            }
        }

        return members;
    }

    private String generateRedisKey(Long projectId, Long hostId) {
        return "host:connect:" + projectId + ":" + hostId;
    }

    @Override
    public DevopsHostDTO checkHostAvailable(Long hostId) {
        // 1. 获取主机信息
        DevopsHostDTO hostDTO = baseQuery(hostId);
        if (hostDTO == null) {
            throw new CommonException("devops.host.not.exist");
        }
        // 2. 校验主机已连接
        hostConnectionHandler.checkHostConnection(hostId);
        // 3. 校验主机权限
        devopsHostUserPermissionService.checkUserOwnUsePermissionOrThrow(hostDTO.getProjectId(), hostDTO, DetailsHelper.getUserDetails().getUserId());
        return hostDTO;
    }

    private boolean checkRedisStatusOperating(String redisKey) {
        return redisTemplate.opsForHash().hasKey(redisKey, "status") && Objects.equals(redisTemplate.opsForHash().get(redisKey, "status"), DevopsHostStatus.OPERATING.getValue());
    }

    private Map<String, String> createMap(Map<String, String> map, String status, String exception) {
        map.put("status", status);
        map.put("exception", exception);
        return map;
    }

    @Async
    void automaticHost(DevopsHostConnectionVO devopsHostConnectionVO, Map<String, String> map, String redisKey, String command) {
        devopsHostAdditionalCheckValidator.validHostInformationMatch(Objects.requireNonNull(ConvertUtils.convertObject(devopsHostConnectionVO, DevopsHostCreateRequestVO.class)));
        SSHClient sshClient = null;
        try {
            sshClient = SshUtil.sshConnect(devopsHostConnectionVO.getHostIp(), devopsHostConnectionVO.getSshPort(), devopsHostConnectionVO.getAuthType(), devopsHostConnectionVO.getUsername(), devopsHostConnectionVO.getPassword());
            ExecResultInfoVO execResultInfoVO = sshUtil.execCommand(sshClient, command);
            redisTemplate.opsForHash().putAll(redisKey, createMap(map, execResultInfoVO.getExitCode() == 0 ? DevopsHostStatus.SUCCESS.getValue() : DevopsHostStatus.FAILED.getValue(), execResultInfoVO.getStdErr()));
        } catch (IOException exception) {
            throw new CommonException("devops.connect.host");
        } finally {
            if (checkRedisStatusOperating(redisKey)) {
                redisTemplate.opsForHash().putAll(redisKey, createMap(map, DevopsHostStatus.FAILED.getValue(), "devops.connect.host"));
            }
            IOUtils.closeQuietly(sshClient);
        }
    }
}
