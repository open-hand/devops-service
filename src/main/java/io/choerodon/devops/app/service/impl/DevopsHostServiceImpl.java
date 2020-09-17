package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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

import io.choerodon.core.domain.Page;
import io.choerodon.core.utils.ConvertUtils;
import io.choerodon.devops.api.validator.DevopsHostAdditionalCheckValidator;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.service.DevopsHostService;
import io.choerodon.devops.infra.constant.GitOpsConstants;
import io.choerodon.devops.infra.constant.MiscConstants;
import io.choerodon.devops.infra.dto.DevopsHostDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.enums.DevopsHostStatus;
import io.choerodon.devops.infra.enums.DevopsHostType;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
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

    @Autowired
    private DevopsHostMapper devopsHostMapper;
    @Lazy
    @Autowired
    private DevopsHostAdditionalCheckValidator devopsHostAdditionalCheckValidator;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;


    @Transactional(rollbackFor = Exception.class)
    @Override
    public DevopsHostVO createHost(Long projectId, DevopsHostCreateRequestVO devopsHostCreateRequestVO) {
        // 补充校验参数
        devopsHostAdditionalCheckValidator.validNameProjectUnique(projectId, devopsHostCreateRequestVO.getName());
        devopsHostAdditionalCheckValidator.validIpAndSshPortProjectUnique(projectId, devopsHostCreateRequestVO.getHostIp(), devopsHostCreateRequestVO.getSshPort());
        devopsHostAdditionalCheckValidator.validIpAndJmeterPortProjectUnique(projectId, devopsHostCreateRequestVO.getHostIp(), devopsHostCreateRequestVO.getJmeterPort());

        DevopsHostDTO devopsHostDTO = ConvertUtils.convertObject(devopsHostCreateRequestVO, DevopsHostDTO.class);
        devopsHostDTO.setProjectId(projectId);

        if (DevopsHostType.DISTRIBUTE_TEST.getValue().equalsIgnoreCase(devopsHostCreateRequestVO.getType())) {
            devopsHostAdditionalCheckValidator.validJmeterPort(devopsHostCreateRequestVO.getJmeterPort());
            devopsHostDTO.setJmeterStatus(DevopsHostStatus.OPERATING.getValue());
        }

        devopsHostDTO.setHostStatus(DevopsHostStatus.OPERATING.getValue());
        return ConvertUtils.convertObject(MapperUtil.resultJudgedInsert(devopsHostMapper, devopsHostDTO, "error.insert.host"), DevopsHostVO.class);
    }

    @Override
    public void batchSetStatusOperating(Long projectId, Set<Long> hostIds) {
        LOGGER.debug("batchSetStatusOperating: projectId: {}, hostIds: {}", projectId, hostIds);
        if (CollectionUtils.isEmpty(hostIds)) {
            return;
        }
        // 这里不需要校验host属于这个项目，因为让项目id参与SQL条件了
        devopsHostMapper.batchSetStatusOperating(projectId, hostIds);
    }

    @Async(GitOpsConstants.HOST_STATUS_EXECUTOR)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void asyncBatchCorrectStatus(Long projectId, Set<Long> hostIds) {
        LOGGER.debug("asyncBatchCorrectStatus: projectId: {}, hostIds: {}", projectId, hostIds);
        // TODO 待优化
        hostIds.forEach(hostId -> {
            DevopsHostDTO hostDTO = devopsHostMapper.selectByPrimaryKey(hostId);
            if (hostDTO == null) {
                return;
            }
            DevopsHostConnectionTestVO devopsHostConnectionTestVO = ConvertUtils.convertObject(hostDTO, DevopsHostConnectionTestVO.class);

            DevopsHostConnectionTestResultVO result = testConnection(projectId, devopsHostConnectionTestVO);
            hostDTO.setHostStatus(result.getHostStatus());
            hostDTO.setHostCheckError(result.getHostCheckError());
            hostDTO.setJmeterStatus(result.getJmeterStatus());
            hostDTO.setJmeterCheckError(result.getJmeterCheckError());
            MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsHostMapper, hostDTO, "error.update.host");
            LOGGER.debug("connection result for host with id {} is {}", hostId, result);
        });
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public DevopsHostVO updateHost(Long projectId, Long hostId, DevopsHostUpdateRequestVO devopsHostUpdateRequestVO) {
        DevopsHostDTO devopsHostDTO = devopsHostMapper.selectByPrimaryKey(hostId);
        CommonExAssertUtil.assertNotNull(devopsHostDTO, "error.host.not.exist", hostId);
        CommonExAssertUtil.assertTrue(devopsHostDTO.getProjectId().equals(projectId), MiscConstants.ERROR_OPERATING_RESOURCE_IN_OTHER_PROJECT);

        // 补充校验参数
        devopsHostAdditionalCheckValidator.validNameProjectUnique(projectId, devopsHostUpdateRequestVO.getName());
        devopsHostAdditionalCheckValidator.validIpAndSshPortProjectUnique(projectId, devopsHostUpdateRequestVO.getHostIp(), devopsHostUpdateRequestVO.getSshPort());
        devopsHostAdditionalCheckValidator.validIpAndJmeterPortProjectUnique(projectId, devopsHostUpdateRequestVO.getHostIp(), devopsHostUpdateRequestVO.getJmeterPort());

        DevopsHostDTO toUpdate = ConvertUtils.convertObject(devopsHostUpdateRequestVO, DevopsHostDTO.class);
        if (DevopsHostType.DISTRIBUTE_TEST.getValue().equalsIgnoreCase(devopsHostDTO.getType())) {
            devopsHostAdditionalCheckValidator.validJmeterPort(devopsHostUpdateRequestVO.getJmeterPort());
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
        return ConvertUtils.convertObject(devopsHostMapper.selectByPrimaryKey(hostId), DevopsHostVO.class);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteHost(Long projectId, Long hostId) {
        // TODO 校验是否可删除

        DevopsHostDTO devopsHostDTO = devopsHostMapper.selectByPrimaryKey(hostId);
        if (devopsHostDTO == null) {
            return;
        }

        CommonExAssertUtil.assertTrue(devopsHostDTO.getProjectId().equals(projectId), MiscConstants.ERROR_OPERATING_RESOURCE_IN_OTHER_PROJECT);

        devopsHostMapper.deleteByPrimaryKey(hostId);
    }

    @Override
    public DevopsHostConnectionTestResultVO testConnection(Long projectId, DevopsHostConnectionTestVO devopsHostConnectionTestVO) {
        DevopsHostConnectionTestResultVO result = new DevopsHostConnectionTestResultVO();
        boolean sshConnected = SshUtil.sshConnect(devopsHostConnectionTestVO.getHostIp(), devopsHostConnectionTestVO.getSshPort(), devopsHostConnectionTestVO.getAuthType(), devopsHostConnectionTestVO.getUsername(), devopsHostConnectionTestVO.getPassword());
        result.setHostStatus(sshConnected ? DevopsHostStatus.SUCCESS.getValue() : DevopsHostStatus.FAILED.getValue());

        // 如果是测试类型的主机, 再测试下jmeter的状态
        if (DevopsHostType.DISTRIBUTE_TEST.getValue().equals(devopsHostConnectionTestVO.getType())) {
            boolean jmeterConnected = JmeterUtil.testJmeterConnections(devopsHostConnectionTestVO.getHostIp(), devopsHostConnectionTestVO.getJmeterPort());
            result.setJmeterStatus(jmeterConnected ? DevopsHostStatus.SUCCESS.getValue() : DevopsHostStatus.FAILED.getValue());
        }

        // TODO 设置真正的错误信息
        result.setJmeterCheckError("failed to check jmeter");
        result.setHostCheckError("failed to check host");
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
        // 分页查询
        Page<DevopsHostVO> result = ConvertUtils.convertPage(PageHelper.doPageAndSort(PageRequestUtil.simpleConvertSortForPage(pageRequest), () -> devopsHostMapper.listByOptions(projectId, TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM)), TypeUtil.cast(maps.get(TypeUtil.PARAMS)))), DevopsHostVO.class);
        if (withUpdaterInfo) {
            // 填充更新者用户信息
            fillUpdaterInfo(result);
        }
        return result;
    }

    private void fillUpdaterInfo(Page<DevopsHostVO> devopsHostVOS) {
        List<Long> userIds = devopsHostVOS.getContent().stream().map(DevopsHostVO::getLastUpdatedBy).collect(Collectors.toList());
        Map<Long, IamUserDTO> userInfo = baseServiceClientOperator.listUsersByIds(userIds).stream().collect(Collectors.toMap(IamUserDTO::getId, Functions.identity()));
        devopsHostVOS.getContent().forEach(host -> host.setUpdaterInfo(userInfo.get(host.getLastUpdatedBy())));
    }
}
