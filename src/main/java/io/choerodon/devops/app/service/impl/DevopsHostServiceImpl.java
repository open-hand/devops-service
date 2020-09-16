package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import com.google.common.base.Functions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.domain.Page;
import io.choerodon.core.utils.ConvertUtils;
import io.choerodon.devops.api.validator.DevopsHostAdditionalCheckValidator;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.service.DevopsHostService;
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
    @Autowired
    private DevopsHostMapper devopsHostMapper;
    @Autowired
    private DevopsHostAdditionalCheckValidator devopsHostAdditionalCheckValidator;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;


    @Override
    public DevopsHostVO createHost(Long projectId, DevopsHostCreateRequestVO devopsHostCreateRequestVO) {
        // 补充校验参数
        devopsHostAdditionalCheckValidator.validNameProjectUnique(projectId, devopsHostCreateRequestVO.getName());
        devopsHostAdditionalCheckValidator.validIpAndSshPortProjectUnique(projectId, devopsHostCreateRequestVO.getHostIp(), devopsHostCreateRequestVO.getSshPort());
        devopsHostAdditionalCheckValidator.validIpAndSshPortProjectUnique(projectId, devopsHostCreateRequestVO.getHostIp(), devopsHostCreateRequestVO.getJmeterPort());
        if (DevopsHostType.DISTRIBUTE_TEST.getValue().equalsIgnoreCase(devopsHostCreateRequestVO.getType())) {
            devopsHostAdditionalCheckValidator.validJmeterPort(devopsHostCreateRequestVO.getJmeterPort());
        }

        DevopsHostDTO devopsHostDTO = ConvertUtils.convertObject(devopsHostCreateRequestVO, DevopsHostDTO.class);
        return ConvertUtils.convertObject(MapperUtil.resultJudgedInsert(devopsHostMapper, devopsHostDTO, "error.insert.host"), DevopsHostVO.class);
        // TODO 再次异步校准状态
    }

    @Override
    public DevopsHostVO updateHost(Long projectId, Long hostId, DevopsHostUpdateRequestVO devopsHostUpdateRequestVO) {
        DevopsHostDTO devopsHostDTO = devopsHostMapper.selectByPrimaryKey(hostId);
        CommonExAssertUtil.assertNotNull(devopsHostDTO, "error.host.not.exist", hostId);
        CommonExAssertUtil.assertTrue(devopsHostDTO.getProjectId().equals(projectId), MiscConstants.ERROR_OPERATING_RESOURCE_IN_OTHER_PROJECT);

        // 补充校验参数
        devopsHostAdditionalCheckValidator.validNameProjectUnique(projectId, devopsHostUpdateRequestVO.getName());
        devopsHostAdditionalCheckValidator.validIpAndSshPortProjectUnique(projectId, devopsHostUpdateRequestVO.getHostIp(), devopsHostUpdateRequestVO.getSshPort());
        devopsHostAdditionalCheckValidator.validIpAndSshPortProjectUnique(projectId, devopsHostUpdateRequestVO.getHostIp(), devopsHostUpdateRequestVO.getJmeterPort());
        if (DevopsHostType.DISTRIBUTE_TEST.getValue().equalsIgnoreCase(devopsHostDTO.getType())) {
            devopsHostAdditionalCheckValidator.validJmeterPort(devopsHostUpdateRequestVO.getJmeterPort());
        }

        DevopsHostDTO toUpdate = ConvertUtils.convertObject(devopsHostUpdateRequestVO, DevopsHostDTO.class);
        toUpdate.setId(devopsHostDTO.getId());
        toUpdate.setObjectVersionNumber(devopsHostDTO.getObjectVersionNumber());
        devopsHostMapper.updateByPrimaryKeySelective(toUpdate);
        return queryHost(projectId, hostId);
    }

    @Override
    public DevopsHostVO queryHost(Long projectId, Long hostId) {
        return ConvertUtils.convertObject(devopsHostMapper.selectByPrimaryKey(hostId), DevopsHostVO.class);
    }

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
        result.setSshStatus(sshConnected ? DevopsHostStatus.SUCCESS.getValue() : DevopsHostStatus.FAILED.getValue());

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
