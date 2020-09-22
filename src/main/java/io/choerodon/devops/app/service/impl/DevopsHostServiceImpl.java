package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import com.google.common.base.Functions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
    private static final long OPERATING_TIMEOUT = 60 * 1000;

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
        if (!CollectionUtils.isEmpty(deployHosts)) {
            devopsHostMapper.batchSetStatusOperating(projectId, deployHosts.stream().map(DevopsHostDTO::getId).collect(Collectors.toSet()), false, current);
        }
        if (!CollectionUtils.isEmpty(testHosts)) {
            devopsHostMapper.batchSetStatusOperating(projectId, testHosts.stream().map(DevopsHostDTO::getId).collect(Collectors.toSet()), true, current);
        }

        return hosts.stream().map(DevopsHostDTO::getId).collect(Collectors.toSet());
    }

    @Async(GitOpsConstants.HOST_STATUS_EXECUTOR)
    @Override
    public void asyncBatchCorrectStatus(Long projectId, Set<Long> hostIds) {
        LOGGER.debug("asyncBatchCorrectStatus: projectId: {}, hostIds: {}", projectId, hostIds);
        // 这么调用, 是解决事务代理不生效问题
        hostIds.forEach(hostId -> ApplicationContextHelper.getContext().getBean(DevopsHostService.class).correctStatus(projectId, hostId));
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

    @Transactional
    @Override
    public void correctStatus(Long projectId, Long hostId) {
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

        DevopsHostDTO toUpdate = ConvertUtils.convertObject(devopsHostUpdateRequestVO, DevopsHostDTO.class);
        if (DevopsHostType.DISTRIBUTE_TEST.getValue().equalsIgnoreCase(devopsHostDTO.getType())) {
            devopsHostAdditionalCheckValidator.validJmeterPort(devopsHostUpdateRequestVO.getJmeterPort());
            if (ipChanged || !devopsHostDTO.getJmeterPort().equals(devopsHostUpdateRequestVO.getJmeterPort())) {
                devopsHostAdditionalCheckValidator.validIpAndJmeterPortProjectUnique(projectId, devopsHostUpdateRequestVO.getHostIp(), devopsHostUpdateRequestVO.getJmeterPort());
            }
            devopsHostAdditionalCheckValidator.validJmeterPath(devopsHostUpdateRequestVO.getJmeterPath());
            toUpdate.setJmeterStatus(DevopsHostStatus.OPERATING.getValue());
        }

        toUpdate.setHostStatus(DevopsHostStatus.OPERATING.getValue());
        toUpdate.setId(devopsHostDTO.getId());
        toUpdate.setObjectVersionNumber(devopsHostDTO.getObjectVersionNumber());
        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsHostMapper, toUpdate, "error.update.host");
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
        DevopsHostConnectionTestResultVO result = new DevopsHostConnectionTestResultVO();
        boolean sshConnected = SshUtil.sshConnect(devopsHostConnectionTestVO.getHostIp(), devopsHostConnectionTestVO.getSshPort(), devopsHostConnectionTestVO.getAuthType(), devopsHostConnectionTestVO.getUsername(), devopsHostConnectionTestVO.getPassword());
        result.setHostStatus(sshConnected ? DevopsHostStatus.SUCCESS.getValue() : DevopsHostStatus.FAILED.getValue());
        if (!sshConnected) {
            result.setHostCheckError("failed to check ssh, please ensure network and authentication is valid");
        }

        // 如果是测试类型的主机, 再测试下jmeter的状态
        if (DevopsHostType.DISTRIBUTE_TEST.getValue().equals(devopsHostConnectionTestVO.getType())) {
            boolean jmeterConnected = testServiceClientOperator.testJmeterConnection(devopsHostConnectionTestVO.getHostIp(), devopsHostConnectionTestVO.getJmeterPort());
            result.setJmeterStatus(jmeterConnected ? DevopsHostStatus.SUCCESS.getValue() : DevopsHostStatus.FAILED.getValue());
            if (!jmeterConnected) {
                result.setJmeterCheckError("failed to check jmeter， please ensure network and jmeter server running");
            }
        }

        return result;
    }

    @Override
    public Boolean testConnectionByIdForDeployHost(Long projectId, Long hostId) {
        DevopsHostDTO devopsHostDTO = devopsHostMapper.selectByPrimaryKey(hostId);
        if (devopsHostDTO == null) {
            return false;
        }

        CommonExAssertUtil.assertTrue(projectId.equals(devopsHostDTO.getProjectId()), MiscConstants.ERROR_OPERATING_RESOURCE_IN_OTHER_PROJECT);
        CommonExAssertUtil.assertTrue(DevopsHostType.DEPLOY.getValue().equals(devopsHostDTO.getType()), "error.host.type.invalid");

        return SshUtil.sshConnect(devopsHostDTO.getHostIp(), devopsHostDTO.getSshPort(), devopsHostDTO.getAuthType(), devopsHostDTO.getUsername(), devopsHostDTO.getPassword());
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
        devopsHostDTOS.forEach(host -> {
            if (isOperating(host.getType(), host.getHostStatus(), host.getJmeterStatus())
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
            return Boolean.TRUE;
        }
        //测试主机，状态占用中不能删除
        if (DevopsHostType.DISTRIBUTE_TEST.getValue().equalsIgnoreCase(devopsHostDTO.getType().trim())
                && DevopsHostStatus.OCCUPIED.getValue().equalsIgnoreCase(devopsHostDTO.getHostStatus().trim())) {
            return Boolean.FALSE;
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

    private void fillUpdaterInfo(Page<DevopsHostVO> devopsHostVOS) {
        List<Long> userIds = devopsHostVOS.getContent().stream().map(DevopsHostVO::getLastUpdatedBy).collect(Collectors.toList());
        Map<Long, IamUserDTO> userInfo = baseServiceClientOperator.listUsersByIds(userIds).stream().collect(Collectors.toMap(IamUserDTO::getId, Functions.identity()));
        devopsHostVOS.getContent().forEach(host -> host.setUpdaterInfo(userInfo.get(host.getLastUpdatedBy())));
    }
}
