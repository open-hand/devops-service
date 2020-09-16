package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.utils.ConvertUtils;
import io.choerodon.devops.api.validator.DevopsHostAdditionalCheckValidator;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.service.DevopsHostService;
import io.choerodon.devops.infra.constant.MiscConstants;
import io.choerodon.devops.infra.dto.DevopsHostDTO;
import io.choerodon.devops.infra.enums.DevopsHostStatus;
import io.choerodon.devops.infra.enums.DevopsHostType;
import io.choerodon.devops.infra.mapper.DevopsHostMapper;
import io.choerodon.devops.infra.util.CommonExAssertUtil;
import io.choerodon.devops.infra.util.JmeterUtil;
import io.choerodon.devops.infra.util.MapperUtil;
import io.choerodon.devops.infra.util.SshUtil;

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
        boolean jmeterConnected = JmeterUtil.testJmeterConnections(devopsHostConnectionTestVO.getHostIp(), devopsHostConnectionTestVO.getJmeterPort());
        result.setJmeterStatus(jmeterConnected ? DevopsHostStatus.SUCCESS.getValue() : DevopsHostStatus.FAILED.getValue());
        return result;
    }
}
