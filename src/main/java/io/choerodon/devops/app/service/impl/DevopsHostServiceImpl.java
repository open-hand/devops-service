package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import com.google.common.base.Functions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.hzero.core.util.UUIDUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
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
import io.choerodon.devops.app.service.DevopsHostService;
import io.choerodon.devops.app.service.EncryptService;
import io.choerodon.devops.infra.constant.GitOpsConstants;
import io.choerodon.devops.infra.constant.MiscConstants;
import io.choerodon.devops.infra.dto.DevopsCdJobDTO;
import io.choerodon.devops.infra.dto.DevopsHostDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.TestServiceClientOperator;
import io.choerodon.devops.infra.mapper.DevopsCdJobMapper;
import io.choerodon.devops.infra.mapper.DevopsHostMapper;
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
    /**
     * 主机状态处于处理中的超时时长
     */
    private static final long OPERATING_TIMEOUT = 300L * 1000;
    private static final String CHECKING_HOST = "checking";

    /**
     * 主机占用的锁的redis key, 变量是主机id
     */
    public static final String HOST_OCCUPY_REDIS_KEY_TEMPLATE = "devops-service:hosts:host-occupy-%s";

    /**
     * 占用主机的过期时间, 超过这个时间, 主机会被释放
     */
    @Value("${devops.host.occupy.timeout-hours:24}")
    private long hostOccupyTimeoutHours;

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
    private TestServiceClientOperator testServiceClientOperator;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    EncryptService encryptService;

    private final Gson gson = new Gson();


    @Transactional(rollbackFor = Exception.class)
    @Override
    public DevopsHostVO createHost(Long projectId, DevopsHostCreateRequestVO devopsHostCreateRequestVO) {
        // 补充校验参数
        devopsHostAdditionalCheckValidator.validNameProjectUnique(projectId, devopsHostCreateRequestVO.getName());
        devopsHostAdditionalCheckValidator.validIpAndSshPortProjectUnique(projectId, devopsHostCreateRequestVO.getHostIp(), devopsHostCreateRequestVO.getSshPort());

        DevopsHostDTO devopsHostDTO = ConvertUtils.convertObject(devopsHostCreateRequestVO, DevopsHostDTO.class);
        devopsHostDTO.setProjectId(projectId);

        if (DevopsHostType.DISTRIBUTE_TEST.getValue().equalsIgnoreCase(devopsHostCreateRequestVO.getType())) {
            devopsHostAdditionalCheckValidator.validJmeterPort(devopsHostCreateRequestVO.getJmeterPort());
            devopsHostAdditionalCheckValidator.validIpAndJmeterPortProjectUnique(projectId, devopsHostCreateRequestVO.getHostIp(), devopsHostCreateRequestVO.getJmeterPort());
            devopsHostAdditionalCheckValidator.validJmeterPath(devopsHostCreateRequestVO.getJmeterPath());
            devopsHostDTO.setJmeterStatus(DevopsHostStatus.OPERATING.getValue());
        }

        devopsHostDTO.setHostStatus(DevopsHostStatus.OPERATING.getValue());
        return ConvertUtils.convertObject(MapperUtil.resultJudgedInsert(devopsHostMapper, devopsHostDTO, "error.insert.host"), DevopsHostVO.class);
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

        // 设置状态为处理中
        Date current = new Date();
        Long updateUserId = DetailsHelper.getUserDetails().getUserId();
        if (!CollectionUtils.isEmpty(deployHosts)) {
            devopsHostMapper.batchSetStatusOperating(projectId, deployHosts.stream().map(DevopsHostDTO::getId).collect(Collectors.toSet()), false, current, updateUserId);
        }
        if (!CollectionUtils.isEmpty(testHosts)) {
            devopsHostMapper.batchSetStatusOperating(projectId, testHosts.stream().map(DevopsHostDTO::getId).collect(Collectors.toSet()), true, current, updateUserId);
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

        redisTemplate.opsForValue().set(correctKey, gson.toJson(map), 10, TimeUnit.MINUTES);
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

    @Transactional(rollbackFor = Exception.class)
    @Async(GitOpsConstants.HOST_STATUS_EXECUTOR)
    @Override
    public void asyncBatchUnOccupyHosts(Long projectId, Set<Long> hostIds) {
        LOGGER.debug("asyncBatchUnOccupyHosts: projectId: {}, hostIds: {}", projectId, hostIds);
        if (CollectionUtils.isEmpty(hostIds)) {
            return;
        }

        List<DevopsHostDTO> hosts = devopsHostMapper.listByProjectIdAndIds(projectId, hostIds);
        if (CollectionUtils.isEmpty(hosts)) {
            return;
        }

        // 释放主机
        unOccupyHosts(projectId, hostIds);
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
            hostDTO.setJmeterStatus(result.getJmeterStatus());
            hostDTO.setJmeterCheckError(result.getJmeterCheckError());
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
            hostDTO.setJmeterStatus(result.getJmeterStatus());
            hostDTO.setJmeterCheckError(result.getJmeterCheckError());
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
        while (!Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(lockKey, "lock", 10, TimeUnit.MINUTES))) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                LOGGER.info("sleep.failed.", e);
            }
        }
        try {
            Map<Long, String> hostStatus = gson.fromJson(redisTemplate.opsForValue().get(correctKey), new TypeToken<Map<Long, String>>() {
            }.getType());
            if (hostStatus == null) {
                hostStatus = new HashMap<>();
            }
            hostStatus.put(hostId, status);
            redisTemplate.opsForValue().set(correctKey, gson.toJson(hostStatus), 10, TimeUnit.MINUTES);
        } finally {
            redisTemplate.delete(lockKey);
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
            // 跳过占用中的测试主机
            if (DevopsHostStatus.OCCUPIED.getValue().equals(hostDTO.getJmeterStatus())) {
                return false;
            }
            // 过滤测试中的主机
            if (isOperating(hostDTO.getType(), hostDTO.getHostStatus(), hostDTO.getJmeterStatus())
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
     * @param type         主机类型
     * @param hostStatus   主机状态
     * @param jmeterStatus jmeter状态
     * @return true表示是处理中
     */
    private boolean isOperating(String type, String hostStatus, String jmeterStatus) {
        if (DevopsHostType.DEPLOY.getValue().equalsIgnoreCase(type)) {
            return DevopsHostStatus.OPERATING.getValue().equals(hostStatus);
        } else {
            return isDistributeHostOperating(hostStatus, jmeterStatus);
        }
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

    private boolean isDistributeHostOperating(String hostStatus, String jmeterStatus) {
        // 测试主机, 任意一个状态失败则失败, 两个状态都成功则成功, jmeter状态为占用就是占用, 否则都是处理中
        return !DevopsHostStatus.FAILED.getValue().equals(hostStatus)
                && !DevopsHostStatus.FAILED.getValue().equals(jmeterStatus)
                && !DevopsHostStatus.OCCUPIED.getValue().equals(jmeterStatus)
                && !(DevopsHostStatus.SUCCESS.getValue().equals(hostStatus) && DevopsHostStatus.SUCCESS.getValue().equals(jmeterStatus));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public DevopsHostVO updateHost(Long projectId, Long hostId, DevopsHostUpdateRequestVO devopsHostUpdateRequestVO) {
        DevopsHostDTO devopsHostDTO = devopsHostMapper.selectByPrimaryKey(hostId);
        CommonExAssertUtil.assertNotNull(devopsHostDTO, "error.host.not.exist", hostId);
        CommonExAssertUtil.assertTrue(devopsHostDTO.getProjectId().equals(projectId), MiscConstants.ERROR_OPERATING_RESOURCE_IN_OTHER_PROJECT);

        // 补充校验参数
        if (!devopsHostDTO.getName().equals(devopsHostUpdateRequestVO.getName())) {
            devopsHostAdditionalCheckValidator.validNameProjectUnique(projectId, devopsHostUpdateRequestVO.getName());
        }
        boolean ipChanged = !devopsHostDTO.getHostIp().equals(devopsHostUpdateRequestVO.getHostIp());
        if (ipChanged || !devopsHostDTO.getSshPort().equals(devopsHostUpdateRequestVO.getSshPort())) {
            devopsHostAdditionalCheckValidator.validIpAndSshPortProjectUnique(projectId, devopsHostUpdateRequestVO.getHostIp(), devopsHostUpdateRequestVO.getSshPort());
        }

        if (DevopsHostType.DISTRIBUTE_TEST.getValue().equalsIgnoreCase(devopsHostDTO.getType())) {
            devopsHostAdditionalCheckValidator.validJmeterPort(devopsHostUpdateRequestVO.getJmeterPort());
            if (ipChanged || !devopsHostDTO.getJmeterPort().equals(devopsHostUpdateRequestVO.getJmeterPort())) {
                devopsHostAdditionalCheckValidator.validIpAndJmeterPortProjectUnique(projectId, devopsHostUpdateRequestVO.getHostIp(), devopsHostUpdateRequestVO.getJmeterPort());
            }
            devopsHostAdditionalCheckValidator.validJmeterPath(devopsHostUpdateRequestVO.getJmeterPath());
            devopsHostDTO.setJmeterStatus(DevopsHostStatus.OPERATING.getValue());
        }

        devopsHostDTO.setName(devopsHostUpdateRequestVO.getName());
        devopsHostDTO.setUsername(devopsHostUpdateRequestVO.getUsername());
        devopsHostDTO.setPassword(devopsHostUpdateRequestVO.getPassword());
        devopsHostDTO.setAuthType(devopsHostUpdateRequestVO.getAuthType());
        devopsHostDTO.setHostIp(devopsHostUpdateRequestVO.getHostIp());
        devopsHostDTO.setSshPort(devopsHostUpdateRequestVO.getSshPort());
        devopsHostDTO.setPrivateIp(devopsHostUpdateRequestVO.getPrivateIp());
        devopsHostDTO.setPrivatePort(devopsHostUpdateRequestVO.getPrivatePort());

        devopsHostDTO.setHostStatus(DevopsHostStatus.OPERATING.getValue());
        MapperUtil.resultJudgedUpdateByPrimaryKey(devopsHostMapper, devopsHostDTO, "error.update.host");
        return queryHost(projectId, hostId);
    }

    @Override
    public DevopsHostVO queryHost(Long projectId, Long hostId) {
        DevopsHostDTO devopsHostDTO = devopsHostMapper.selectByPrimaryKey(hostId);
        if (devopsHostDTO == null || !projectId.equals(devopsHostDTO.getProjectId())) {
            return null;
        }

        // 校验超时
        checkTimeout(projectId, ArrayUtil.singleAsList(devopsHostDTO));

        return ConvertUtils.convertObject(devopsHostDTO, DevopsHostVO.class);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteHost(Long projectId, Long hostId) {
        DevopsHostDTO devopsHostDTO = devopsHostMapper.selectByPrimaryKey(hostId);
        if (devopsHostDTO == null) {
            return;
        }

        CommonExAssertUtil.assertTrue(devopsHostDTO.getProjectId().equals(projectId), MiscConstants.ERROR_OPERATING_RESOURCE_IN_OTHER_PROJECT);

        if (!checkHostDelete(projectId, hostId)) {
            throw new CommonException("error.delete.host.already.referenced.in.pipeline");
        }

        devopsHostMapper.deleteByPrimaryKey(hostId);
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

            // 如果是测试类型的主机, 再测试下jmeter的状态
            if (DevopsHostType.DISTRIBUTE_TEST.getValue().equals(devopsHostConnectionTestVO.getType())) {
                // ssh测试成功才有必要测试jmeter状态
                if (DevopsHostStatus.SUCCESS.getValue().equals(result.getHostStatus())) {
                    boolean jmeterConnected = testServiceClientOperator.testJmeterConnection(devopsHostConnectionTestVO.getHostIp(), devopsHostConnectionTestVO.getJmeterPort());
                    if (!jmeterConnected) {
                        result.setJmeterCheckError("failed to check jmeter， please ensure network and jmeter server running");
                        result.setJmeterStatus(DevopsHostStatus.FAILED.getValue());
                    } else {
                        boolean jmeterPathValid = SshUtil.execForOk(sshClient, String.format(MiscConstants.LS_JMETER_COMMAND, devopsHostConnectionTestVO.getJmeterPath()));
                        if (jmeterPathValid) {
                            result.setJmeterStatus(DevopsHostStatus.SUCCESS.getValue());
                        } else {
                            result.setJmeterStatus(DevopsHostStatus.FAILED.getValue());
                            result.setJmeterCheckError("failed to check jmeter script. please ensure jmeter home is valid");
                        }
                    }
                } else {
                    result.setJmeterStatus(DevopsHostStatus.FAILED.getValue());
                    result.setJmeterCheckError("failed due to ssh failed");
                }
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
    public boolean isIpJmeterPortUnique(Long projectId, String ip, Integer jmeterPort) {
        DevopsHostDTO condition = new DevopsHostDTO();
        condition.setProjectId(Objects.requireNonNull(projectId));
        condition.setHostIp(Objects.requireNonNull(ip));
        condition.setJmeterPort(Objects.requireNonNull(jmeterPort));
        return devopsHostMapper.selectCount(condition) == 0;
    }

    @Override
    public Page<DevopsHostVO> pageByOptions(Long projectId, PageRequest pageRequest, boolean withUpdaterInfo, @Nullable String options) {
        // 解析查询参数
        Map<String, Object> maps = TypeUtil.castMapParams(options);
        Page<DevopsHostDTO> page = PageHelper.doPageAndSort(PageRequestUtil.simpleConvertSortForPage(pageRequest), () -> devopsHostMapper.listByOptions(projectId, TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM)), TypeUtil.cast(maps.get(TypeUtil.PARAMS))));

        // 校验超时
        checkTimeout(projectId, page.getContent());

        // 分页查询
        Page<DevopsHostVO> result = ConvertUtils.convertPage(page, DevopsHostVO.class);
        if (withUpdaterInfo) {
            // 填充更新者用户信息
            fillUpdaterInfo(result);
        }
        return result;
    }

    /**
     * 校验主机的状态是否超时
     *
     * @param projectId      项目id
     * @param devopsHostDTOS 主机
     */
    private void checkTimeout(Long projectId, List<DevopsHostDTO> devopsHostDTOS) {
        Set<Long> ids = new HashSet<>();
        Set<Long> occupyTimeoutHosts = new HashSet<>();
        devopsHostDTOS.forEach(host -> {
            // 收集校验超时的主机
            if (isOperating(host.getType(), host.getHostStatus(), host.getJmeterStatus())
                    && isTimeout(host.getLastUpdateDate())) {
                ids.add(host.getId());
            }

            // 收集占用超时的主机
            if (DevopsHostStatus.OCCUPIED.getValue().equals(host.getJmeterStatus())
                    && isHostOccupiedTimeout(host.getLastUpdateDate())) {
                occupyTimeoutHosts.add(host.getId());
            }
        });

        // 不为空 异步设置失败
        if (!ids.isEmpty()) {
            ApplicationContextHelper.getContext().getBean(DevopsHostService.class).asyncBatchSetTimeoutHostFailed(projectId, ids);
        }

        // 异步释放被占用的主机
        if (!occupyTimeoutHosts.isEmpty()) {
            ApplicationContextHelper.getContext().getBean(DevopsHostService.class).asyncBatchUnOccupyHosts(projectId, occupyTimeoutHosts);
        }
    }

    /**
     * 主机被占用的时长是否超时了
     *
     * @param lastUpdateDate 最后更新时间
     * @return true表示超时
     */
    private boolean isHostOccupiedTimeout(Date lastUpdateDate) {
        long current = System.currentTimeMillis();
        long lastUpdate = lastUpdateDate.getTime();
        long hourToMillis = 60L * 60L * 1000L;
        // 最后更新时间加上过期时间的毫秒数是否大于现在的毫秒数
        return (lastUpdate + hostOccupyTimeoutHours * hourToMillis) < current;
    }

    @Override
    public boolean checkHostDelete(Long projectId, Long hostId) {
        DevopsHostDTO devopsHostDTO = devopsHostMapper.selectByPrimaryKey(hostId);
        if (Objects.isNull(devopsHostDTO)) {
            return Boolean.TRUE;
        }
        //测试主机，状态占用中不能删除
        if (DevopsHostType.DISTRIBUTE_TEST.getValue().equalsIgnoreCase(devopsHostDTO.getType().trim())) {
            if (DevopsHostStatus.OCCUPIED.getValue().equalsIgnoreCase(devopsHostDTO.getHostStatus().trim())) {
                return Boolean.FALSE;
            } else {
                return Boolean.TRUE;
            }
        }
        DevopsCdJobDTO devopsCdJobDTO = new DevopsCdJobDTO();
        devopsCdJobDTO.setProjectId(projectId);
        devopsCdJobDTO.setType(JobTypeEnum.CD_HOST.value());
        List<DevopsCdJobDTO> devopsCdJobDTOS = devopsCdJobMapper.select(devopsCdJobDTO);
        if (CollectionUtils.isEmpty(devopsCdJobDTOS)) {
            return Boolean.TRUE;
        }
        for (DevopsCdJobDTO cdJobDTO : devopsCdJobDTOS) {
            CdHostDeployConfigVO cdHostDeployConfigVO = JsonHelper.unmarshalByJackson(cdJobDTO.getMetadata(), CdHostDeployConfigVO.class);
            if (!HostDeployType.CUSTOMIZE_DEPLOY.getValue().equalsIgnoreCase(cdHostDeployConfigVO.getHostDeployType().trim())) {
                continue;
            }
            HostConnectionVO hostConnectionVO = cdHostDeployConfigVO.getHostConnectionVO();
            if (!HostSourceEnum.EXISTHOST.getValue().equalsIgnoreCase(hostConnectionVO.getHostSource().trim())) {
                continue;
            }
            if (hostConnectionVO.getHostId().equals(hostId)) {
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    @Override
    public CheckingProgressVO getCheckingProgress(Long projectId, String correctKey) {
        Map<Long, String> hostStatusMap = gson.fromJson(redisTemplate.opsForValue().get(correctKey), new TypeToken<Map<Long, String>>() {
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
            Map<Long, String> hostStatusMap = gson.fromJson(redisTemplate.opsForValue().get(correctKey), new TypeToken<Map<Long, String>>() {
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
    public List<DevopsHostDTO> listDistributionTestHostsByIds(Long projectId, Set<Long> hostIds) {
        if (CollectionUtils.isEmpty(hostIds)) {
            return Collections.emptyList();
        }
        return devopsHostMapper.listDistributionTestHostsByIds(projectId, hostIds);
    }

    @Override
    public Page<DevopsHostDTO> listDistributionTestHosts(PageRequest pageRequest) {
        return PageHelper.doPage(pageRequest, () -> devopsHostMapper.listDistributionTestHosts());
    }

    @Override
    public boolean occupyHosts(Long projectId, Long recordId, Set<Long> hostIds) {
        if (CollectionUtils.isEmpty(hostIds)) {
            return false;
        }

        // 锁key
        String lockKey;
        // 锁值
        String value = recordId.toString();
        // 加锁成功的key
        List<String> locked = new ArrayList<>();
        // 是否有某个加锁失败
        boolean failedToLock = false;

        try {
            // 尝试给所有id加锁
            for (Long hostId : hostIds) {
                lockKey = hostOccupyKey(hostId);
                Boolean result = redisTemplate.opsForValue().setIfAbsent(lockKey, value, hostOccupyTimeoutHours, TimeUnit.HOURS);
                if (Boolean.TRUE.equals(result)) {
                    locked.add(lockKey);
                } else {
                    LOGGER.debug("Failed to acquire lock for host with id {}", hostId);
                    failedToLock = true;
                    break;
                }
            }

            // 如果获取锁失败, 将已经获取成功的锁释放
            if (failedToLock) {
                releaseLocks(locked);
                return false;
            }

            List<DevopsHostDTO> hosts = devopsHostMapper.listDistributionTestHostsByIds(projectId, hostIds);
            // 如果查出的主机数量不一致, 认为失败
            if (hosts.size() != hostIds.size()) {
                LOGGER.debug("The size doesn't equals. expect: {}, actual: {}", hostIds.size(), hosts.size());
                releaseLocks(locked);
                return false;
            }

            // 将所有主机更新为占用中的状态
            devopsHostMapper.updateJmeterStatus(hostIds, DevopsHostStatus.OCCUPIED.getValue());
        } catch (Exception ex) {
            LOGGER.warn("Ex occurred when occupying host {}", hostIds);
            LOGGER.warn("The ex is:", ex);
            releaseLocks(locked);
            return false;
        }

        return true;
    }

    private void releaseLocks(List<String> keys) {
        keys.forEach(key -> redisTemplate.delete(key));
    }

    @Override
    public boolean unOccupyHosts(Long projectId, Set<Long> hostIds) {
        if (CollectionUtils.isEmpty(hostIds)) {
            return true;
        }

        // 锁key
        String lockKey;

        try {
            // 尝试给所有id解锁
            for (Long hostId : hostIds) {
                lockKey = hostOccupyKey(hostId);
                redisTemplate.delete(lockKey);
            }
            LOGGER.debug("Finished to delete redis locks for hosts {}", hostIds);

            // 将所有主机更新为成功中的状态
            devopsHostMapper.updateJmeterStatus(hostIds, DevopsHostStatus.SUCCESS.getValue());
        } catch (Exception ex) {
            LOGGER.warn("Ex occurred when un-occupying host {}", hostIds);
            LOGGER.warn("The ex is:", ex);
            return false;
        }

        return true;
    }

    private String hostOccupyKey(Long hostId) {
        return String.format(HOST_OCCUPY_REDIS_KEY_TEMPLATE, hostId);
    }

    private void fillUpdaterInfo(Page<DevopsHostVO> devopsHostVOS) {
        List<Long> userIds = devopsHostVOS.getContent().stream().map(DevopsHostVO::getLastUpdatedBy).collect(Collectors.toList());
        Map<Long, IamUserDTO> userInfo = baseServiceClientOperator.listUsersByIds(userIds).stream().collect(Collectors.toMap(IamUserDTO::getId, Functions.identity()));
        devopsHostVOS.getContent().forEach(host -> host.setUpdaterInfo(userInfo.get(host.getLastUpdatedBy())));
    }
}
