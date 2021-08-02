package io.choerodon.devops.app.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Functions;
import com.google.common.base.Joiner;
import com.google.gson.Gson;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.hzero.core.base.BaseConstants;
import org.hzero.websocket.helper.KeySocketSendHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
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
import io.choerodon.devops.infra.enums.LabelType;
import io.choerodon.devops.infra.enums.host.HostCommandEnum;
import io.choerodon.devops.infra.enums.host.HostCommandStatusEnum;
import io.choerodon.devops.infra.enums.host.HostInstanceType;
import io.choerodon.devops.infra.enums.host.HostResourceType;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.*;
import io.choerodon.devops.infra.util.*;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.Select;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * @author zmf
 * @since 2020/9/15
 */
@Service
public class DevopsHostServiceImpl implements DevopsHostService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsHostServiceImpl.class);

    private static final String DIS_CONNECTION = "cat $HOME/choerodon/c7n-agent.pid |xargs kill -9";
    private static final String ERROR_HOST_NOT_FOUND = "error.host.not.found";
    private static final String ERROR_HOST_STATUS_IS_NOT_DISCONNECT = "error.host.status.is.not.disconnect";
    private static final String LOGIN_NAME = "loginName";
    private static final String REAL_NAME = "realName";
    private static final String HOST_AGENT = "curl -o host.sh %s/devops/v1/projects/%d/hosts/%d/download_file/%s && sh host.sh";
    private static final String HOST_UNINSTALL_SHELL = "ps aux|grep c7n-agent | grep -v grep |awk '{print  $2}' |xargs kill -9";
    private static final String HOST_ACTIVATE_COMMAND_TEMPLATE;

    static {
        try (InputStream inputStream = DevopsClusterServiceImpl.class.getResourceAsStream("/shell/host.sh")) {
            HOST_ACTIVATE_COMMAND_TEMPLATE = org.apache.commons.io.IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new CommonException("error.load.host.sh");
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
    private DevopsCdJobMapper devopsCdJobMapper;
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
    private DevopsNormalInstanceService devopsNormalInstanceService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private DevopsHostAppInstanceRelMapper devopsHostAppInstanceRelMapper;
    @Autowired
    private DevopsDockerInstanceMapper devopsDockerInstanceMapper;
    @Autowired
    private DevopsNormalInstanceMapper devopsNormalInstanceMapper;
    @Autowired
    private SshUtil sshUtil;
    @Autowired
    private DevopsHostCommandMapper devopsHostCommandMapper;
    @Autowired
    private DevopsHostUserPermissionService devopsHostUserPermissionService;
    @Autowired
    private UserAttrService userAttrService;
    @Autowired
    private PermissionHelper permissionHelper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public DevopsHostVO createHost(Long projectId, DevopsHostCreateRequestVO devopsHostCreateRequestVO) {
        // 补充校验参数
        devopsHostAdditionalCheckValidator.validNameProjectUnique(projectId, devopsHostCreateRequestVO.getName());
        if (devopsHostAdditionalCheckValidator.validIpAndSshPortComplete(devopsHostCreateRequestVO)) {
            devopsHostAdditionalCheckValidator.validHostInformationMatch(devopsHostCreateRequestVO);
        }
        DevopsHostDTO devopsHostDTO = ConvertUtils.convertObject(devopsHostCreateRequestVO, DevopsHostDTO.class);
        devopsHostDTO.setSkipCheckPermission(true);
        devopsHostDTO.setProjectId(projectId);

        devopsHostDTO.setHostStatus(DevopsHostStatus.DISCONNECT.getValue());
        devopsHostDTO.setToken(GenerateUUID.generateUUID().replaceAll("-", ""));
        return ConvertUtils.convertObject(MapperUtil.resultJudgedInsert(devopsHostMapper, devopsHostDTO, "error.insert.host"), DevopsHostVO.class);
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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateHost(Long projectId, Long hostId, DevopsHostUpdateRequestVO devopsHostUpdateRequestVO) {
        DevopsHostDTO devopsHostDTO = devopsHostMapper.selectByPrimaryKey(hostId);
        CommonExAssertUtil.assertNotNull(devopsHostDTO, "error.host.not.exist", hostId);
        CommonExAssertUtil.assertTrue(devopsHostDTO.getProjectId().equals(projectId), MiscConstants.ERROR_OPERATING_RESOURCE_IN_OTHER_PROJECT);
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

        MapperUtil.resultJudgedUpdateByPrimaryKey(devopsHostMapper, devopsHostDTO, "error.update.host");
    }

    @Override
    public DevopsHostVO queryHost(Long projectId, Long hostId) {
        DevopsHostDTO devopsHostDTO = devopsHostMapper.selectByPrimaryKey(hostId);
        if (devopsHostDTO == null || !projectId.equals(devopsHostDTO.getProjectId())) {
            return null;
        }

        return ConvertUtils.convertObject(devopsHostDTO, DevopsHostVO.class);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteHost(Long projectId, Long hostId) {
        DevopsHostDTO devopsHostDTO = devopsHostMapper.selectByPrimaryKey(hostId);
        checkEnableHostDelete(hostId);
        CommonExAssertUtil.assertTrue(devopsHostDTO.getProjectId().equals(projectId), MiscConstants.ERROR_OPERATING_RESOURCE_IN_OTHER_PROJECT);
        try {
            devopsHostMapper.deleteByPrimaryKey(hostId);
            devopsDockerInstanceMapper.deleteByHostId(hostId);
            devopsHostCommandMapper.deleteByHostId(hostId);
            devopsNormalInstanceMapper.deleteByHostId(hostId);
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
    public DevopsHostConnectionTestResultVO testConnection(Long projectId, DevopsHostConnectionTestVO devopsHostConnectionTestVO) {
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

    public Set<Object> multiTestConnection(Long projectId, Set<Long> hostIds) {
        List<DevopsHostDTO> devopsHostDTOList = devopsHostMapper.listByProjectIdAndIds(projectId, hostIds);
        CommonExAssertUtil.assertTrue(devopsHostDTOList.size() > 0, "error.component.host.size");
        Set<Long> connectionFailedHostIds = new HashSet<>();
        devopsHostDTOList.forEach(d -> {
            DevopsHostConnectionTestResultVO devopsHostConnectionTestResultVO = testConnection(projectId, ConvertUtils.convertObject(d, DevopsHostConnectionTestVO.class));
            if (!DevopsHostStatus.SUCCESS.getValue().equals(devopsHostConnectionTestResultVO.getHostStatus())) {
                connectionFailedHostIds.add(d.getId());
            }
        });
        return encryptService.encryptIds(connectionFailedHostIds);
    }

    @Override
    public Boolean testConnectionByIdForDeployHost(Long projectId, Long hostId) {
        DevopsHostDTO devopsHostDTO = devopsHostMapper.selectByPrimaryKey(hostId);
        if (devopsHostDTO == null) {
            return false;
        }

        CommonExAssertUtil.assertTrue(projectId.equals(devopsHostDTO.getProjectId()), MiscConstants.ERROR_OPERATING_RESOURCE_IN_OTHER_PROJECT);
        CommonExAssertUtil.assertTrue(DevopsHostType.DEPLOY.getValue().equals(devopsHostDTO.getType()), "error.host.type.invalid");

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
    public boolean HostIdInstanceIdMatch(Long hostId, Long instanceId) {
        DevopsNormalInstanceDTO devopsNormalInstanceDTO = devopsNormalInstanceMapper.selectByPrimaryKey(instanceId);
        return devopsNormalInstanceDTO != null && devopsNormalInstanceDTO.getHostId().equals(hostId);
    }

    @Override
    public boolean HostIdDockerInstanceMatch(Long hostId, Long instanceId) {
        DevopsDockerInstanceDTO devopsDockerInstanceDTO = devopsDockerInstanceMapper.selectByPrimaryKey(instanceId);
        return devopsDockerInstanceDTO != null && devopsDockerInstanceDTO.getHostId().equals(hostId);
    }

    @Override
    public Page<DevopsHostVO> pageByOptions(Long projectId, PageRequest pageRequest, boolean withCreatorInfo, @Nullable String searchParam, @Nullable String hostStatus, @Nullable Boolean doPage) {
        boolean projectOwnerOrRoot = permissionHelper.isGitlabProjectOwnerOrGitlabAdmin(projectId);
        // 解析查询参数
        Page<DevopsHostVO> page;
        Select<DevopsHostVO> select = null;
        if (projectOwnerOrRoot) {
            select = () -> devopsHostMapper.listByOptions(projectId, searchParam, hostStatus);
        } else {
            select = () -> devopsHostMapper.listMemberHostByOptions(projectId, searchParam, hostStatus, DetailsHelper.getUserDetails().getUserId());
        }
        if (doPage) {
            page = PageHelper.doPageAndSort(PageRequestUtil.simpleConvertSortForPage(pageRequest), select);
        } else {
            page = new Page<>();
            List<DevopsHostVO> devopsHostVOS = select.doSelect();
            page.setContent(devopsHostVOS);
        }

        // 分页查询
        if (withCreatorInfo) {
            // 填充创建者用户信息
            fillCreatorInfo(page);
        }
        page.getContent().forEach(h -> {
            // 如果是项目所有者或者root，展示权限管理tab和按钮
            if (projectOwnerOrRoot) {
                h.setShowPermission(true);
            } else {
                // 项目成员且为主机创建者，展示权限管理tab和按钮
                // 仅仅是项目成员，不展示权限管理tab和按钮
                h.setShowPermission(h.getCreatedBy().equals(DetailsHelper.getUserDetails().getUserId()));
            }
        });
        return page;
    }

    @Override
    public boolean checkHostDelete(Long projectId, Long hostId) {
        DevopsHostDTO devopsHostDTO = devopsHostMapper.selectByPrimaryKey(hostId);
        if (Objects.isNull(devopsHostDTO)) {
            throw new CommonException(ERROR_HOST_NOT_FOUND);
        }
        if (DevopsHostStatus.CONNECTED.getValue().equals(devopsHostDTO.getHostStatus())) {
            return Boolean.FALSE;
        }

        return Boolean.TRUE;
    }

    @Override
    public DevopsHostDTO baseQuery(Long hostId) {
        return devopsHostMapper.selectByPrimaryKey(hostId);
    }

    @Override
    @Transactional
    public void baseUpdateHostStatus(Long hostId, DevopsHostStatus status) {
        DevopsHostDTO devopsHostDTO = devopsHostMapper.selectByPrimaryKey(hostId);
        // 更新主机连接状态
        if (devopsHostDTO != null) {
            devopsHostDTO.setHostStatus(status.getValue());
        }
        devopsHostMapper.updateByPrimaryKeySelective(devopsHostDTO);
    }

    @Override
    public List<DevopsJavaInstanceVO> listJavaProcessInfo(Long projectId, Long hostId) {
        List<DevopsNormalInstanceDTO> devopsNormalInstanceDTOList = devopsNormalInstanceService.listByHostId(hostId);
        if (CollectionUtils.isEmpty(devopsNormalInstanceDTOList)) {
            return new ArrayList<>();
        }

        List<DevopsJavaInstanceVO> devopsJavaInstanceVOS = ConvertUtils.convertList(devopsNormalInstanceDTOList, DevopsJavaInstanceVO.class);

        UserDTOFillUtil.fillUserInfo(devopsJavaInstanceVOS, "createdBy", "deployer");
        return devopsJavaInstanceVOS;
    }

    @Override
    public List<DevopsDockerInstanceVO> listDockerProcessInfo(Long projectId, Long hostId) {
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
    public void deleteJavaProcess(Long projectId, Long hostId, Long instanceId) {
        devopsHostAdditionalCheckValidator.validHostIdAndInstanceIdMatch(hostId, instanceId);
        DevopsNormalInstanceDTO normalInstanceDTO = devopsNormalInstanceMapper.selectByPrimaryKey(instanceId);
        if (normalInstanceDTO.getPid() == null) {
            devopsNormalInstanceMapper.deleteByPrimaryKey(instanceId);
            return;
        }
        DevopsHostCommandDTO devopsHostCommandDTO = new DevopsHostCommandDTO();
        devopsHostCommandDTO.setCommandType(HostCommandEnum.KILL_JAR.value());
        devopsHostCommandDTO.setHostId(hostId);
        devopsHostCommandDTO.setInstanceType(HostResourceType.JAVA_PROCESS.value());
        devopsHostCommandDTO.setInstanceId(instanceId);
        devopsHostCommandDTO.setStatus(HostCommandStatusEnum.OPERATING.value());
        devopsHostCommandService.baseCreate(devopsHostCommandDTO);


        HostAgentMsgVO hostAgentMsgVO = new HostAgentMsgVO();
        hostAgentMsgVO.setHostId(String.valueOf(hostId));
        hostAgentMsgVO.setType(HostCommandEnum.KILL_JAR.value());
        hostAgentMsgVO.setCommandId(String.valueOf(devopsHostCommandDTO.getId()));


        JavaProcessInfoVO javaProcessInfoVO = new JavaProcessInfoVO();
        javaProcessInfoVO.setInstanceId(String.valueOf(instanceId));
        javaProcessInfoVO.setPid(normalInstanceDTO.getPid());
        hostAgentMsgVO.setPayload(JsonHelper.marshalByJackson(javaProcessInfoVO));

        webSocketHelper.sendByGroup(DevopsHostConstants.GROUP + hostId, DevopsHostConstants.GROUP + hostId, JsonHelper.marshalByJackson(hostAgentMsgVO));

    }

    @Override
    @Transactional
    public void deleteDockerProcess(Long projectId, Long hostId, Long instanceId) {
        devopsHostAdditionalCheckValidator.validHostIdAndDockerInstanceIdMatch(hostId, instanceId);
        DevopsDockerInstanceDTO dockerInstanceDTO = devopsDockerInstanceMapper.selectByPrimaryKey(instanceId);
        if (dockerInstanceDTO.getContainerId() == null) {
            devopsDockerInstanceMapper.deleteByPrimaryKey(instanceId);
            return;
        }

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
        hostAgentMsgVO.setPayload(JsonHelper.marshalByJackson(dockerProcessInfoVO));

        webSocketHelper.sendByGroup(DevopsHostConstants.GROUP + hostId, DevopsHostConstants.GROUP + hostId, JsonHelper.marshalByJackson(hostAgentMsgVO));

    }

    @Override
    @Transactional
    public void stopDockerProcess(Long projectId, Long hostId, Long instanceId) {
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

        DockerProcessInfoVO dockerProcessInfoVO = new DockerProcessInfoVO();
        dockerProcessInfoVO.setInstanceId(String.valueOf(instanceId));
        dockerProcessInfoVO.setContainerId(devopsDockerInstanceDTO.getContainerId());
        hostAgentMsgVO.setPayload(JsonHelper.marshalByJackson(dockerProcessInfoVO));

        webSocketHelper.sendByGroup(DevopsHostConstants.GROUP + hostId, DevopsHostConstants.GROUP + hostId, JsonHelper.marshalByJackson(hostAgentMsgVO));

    }

    @Override
    @Transactional
    public void restartDockerProcess(Long projectId, Long hostId, Long instanceId) {
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

        DockerProcessInfoVO dockerProcessInfoVO = new DockerProcessInfoVO();
        dockerProcessInfoVO.setInstanceId(String.valueOf(instanceId));
        dockerProcessInfoVO.setContainerId(devopsDockerInstanceDTO.getContainerId());

        hostAgentMsgVO.setPayload(JsonHelper.marshalByJackson(dockerProcessInfoVO));

        webSocketHelper.sendByGroup(DevopsHostConstants.GROUP + hostId, DevopsHostConstants.GROUP + hostId, JsonHelper.marshalByJackson(hostAgentMsgVO));

    }

    @Override
    @Transactional
    public void startDockerProcess(Long projectId, Long hostId, Long instanceId) {
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

        DockerProcessInfoVO dockerProcessInfoVO = new DockerProcessInfoVO();
        dockerProcessInfoVO.setInstanceId(String.valueOf(instanceId));
        dockerProcessInfoVO.setContainerId(devopsDockerInstanceDTO.getContainerId());
        hostAgentMsgVO.setPayload(JsonHelper.marshalByJackson(dockerProcessInfoVO));

        webSocketHelper.sendByGroup(DevopsHostConstants.GROUP + hostId, DevopsHostConstants.GROUP + hostId, JsonHelper.marshalByJackson(hostAgentMsgVO));

    }

    @Override
    public String downloadCreateHostFile(Long projectId, Long hostId, String token, HttpServletResponse res) {
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
        String resourceInfo = stringRedisTemplate.opsForValue().get(String.format(DevopsHostConstants.HOST_RESOURCE_INFO_KEY, hostId));
        if (StringUtils.isEmpty(resourceInfo)) {
            return new ResourceUsageInfoVO();
        }
        return JsonHelper.unmarshalByJackson(resourceInfo, ResourceUsageInfoVO.class);
    }

    @Override
    public String queryShell(Long projectId, Long hostId) {
        DevopsHostDTO devopsHostDTO = devopsHostMapper.selectByPrimaryKey(hostId);
        return String.format(HOST_AGENT, apiHost, projectId, hostId, devopsHostDTO.getToken());
    }

    @Override
    public String queryUninstallShell(Long projectId, Long hostId) {
        return HOST_UNINSTALL_SHELL;
    }

    @Override
    public void connectHost(Long projectId, Long hostId, DevopsHostConnectionVO devopsHostConnectionVO) {
        String redisKey = generateRedisKey(projectId, hostId);
        if (checkRedisStatusOperating(redisKey)) {
            return;
        }
        Map<String, String> map = new HashMap<>();
        String command = queryShell(projectId, hostId);
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
    public Page<?> queryInstanceList(Long projectId, Long hostId, Long appServiceId, PageRequest pageRequest, String name, String type, String status, String params) {
        return queryHostInstances(projectId, hostId, appServiceId, pageRequest, name, type, status, params);

    }

    @Override
    public Page<DevopsHostInstanceVO> queryInstanceListByHostId(Long projectId, Long hostId, PageRequest pageRequest, String name, String type, String status, String params) {
        Page<DevopsHostInstanceVO> devopsHostInstanceVOPage = PageHelper.doPageAndSort(pageRequest, () -> devopsHostAppInstanceRelMapper.queryInstanceListByHostId(hostId, name, type, status, params));
        if (CollectionUtils.isEmpty(devopsHostInstanceVOPage.getContent())) {
            return new Page<>();
        }
        List<DevopsHostInstanceVO> devopsHostInstanceVOS = devopsHostInstanceVOPage.getContent();
        List<DevopsHostInstanceVO> hostInstances = new ArrayList<>();

        List<DevopsHostInstanceVO> devopsDockerInstances = devopsHostInstanceVOS.stream().filter(hostAppInstanceRelDTO -> StringUtils.equalsIgnoreCase(hostAppInstanceRelDTO.getInstanceType(), HostInstanceType.DOCKER_PROCESS.value())).collect(Collectors.toList());
        handleDockerProcess(devopsDockerInstances, hostInstances);

        List<DevopsHostInstanceVO> devopsNormalInstances = devopsHostInstanceVOS.stream().filter(hostAppInstanceRelDTO -> !StringUtils.equalsIgnoreCase(hostAppInstanceRelDTO.getInstanceType(), HostInstanceType.DOCKER_PROCESS.value())).collect(Collectors.toList());
        handleNormalProcess(devopsNormalInstances, hostInstances);

        UserDTOFillUtil.fillUserInfo(hostInstances, "createdBy", "deployer");

        //按照部署时间排序
        List<DevopsHostInstanceVO> hostInstanceVOS = hostInstances.stream().sorted(Comparator.comparing(DevopsHostInstanceVO::getCreationDate).reversed()).collect(Collectors.toList());
        devopsHostInstanceVOPage.setContent(hostInstanceVOS);
        return devopsHostInstanceVOPage;
    }

    @Override
    public String disconnectionHost() {
        return DIS_CONNECTION;
    }

    @Override
    public Page<DevopsUserPermissionVO> pageUserPermissionByHostId(Long projectId, PageRequest pageable, String params, Long envId) {
        DevopsHostDTO devopsHostDTO = devopsHostMapper.selectByPrimaryKey(envId);
        // 校验用户为项目所有者或者为主机创建者
        CommonExAssertUtil.assertTrue(permissionHelper.isGitlabProjectOwnerOrGitlabAdmin(projectId) || devopsHostDTO.getCreatedBy().equals(DetailsHelper.getUserDetails().getUserId()), "error.host.permission.denied");

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
        if (!devopsHostDTO.getSkipCheckPermission()) {
            // 根据搜索参数查询数据库中所有的环境权限分配数据
            List<DevopsHostUserPermissionDTO> devopsHostUserPermissionDTOList = devopsHostUserPermissionService.listUserHostPermissionByOption(envId, searchParamMap, paramList);
            List<Long> permissions = devopsHostUserPermissionDTOList.stream().map(DevopsHostUserPermissionDTO::getIamUserId).collect(Collectors.toList());
            projectMembers = projectMembers
                    .stream()
                    .filter(member -> permissions.contains(member.getIamUserId()) || baseServiceClientOperator.isGitlabProjectOwner(member.getIamUserId(), projectId) || member.getIamUserId().equals(devopsHostDTO.getCreatedBy()))
                    .collect(Collectors.toList());
            projectMembers.forEach(devopsUserPermissionVO -> {
                if (permissions.contains(devopsUserPermissionVO.getIamUserId())) {
                    devopsHostUserPermissionDTOList.forEach(devopsEnvUserPermissionDTO -> {
                        if (devopsEnvUserPermissionDTO.getIamUserId().equals(devopsUserPermissionVO.getIamUserId())) {
                            devopsUserPermissionVO.setCreationDate(devopsEnvUserPermissionDTO.getCreationDate());
                        }
                    });
                }
            });
        }
        return DevopsUserPermissionVO.combineOwnerAndMember(projectMembers, projectOwners, pageable, devopsHostDTO.getCreatedBy());
    }

    @Override
    public void deletePermissionOfUser(Long projectId, Long hostId, Long userId) {
        if (hostId == null || userId == null) {
            return;
        }

        DevopsHostDTO devopsHostDTO = devopsHostMapper.selectByPrimaryKey(hostId);
        // 校验用户为项目所有者或者为主机创建者
        CommonExAssertUtil.assertTrue(permissionHelper.isGitlabProjectOwnerOrGitlabAdmin(projectId) || devopsHostDTO.getCreatedBy().equals(DetailsHelper.getUserDetails().getUserId()), "error.host.permission.denied");

        if (devopsHostDTO == null) {
            return;
        }

        if (userId.equals(devopsHostDTO.getCreatedBy())) {
            throw new CommonException("error.delete.permission.of.creator");
        }

        CommonExAssertUtil.assertTrue(projectId.equals(devopsHostDTO.getProjectId()), MiscConstants.ERROR_OPERATING_RESOURCE_IN_OTHER_PROJECT);

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(userId);

        if (userAttrDTO == null) {
            return;
        }

        if (baseServiceClientOperator.isGitlabProjectOwner(userAttrDTO.getIamUserId(), projectId)) {
            throw new CommonException("error.delete.permission.of.project.owner");
        }

        // 删除数据库中的纪录
        DevopsHostUserPermissionDTO devopsHostUserPermissionDTO = new DevopsHostUserPermissionDTO();
        devopsHostUserPermissionDTO.setHostId(hostId);
        devopsHostUserPermissionDTO.setIamUserId(userId);
        devopsHostUserPermissionService.baseDelete(devopsHostUserPermissionDTO);
    }

    @Override
    public List<DevopsUserVO> listAllUserPermission(Long hostId) {
        DevopsHostDTO devopsHostDTO = devopsHostMapper.selectByPrimaryKey(hostId);
        // 校验用户为项目所有者或者为主机创建者
        CommonExAssertUtil.assertTrue(permissionHelper.isGitlabProjectOwnerOrGitlabAdmin(devopsHostDTO.getProjectId()) || devopsHostDTO.getCreatedBy().equals(DetailsHelper.getUserDetails().getUserId()), "error.host.permission.denied");

        return ConvertUtils.convertList(devopsHostUserPermissionService.baseListByHostId(hostId), DevopsUserVO.class);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateHostUserPermission(Long projectId, DevopsHostPermissionUpdateVO devopsHostPermissionUpdateVO) {
        DevopsHostDTO preHostDTO = devopsHostMapper.selectByPrimaryKey(devopsHostPermissionUpdateVO.getHostId());

        // 校验主机属于该项目
        CommonExAssertUtil.assertTrue(projectId.equals(preHostDTO.getProjectId()), MiscConstants.ERROR_OPERATING_RESOURCE_IN_OTHER_PROJECT);
        // 校验用户为项目所有者或者为主机创建者
        CommonExAssertUtil.assertTrue(permissionHelper.isGitlabProjectOwnerOrGitlabAdmin(projectId) || preHostDTO.getCreatedBy().equals(DetailsHelper.getUserDetails().getUserId()), "error.host.permission.denied");


        List<Long> addIamUserIds = devopsHostPermissionUpdateVO.getUserIds();
        // 判断更新的情况
        if (preHostDTO.getSkipCheckPermission()) {
            if (devopsHostPermissionUpdateVO.getSkipCheckPermission()) {
                return;
            } else {
                // 待添加的用户列表为空
                if (CollectionUtils.isEmpty(addIamUserIds)) {
                    return;
                }
                // 添加权限
                List<IamUserDTO> addIamUsers = baseServiceClientOperator.listUsersByIds(addIamUserIds);
                List<DevopsHostUserPermissionDTO> permissionDTOListToInsert = new ArrayList<>();
                addIamUsers.forEach(e ->
                        permissionDTOListToInsert.add(new DevopsHostUserPermissionDTO(e.getLoginName(), e.getId(), e.getRealName(), preHostDTO.getId()))
                );

                devopsHostUserPermissionService.batchInsert(permissionDTOListToInsert);

                // 更新字段
                preHostDTO.setSkipCheckPermission(devopsHostPermissionUpdateVO.getSkipCheckPermission());
                preHostDTO.setObjectVersionNumber(devopsHostPermissionUpdateVO.getObjectVersionNumber());
                devopsHostMapper.updateByPrimaryKeySelective(preHostDTO);
            }
        } else {
            if (devopsHostPermissionUpdateVO.getSkipCheckPermission()) {
                // 删除原先所有的分配情况
                devopsHostUserPermissionService.deleteByHostId(preHostDTO.getId());

                // 更新字段
                preHostDTO.setSkipCheckPermission(devopsHostPermissionUpdateVO.getSkipCheckPermission());
                preHostDTO.setObjectVersionNumber(devopsHostPermissionUpdateVO.getObjectVersionNumber());
                devopsHostMapper.updateByPrimaryKeySelective(preHostDTO);
            } else {
                // 待添加的用户列表为空
                if (CollectionUtils.isEmpty(addIamUserIds)) {
                    return;
                }
                devopsHostUserPermissionService.baseUpdate(devopsHostPermissionUpdateVO.getHostId(), addIamUserIds);
            }
        }
    }

    @Override
    public Page<DevopsUserVO> listNonRelatedMembers(Long projectId, Long hostId, Long selectedIamUserId, PageRequest pageable, String params) {
        DevopsHostDTO devopsHostDTO = devopsHostMapper.selectByPrimaryKey(hostId);
        // 校验用户为项目所有者或者为主机创建者
        CommonExAssertUtil.assertTrue(permissionHelper.isGitlabProjectOwnerOrGitlabAdmin(projectId) || devopsHostDTO.getCreatedBy().equals(DetailsHelper.getUserDetails().getUserId()), "error.host.permission.denied");

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
            Page<DevopsUserVO> pageInfo = new Page<>();
            pageInfo.setContent(new ArrayList<>());
            return pageInfo;
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

        Page<IamUserDTO> pageInfo = PageInfoUtil.createPageFromList(members, pageable);

        return ConvertUtils.convertPage(pageInfo, member -> new DevopsUserVO(member.getId(), member.getLdap() ? member.getLoginName() : member.getEmail(), member.getRealName(), member.getImageUrl()));
    }

    private void handleNormalProcess(List<DevopsHostInstanceVO> devopsNormalInstances, List<DevopsHostInstanceVO> hostInstances) {
        if (!CollectionUtils.isEmpty(devopsNormalInstances)) {
            List<Long> normalInstanceIds = devopsNormalInstances.stream().map(DevopsHostInstanceVO::getId).collect(Collectors.toList());
            List<DevopsNormalInstanceDTO> devopsNormalInstanceDTOS = devopsNormalInstanceMapper.selectByIds(Joiner.on(BaseConstants.Symbol.COMMA).join(normalInstanceIds));
            List<DevopsNormalInstanceVO> devopsNormalInstanceVOS = ConvertUtils.convertList(devopsNormalInstanceDTOS, DevopsNormalInstanceVO.class);
            devopsNormalInstanceVOS.forEach(devopsNormalInstanceVO -> {
                devopsNormalInstanceVO.setInstanceType(HostInstanceType.NORMAL_PROCESS.value());
                //加上操作状态
                devopsNormalInstanceVO.setDevopsHostCommandDTO(devopsHostCommandMapper.selectLatestByInstanceId(devopsNormalInstanceVO.getId()));
            });
            hostInstances.addAll(devopsNormalInstanceVOS);
        }
    }

    private Page<?> queryHostInstances(Long projectId, Long hostId, Long appServiceId, PageRequest pageRequest, String name, String type, String status, String params) {
        Page<DevopsHostAppInstanceRelDTO> hostAppInstanceRelDTOPage = PageHelper.doPageAndSort(pageRequest, () -> devopsHostAppInstanceRelMapper.queryInstanceListByHostIdAndAppId(projectId, hostId, appServiceId, name, type, status, params));
        if (CollectionUtils.isEmpty(hostAppInstanceRelDTOPage.getContent())) {
            return new Page<>();
        }
        List<Object> hostInstances = new ArrayList<>();
        handHostProcess(hostAppInstanceRelDTOPage, hostInstances);
        UserDTOFillUtil.fillUserInfo(hostInstances, "createdBy", "deployer");
        Page<Object> resultPage = new Page<>();
        BeanUtils.copyProperties(hostAppInstanceRelDTOPage, resultPage);
        resultPage.setContent(hostInstances);
        return resultPage;
    }


    private void handHostProcess(Page<DevopsHostAppInstanceRelDTO> hostAppInstanceRelDTOPage, List<Object> hostInstances) {
        //筛选出docker进程
        handDockerProcess(hostAppInstanceRelDTOPage, hostInstances);
        //筛选出非docker进程
        handNormalProcess(hostAppInstanceRelDTOPage, hostInstances);
    }

    private void handNormalProcess(Page<DevopsHostAppInstanceRelDTO> hostAppInstanceRelDTOPage, List<Object> hostInstances) {
        List<DevopsHostAppInstanceRelDTO> normalHostInstances = hostAppInstanceRelDTOPage.getContent().stream().filter(hostAppInstanceRelDTO -> !StringUtils.equalsIgnoreCase(hostAppInstanceRelDTO.getInstanceType(), HostInstanceType.DOCKER_PROCESS.value())).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(normalHostInstances)) {
            List<Long> normalInstanceIds = normalHostInstances.stream().map(DevopsHostAppInstanceRelDTO::getInstanceId).collect(Collectors.toList());
            List<DevopsNormalInstanceDTO> devopsNormalInstanceDTOS = devopsNormalInstanceMapper.selectByIds(Joiner.on(BaseConstants.Symbol.COMMA).join(normalInstanceIds));
            List<DevopsNormalInstanceVO> devopsNormalInstanceVOS = ConvertUtils.convertList(devopsNormalInstanceDTOS, DevopsNormalInstanceVO.class);
            devopsNormalInstanceVOS.forEach(devopsNormalInstanceVO -> {
                devopsNormalInstanceVO.setInstanceType(HostInstanceType.NORMAL_PROCESS.value());
                //加上操作状态
                devopsNormalInstanceVO.setDevopsHostCommandDTO(devopsHostCommandMapper.selectLatestByInstanceId(devopsNormalInstanceVO.getId()));
            });
            hostInstances.addAll(devopsNormalInstanceVOS);
        }
    }

    private void handDockerProcess(Page<DevopsHostAppInstanceRelDTO> hostAppInstanceRelDTOPage, List<Object> hostInstances) {
        List<DevopsHostAppInstanceRelDTO> dockerHostInstances = hostAppInstanceRelDTOPage.getContent().stream().filter(hostAppInstanceRelDTO -> StringUtils.equalsIgnoreCase(hostAppInstanceRelDTO.getInstanceType(), HostInstanceType.DOCKER_PROCESS.value())).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(dockerHostInstances)) {
            List<Long> dockerInstanceIds = dockerHostInstances.stream().map(DevopsHostAppInstanceRelDTO::getInstanceId).collect(Collectors.toList());
            List<DevopsDockerInstanceDTO> devopsDockerInstanceDTOS = devopsDockerInstanceMapper.selectByIds(Joiner.on(BaseConstants.Symbol.COMMA).join(dockerInstanceIds));
            List<DevopsDockerInstanceVO> devopsDockerInstanceVOS = ConvertUtils.convertList(devopsDockerInstanceDTOS, DevopsDockerInstanceVO.class);
            devopsDockerInstanceVOS.forEach(devopsDockerInstanceVO -> {
                devopsDockerInstanceVO.setInstanceType(HostInstanceType.DOCKER_PROCESS.value());
                //加上操作状态
                devopsDockerInstanceVO.setDevopsHostCommandDTO(devopsHostCommandMapper.selectLatestByInstanceId(devopsDockerInstanceVO.getId()));
            });
            hostInstances.addAll(devopsDockerInstanceVOS);
        }
    }

    private void handleDockerProcess(List<DevopsHostInstanceVO> devopsHostInstanceVOS, List<DevopsHostInstanceVO> hostInstances) {
        if (!CollectionUtils.isEmpty(devopsHostInstanceVOS)) {
            List<Long> dockerInstanceIds = devopsHostInstanceVOS.stream().map(DevopsHostInstanceVO::getId).collect(Collectors.toList());
            List<DevopsDockerInstanceDTO> devopsDockerInstanceDTOS = devopsDockerInstanceMapper.selectByIds(Joiner.on(BaseConstants.Symbol.COMMA).join(dockerInstanceIds));
            List<DevopsDockerInstanceVO> devopsDockerInstanceVOS = ConvertUtils.convertList(devopsDockerInstanceDTOS, DevopsDockerInstanceVO.class);
            devopsDockerInstanceVOS.forEach(devopsDockerInstanceVO -> {
                devopsDockerInstanceVO.setInstanceType(HostInstanceType.DOCKER_PROCESS.value());
                //加上操作状态
                devopsDockerInstanceVO.setDevopsHostCommandDTO(devopsHostCommandMapper.selectLatestByInstanceId(devopsDockerInstanceVO.getId()));
            });
            hostInstances.addAll(devopsDockerInstanceVOS);
        }
    }

    private void fillCreatorInfo(Page<DevopsHostVO> devopsHostVOS) {
        List<Long> userIds = devopsHostVOS.getContent().stream().map(DevopsHostVO::getCreatedBy).collect(Collectors.toList());
        Map<Long, IamUserDTO> userInfo = baseServiceClientOperator.listUsersByIds(userIds).stream().collect(Collectors.toMap(IamUserDTO::getId, Functions.identity()));
        devopsHostVOS.getContent().forEach(host -> host.setCreatorInfo(userInfo.get(host.getCreatedBy())));
    }

    private String generateRedisKey(Long projectId, Long hostId) {
        return "host:connect:" + projectId + ":" + hostId;
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
            throw new CommonException("error.connect.host");
        } finally {
            if (checkRedisStatusOperating(redisKey)) {
                redisTemplate.opsForHash().putAll(redisKey, createMap(map, DevopsHostStatus.FAILED.getValue(), "error.connect.host"));
            }
            IOUtils.closeQuietly(sshClient);
        }
    }

    private boolean checkRedisStatusOperating(String redisKey) {
        return redisTemplate.opsForHash().hasKey(redisKey, "status") && Objects.equals(redisTemplate.opsForHash().get(redisKey, "status"), DevopsHostStatus.OPERATING.getValue());
    }

    private Map<String, String> createMap(Map<String, String> map, String status, String exception) {
        map.put("status", status);
        map.put("exception", exception);
        return map;
    }
}
