package io.choerodon.devops.app.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.devops.app.service.DevopsHostCommandService;
import io.choerodon.devops.infra.constant.DevopsHostConstants;
import io.choerodon.devops.infra.dto.DevopsHostCommandDTO;
import io.choerodon.devops.infra.mapper.DevopsHostCommandMapper;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/6/28 14:45
 */
@Service
public class DevopsHostCommandServiceImpl implements DevopsHostCommandService {

    private static final String ERROR_SAVE_HOST_COMMAND_FAILED = "error.save.host.command.failed";
    private static final String ERROR_UPDATE_HOST_COMMAND_FAILED = "error.update.host.command.failed";

    @Autowired
    private DevopsHostCommandMapper devopsHostCommandMapper;

    @Override
    @Transactional
    public void baseCreate(DevopsHostCommandDTO devopsHostCommandDTO) {
        MapperUtil.resultJudgedInsertSelective(devopsHostCommandMapper, devopsHostCommandDTO, ERROR_SAVE_HOST_COMMAND_FAILED);
    }

    @Override
    public DevopsHostCommandDTO baseQueryById(Long commandId) {
        return devopsHostCommandMapper.selectByPrimaryKey(commandId);
    }

    @Override
    @Transactional
    public void baseUpdate(DevopsHostCommandDTO devopsHostCommandDTO) {
        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsHostCommandMapper, devopsHostCommandDTO, ERROR_UPDATE_HOST_COMMAND_FAILED);
    }

    @Override
    public DevopsHostCommandDTO queryInstanceLatest(Long instanceId) {
        return devopsHostCommandMapper.queryInstanceLatest(instanceId);
    }

    @Override
    public List<DevopsHostCommandDTO> listStagnatedRecord(String hostId) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DevopsHostConstants.DATE_PATTERN);

        // 获取三分钟以前的时间
        Date threeMinutesBefore = new Date(System.currentTimeMillis() - DevopsHostConstants.THREE_MINUTE_MILLISECONDS);
        String beforeDate = simpleDateFormat.format(threeMinutesBefore);
        return devopsHostCommandMapper.listStagnatedRecord(hostId, beforeDate);
    }

    @Override
    @Transactional
    public void batchUpdateTimeoutCommand(Set<Long> missCommands) {
        devopsHostCommandMapper.batchUpdateTimeoutCommand(missCommands);
    }
}
