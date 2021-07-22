package io.choerodon.devops.app.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Functions;
import com.google.common.base.Joiner;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.hzero.core.base.BaseConstants;
import org.hzero.core.util.UUIDUtils;
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

import io.choerodon.core.convertor.ApplicationContextHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.core.utils.ConvertUtils;
import io.choerodon.devops.api.validator.DevopsHostAdditionalCheckValidator;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.host.*;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.DevopsHostConstants;
import io.choerodon.devops.infra.constant.GitOpsConstants;
import io.choerodon.devops.infra.constant.MiscConstants;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.enums.DevopsHostStatus;
import io.choerodon.devops.infra.enums.DevopsHostType;
import io.choerodon.devops.infra.enums.host.HostCommandEnum;
import io.choerodon.devops.infra.enums.host.HostCommandStatusEnum;
import io.choerodon.devops.infra.enums.host.HostInstanceType;
import io.choerodon.devops.infra.enums.host.HostResourceType;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.*;
import io.choerodon.devops.infra.util.*;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * @author zmf
 * @since 2020/9/15
 */
@Service
public class DevopsHostServiceImpl implements DevopsHostService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsHostServiceImpl.class);

    private static final String ERROR_HOST_NOT_FOUND = "error.host.not.found";
    private static final String ERROR_HOST_STATUS_IS_NOT_DISCONNECT = "error.host.status.is.not.disconnect";
    /**
     * 主机状态处于处理中的超时时长
     */
    private static final long OPERATING_TIMEOUT = 300L * 1000;
    private static final String CHECKING_HOST = "checking";
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
    public Set<Long> batchSetStatusOperating(Long projectId, Set<Long> hostIds) {
        LOGGER.debug("batchSetStatusOperating: projectId: {}, hostIds: {}", projectId, hostIds);
        if (CollectionUtils.isEmpty(hostIds)) {
            return Collections.emptySet();
        }

        // 过滤出需要进行校准的主机
        List<DevopsHostDTO> hosts = filterHostsToCorrect(devopsHostMapper.listByProjectIdAndIds(projectId, hostIds));
        if (CollectionUtils.isEmpty(hosts)) {
            return Collections.emptySet();
        }

        // 设置状态为处理中
        Date current = new Date();
        Long updateUserId = DetailsHelper.getUserDetails().getUserId();
        if (!CollectionUtils.isEmpty(hosts)) {
            devopsHostMapper.batchSetStatusOperating(projectId, hosts.stream().map(DevopsHostDTO::getId).collect(Collectors.toSet()), current, updateUserId);
        }

        return hosts.stream().map(DevopsHostDTO::getId).collect(Collectors.toSet());
    }

    @Async(GitOpsConstants.HOST_STATUS_EXECUTOR)
    @Override
    public void asyncBatchCorrectStatus(Long projectId, Set<Long> hostIds, Long userId) {
        LOGGER.debug("asyncBatchCorrectStatus: projectId: {}, hostIds: {}", projectId, hostIds);
        // 这么调用, 是解决事务代理不生效问题
        hostIds.forEach(hostId -> ApplicationContextHelper.getContext().getBean(DevopsHostService.class).correctStatus(projectId, hostId, userId));
    }

    @Override
    public String asyncBatchCorrectStatusWithProgress(Long projectId, Set<Long> hostIds) {
        String correctKey = UUIDUtils.generateUUID();
        // 初始化校验状态
        Map<Long, String> map = new HashMap<>();
        hostIds.forEach(hostId -> map.put(hostId, CHECKING_HOST));

        stringRedisTemplate.opsForValue().set(correctKey, gson.toJson(map), 10, TimeUnit.MINUTES);
        hostIds.forEach(hostId -> ApplicationContextHelper.getContext().getBean(DevopsHostService.class).correctStatus(projectId, correctKey, hostId));
        return correctKey;
    }

    @Transactional(rollbackFor = Exception.class)
    @Async(GitOpsConstants.HOST_STATUS_EXECUTOR)
    @Override
    public void asyncBatchSetTimeoutHostFailed(Long projectId, Set<Long> hostIds) {
        LOGGER.debug("batchSetStatusTimeoutFailed: projectId: {}, hostIds: {}", projectId, hostIds);
        if (CollectionUtils.isEmpty(hostIds)) {
            return;
        }

        List<DevopsHostDTO> hosts = devopsHostMapper.listByProjectIdAndIds(projectId, hostIds);
        if (CollectionUtils.isEmpty(hosts)) {
            return;
        }

        // 分类测试主机和部署的主机
        List<DevopsHostDTO> deployHosts = new ArrayList<>();
        List<DevopsHostDTO> testHosts = new ArrayList<>();
        hosts.forEach(host -> {
            if (DevopsHostType.DEPLOY.getValue().equalsIgnoreCase(host.getType())) {
                deployHosts.add(host);
            } else {
                testHosts.add(host);
            }
        });

        // 设置状态为失败
        Date current = new Date();
        if (!CollectionUtils.isEmpty(deployHosts)) {
            devopsHostMapper.batchSetStatusTimeoutFailed(projectId, deployHosts.stream().map(DevopsHostDTO::getId).collect(Collectors.toSet()), false, current);
        }
        if (!CollectionUtils.isEmpty(testHosts)) {
            devopsHostMapper.batchSetStatusTimeoutFailed(projectId, testHosts.stream().map(DevopsHostDTO::getId).collect(Collectors.toSet()), true, current);
        }
    }

    @Async(GitOpsConstants.HOST_STATUS_EXECUTOR)
    @Transactional
    @Override
    public void correctStatus(Long projectId, Long hostId, Long updaterId) {
        boolean noContextPre = DetailsHelper.getUserDetails() == null;

        try {
            DevopsHostDTO hostDTO = devopsHostMapper.selectByPrimaryKey(hostId);
            if (hostDTO == null) {
                return;
            }

            // 设置上下文, 以免丢失更新者信息
            CustomContextUtil.setDefaultIfNull(updaterId != null ? updaterId : hostDTO.getLastUpdatedBy());
            DevopsHostConnectionTestVO devopsHostConnectionTestVO = ConvertUtils.convertObject(hostDTO, DevopsHostConnectionTestVO.class);

            DevopsHostConnectionTestResultVO result = testConnection(projectId, devopsHostConnectionTestVO);
            hostDTO.setHostStatus(result.getHostStatus());
            hostDTO.setHostCheckError(result.getHostCheckError());
            // 不对更新涉及的纪录结果进行判断
            devopsHostMapper.updateByPrimaryKeySelective(hostDTO);
            LOGGER.debug("connection result for host with id {} is {}", hostId, result);
        } catch (Exception ex) {
            LOGGER.warn("Failed to correct status for host with id {}", hostId);
            LOGGER.warn("The ex is ", ex);
        } finally {
            // 如果之前没有上下文, 清除上下文
            if (noContextPre) {
                CustomContextUtil.clearContext();
            }
        }
    }

    @Override
    @Async
    public void correctStatus(Long projectId, String correctKey, Long hostId) {
        boolean noContextPre = DetailsHelper.getUserDetails() == null;
        try {
            DevopsHostDTO hostDTO = devopsHostMapper.selectByPrimaryKey(hostId);
            if (hostDTO == null) {
                return;
            }

            // 设置上下文, 以免丢失更新者信息
            CustomContextUtil.setDefaultIfNull(hostDTO.getLastUpdatedBy());
            DevopsHostConnectionTestVO devopsHostConnectionTestVO = ConvertUtils.convertObject(hostDTO, DevopsHostConnectionTestVO.class);

            DevopsHostConnectionTestResultVO result = testConnection(projectId, devopsHostConnectionTestVO);
            hostDTO.setHostStatus(result.getHostStatus());
            hostDTO.setHostCheckError(result.getHostCheckError());
            // 不对更新涉及的纪录结果进行判断
            devopsHostMapper.updateByPrimaryKeySelective(hostDTO);
            String status = DevopsHostStatus.FAILED.getValue();
            if (DevopsHostStatus.SUCCESS.getValue().equals(result.getHostStatus()) && DevopsHostStatus.SUCCESS.getValue().equals(result.getJmeterStatus())) {
                status = DevopsHostStatus.SUCCESS.getValue();
            }
            updateHostStatus(correctKey, hostId, status);
            LOGGER.debug("connection result for host with id {} is {}", hostId, result);
        } catch (Exception ex) {
            LOGGER.warn("Failed to correct status for host with id {}", hostId);
            LOGGER.warn("The ex is ", ex);
        } finally {
            // 如果之前没有上下文, 清除上下文
            if (noContextPre) {
                CustomContextUtil.clearContext();
            }
        }
    }

    private void updateHostStatus(String correctKey, Long hostId, String status) {
        String lockKey = "checkHost:status:lock:" + correctKey;
        while (!Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(lockKey, "lock", 10, TimeUnit.MINUTES))) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                LOGGER.info("sleep.failed.", e);
            }
        }
        try {
            Map<Long, String> hostStatus = gson.fromJson(stringRedisTemplate.opsForValue().get(correctKey), new TypeToken<Map<Long, String>>() {
            }.getType());
            if (hostStatus == null) {
                hostStatus = new HashMap<>();
            }
            hostStatus.put(hostId, status);
            stringRedisTemplate.opsForValue().set(correctKey, gson.toJson(hostStatus), 10, TimeUnit.MINUTES);
        } finally {
            stringRedisTemplate.delete(lockKey);
        }

    }

    /**
     * 过滤出需要进行校准的主机
     *
     * @param hosts 主机
     * @return 需要进行校准的主机
     */
    private List<DevopsHostDTO> filterHostsToCorrect(List<DevopsHostDTO> hosts) {
        return hosts.stream().filter(hostDTO -> {
            // 过滤测试中的主机
            if (isOperating(hostDTO.getType(), hostDTO.getHostStatus())
                    && !isTimeout(hostDTO.getLastUpdateDate())) {
                LOGGER.info("Skip correct for operating host with id {}", hostDTO.getId());
                return false;
            }
            return true;
        }).collect(Collectors.toList());
    }

    /**
     * 主机状态是否是处理中
     *
     * @param type       主机类型
     * @param hostStatus 主机状态
     * @return true表示是处理中
     */
    private boolean isOperating(String type, String hostStatus) {
        if (DevopsHostType.DEPLOY.getValue().equalsIgnoreCase(type)) {
            return DevopsHostStatus.OPERATING.getValue().equals(hostStatus);
        }
        return false;
    }

    /**
     * 判断主机处于处理中的时间是否超时了
     *
     * @param lastUpdateDate 上次更新时间
     * @return true表示超时
     */
    private boolean isTimeout(Date lastUpdateDate) {
        return System.currentTimeMillis() - lastUpdateDate.getTime() >= OPERATING_TIMEOUT;
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
    public Page<DevopsHostVO> pageByOptions(Long projectId, PageRequest pageRequest, boolean withUpdaterInfo, @Nullable String searchParam, @Nullable String hostStatus, @Nullable Boolean doPage) {
        // 解析查询参数
        Page<DevopsHostVO> page;
        if (doPage) {
            page = PageHelper.doPageAndSort(PageRequestUtil.simpleConvertSortForPage(pageRequest), () -> devopsHostMapper.listByOptions(projectId, searchParam, hostStatus));
        } else {
            page = new Page<>();
            List<DevopsHostVO> devopsHostVOS = devopsHostMapper.listByOptions(projectId, searchParam, hostStatus);
            page.setContent(devopsHostVOS);
        }

        // 分页查询
        if (withUpdaterInfo) {
            // 填充更新者用户信息
            fillUpdaterInfo(page);
        }
        return page;
    }

    /**
     * 校验主机的状态是否超时
     *
     * @param projectId      项目id
     * @param devopsHostDTOS 主机
     */
    private void checkTimeout(Long projectId, List<DevopsHostDTO> devopsHostDTOS) {
        Set<Long> ids = new HashSet<>();
        devopsHostDTOS.forEach(host -> {
            // 收集校验超时的主机
            if (isOperating(host.getType(), host.getHostStatus())
                    && isTimeout(host.getLastUpdateDate())) {
                ids.add(host.getId());
            }
        });

        // 不为空 异步设置失败
        if (!ids.isEmpty()) {
            ApplicationContextHelper.getContext().getBean(DevopsHostService.class).asyncBatchSetTimeoutHostFailed(projectId, ids);
        }
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
    public CheckingProgressVO getCheckingProgress(Long projectId, String correctKey) {
        Map<Long, String> hostStatusMap = gson.fromJson(stringRedisTemplate.opsForValue().get(correctKey), new TypeToken<Map<Long, String>>() {
        }.getType());
        if (hostStatusMap == null) {
            return null;
        }
        int size = hostStatusMap.size();
        if (size == 0) {
            return null;
        }
        int failed = 0;
        int success = 0;
        int checking = 0;
        Set<Long> ids = hostStatusMap.keySet();
        for (Long id : ids) {
            if (CHECKING_HOST.equals(hostStatusMap.get(id))) {
                checking++;
            }
            if (DevopsHostStatus.FAILED.getValue().equals(hostStatusMap.get(id))) {
                failed++;
            }
            if (DevopsHostStatus.SUCCESS.getValue().equals(hostStatusMap.get(id))) {
                success++;
            }
        }
        CheckingProgressVO checkingProgressVO = new CheckingProgressVO();
        if (checking > 0) {
            checkingProgressVO.setStatus(CHECKING_HOST);
        }
        if (failed > 0) {
            checkingProgressVO.setStatus(DevopsHostStatus.FAILED.getValue());
        }
        if (success == size) {
            checkingProgressVO.setStatus(DevopsHostStatus.SUCCESS.getValue());
        }
        double progress = (double) success / (double) size;
        checkingProgressVO.setProgress(progress * 100);

        return checkingProgressVO;
    }

    @Override
    public Page<DevopsHostVO> pagingWithCheckingStatus(Long projectId, PageRequest pageRequest, String correctKey, String searchParam) {
        Set<Long> hostIds = new HashSet<>();
        if (!StringUtils.isAllEmpty(correctKey)) {
            Map<Long, String> hostStatusMap = gson.fromJson(stringRedisTemplate.opsForValue().get(correctKey), new TypeToken<Map<Long, String>>() {
            }.getType());
            if (!CollectionUtils.isEmpty(hostStatusMap)) {
                hostIds = hostStatusMap.keySet();
            }
        }
        Page<DevopsHostVO> page;
        if (CollectionUtils.isEmpty(hostIds)) {
            page = PageHelper.doPageAndSort(pageRequest, () -> devopsHostMapper.listBySearchParam(projectId, searchParam));
        } else {
            Set<Long> finalHostIds = hostIds;
            page = PageHelper.doPage(pageRequest, () -> devopsHostMapper.pagingWithCheckingStatus(projectId, finalHostIds, searchParam));
        }
        // 添加用户信息
        if (!page.isEmpty()) {
            List<Long> userIds = page.getContent().stream().map(DevopsHostVO::getLastUpdatedBy).collect(Collectors.toList());
            List<IamUserDTO> iamUserDTOS = baseServiceClientOperator.queryUsersByUserIds(userIds);
            Map<Long, IamUserDTO> userDTOMap = iamUserDTOS.stream().collect(Collectors.toMap(IamUserDTO::getId, v -> v));
            page.getContent().forEach(devopsHostVO -> {
                if (userDTOMap.get(devopsHostVO.getLastUpdatedBy()) != null) {
                    devopsHostVO.setUpdaterInfo(userDTOMap.get(devopsHostVO.getLastUpdatedBy()));
                }
            });
        }
        return page;
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

        DevopsNormalInstanceDTO normalInstanceDTO = devopsNormalInstanceMapper.selectByPrimaryKey(instanceId);

        JavaProcessInfoVO javaProcessInfoVO = new JavaProcessInfoVO();
        javaProcessInfoVO.setInstanceId(String.valueOf(instanceId));
        javaProcessInfoVO.setPid(normalInstanceDTO.getPid());
        hostAgentMsgVO.setPayload(JsonHelper.marshalByJackson(javaProcessInfoVO));

        webSocketHelper.sendByGroup(DevopsHostConstants.GROUP + hostId, DevopsHostConstants.GROUP + hostId, JsonHelper.marshalByJackson(hostAgentMsgVO));

    }

    @Override
    @Transactional
    public void deleteDockerProcess(Long projectId, Long hostId, Long instanceId) {
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

        DevopsDockerInstanceDTO dockerInstanceDTO = devopsDockerInstanceMapper.selectByPrimaryKey(instanceId);

        DockerProcessInfoVO dockerProcessInfoVO = new DockerProcessInfoVO();
        dockerProcessInfoVO.setInstanceId(String.valueOf(instanceId));
        dockerProcessInfoVO.setContainerId(dockerInstanceDTO.getContainerId());
        hostAgentMsgVO.setPayload(JsonHelper.marshalByJackson(dockerProcessInfoVO));

        webSocketHelper.sendByGroup(DevopsHostConstants.GROUP + hostId, DevopsHostConstants.GROUP + hostId, JsonHelper.marshalByJackson(hostAgentMsgVO));

    }

    @Override
    @Transactional
    public void stopDockerProcess(Long projectId, Long hostId, Long instanceId) {
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
        String commend = queryShell(projectId, hostId);
        redisTemplate.opsForHash().putAll(redisKey, createMap(map, DevopsHostStatus.OPERATING.getValue(), null));
        automaticHost(devopsHostConnectionVO, map, redisKey, commend);
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
    public Page<?> queryInstanceList(Long projectId, Long hostId, Long appServiceId, PageRequest pageRequest, String searchType, String searchParam) {
        return queryHostInstances(projectId, hostId, appServiceId, pageRequest, searchType, searchParam);

    }

    @Override
    public Page<DevopsHostInstanceVO> queryInstanceListByHostId(Long projectId, Long hostId, PageRequest pageRequest, String searchType, String searchParam) {
        Page<DevopsHostInstanceVO> devopsHostInstanceVOPage = PageHelper.doPageAndSort(pageRequest, () -> devopsHostAppInstanceRelMapper.queryInstanceListByHostId(hostId, searchType, searchParam));
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

    private void handleNormalProcess(List<DevopsHostInstanceVO> devopsNormalInstances, List<DevopsHostInstanceVO> hostInstances) {
        if (!CollectionUtils.isEmpty(devopsNormalInstances)) {
            List<Long> normalInstanceIds = devopsNormalInstances.stream().map(DevopsHostInstanceVO::getId).collect(Collectors.toList());
            List<DevopsNormalInstanceDTO> devopsNormalInstanceDTOS = devopsNormalInstanceMapper.selectByIds(Joiner.on(BaseConstants.Symbol.COMMA).join(normalInstanceIds));
            List<DevopsNormalInstanceVO> devopsNormalInstanceVOS = ConvertUtils.convertList(devopsNormalInstanceDTOS, DevopsNormalInstanceVO.class);
            devopsNormalInstanceVOS.forEach(devopsNormalInstanceVO -> {
                //加上操作状态
                devopsNormalInstanceVO.setDevopsHostCommandDTO(devopsHostCommandMapper.selectLatestByInstanceId(devopsNormalInstanceVO.getId()));
            });
            hostInstances.addAll(devopsNormalInstanceVOS);
        }
    }

    private Page<?> queryHostInstances(Long projectId, Long hostId, Long appServiceId, PageRequest pageRequest, String searchType, String searchParam) {
        Page<DevopsHostAppInstanceRelDTO> hostAppInstanceRelDTOPage = PageHelper.doPageAndSort(pageRequest, () -> devopsHostAppInstanceRelMapper.queryInstanceListByHostIdAndAppId(projectId, hostId, appServiceId, searchType, searchParam));
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
        List<DevopsHostAppInstanceRelDTO> normalHostInstances = hostAppInstanceRelDTOPage.getContent().stream().filter(hostAppInstanceRelDTO -> StringUtils.equalsIgnoreCase(hostAppInstanceRelDTO.getInstanceType(), HostInstanceType.NORMAL_PROCESS.value())).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(normalHostInstances)) {
            List<Long> normalInstanceIds = normalHostInstances.stream().map(DevopsHostAppInstanceRelDTO::getInstanceId).collect(Collectors.toList());
            List<DevopsNormalInstanceDTO> devopsNormalInstanceDTOS = devopsNormalInstanceMapper.selectByIds(Joiner.on(BaseConstants.Symbol.COMMA).join(normalInstanceIds));
            List<DevopsNormalInstanceVO> devopsNormalInstanceVOS = ConvertUtils.convertList(devopsNormalInstanceDTOS, DevopsNormalInstanceVO.class);
            devopsNormalInstanceVOS.forEach(devopsNormalInstanceVO -> {
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

    private void fillUpdaterInfo(Page<DevopsHostVO> devopsHostVOS) {
        List<Long> userIds = devopsHostVOS.getContent().stream().map(DevopsHostVO::getLastUpdatedBy).collect(Collectors.toList());
        Map<Long, IamUserDTO> userInfo = baseServiceClientOperator.listUsersByIds(userIds).stream().collect(Collectors.toMap(IamUserDTO::getId, Functions.identity()));
        devopsHostVOS.getContent().forEach(host -> host.setUpdaterInfo(userInfo.get(host.getLastUpdatedBy())));
    }

    private String generateRedisKey(Long projectId, Long hostId) {
        return "host:connect:" + projectId + ":" + hostId;
    }

    @Async
    void automaticHost(DevopsHostConnectionVO devopsHostConnectionVO, Map<String, String> map, String redisKey, String commend) {
        devopsHostAdditionalCheckValidator.validHostInformationMatch(Objects.requireNonNull(ConvertUtils.convertObject(devopsHostConnectionVO, DevopsHostCreateRequestVO.class)));
        SSHClient sshClient = null;
        try {
            sshClient = SshUtil.sshConnect(devopsHostConnectionVO.getHostIp(), devopsHostConnectionVO.getSshPort(), devopsHostConnectionVO.getAuthType(), devopsHostConnectionVO.getUsername(), devopsHostConnectionVO.getPassword());
            ExecResultInfoVO execResultInfoVO = sshUtil.execCommand(sshClient, commend);
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
