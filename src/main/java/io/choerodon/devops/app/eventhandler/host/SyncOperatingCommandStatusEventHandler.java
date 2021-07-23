package io.choerodon.devops.app.eventhandler.host;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import org.hzero.websocket.helper.KeySocketSendHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import io.choerodon.devops.api.vo.host.HostAgentMsgVO;
import io.choerodon.devops.app.service.DevopsHostCommandService;
import io.choerodon.devops.infra.constant.DevopsHostConstants;
import io.choerodon.devops.infra.dto.DevopsHostCommandDTO;
import io.choerodon.devops.infra.enums.host.HostCommandEnum;
import io.choerodon.devops.infra.enums.host.HostMsgEventEnum;
import io.choerodon.devops.infra.util.JsonHelper;

/**
 * 〈功能简述〉
 * 〈接受agent同步操作状态的消息，查询处于操作中三分钟以上的操作记录，发送给agent〉
 *
 * @author wanghao
 * @since 2021/7/23 14:35
 */
@Component
public class SyncOperatingCommandStatusEventHandler implements HostMsgHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SyncOperatingCommandStatusEventHandler.class);

    @Autowired
    private DevopsHostCommandService devopsHostCommandService;
    @Autowired
    private KeySocketSendHelper webSocketHelper;

    @Override
    public void handler(String hostId, @Nullable Long commandId, String payload) {
        // 1. 查询处于操作中三分钟以上的操作记录
        List<DevopsHostCommandDTO> devopsHostCommandDTOList = devopsHostCommandService.listStagnatedRecord(hostId);
        if (CollectionUtils.isEmpty(devopsHostCommandDTOList)) {
            LOGGER.info(">>>>>>>>>>>>>>>Stagnated operating host command is null, skip.<<<<<<<<<<<<<<<<<<");
            return;
        }
        Set<String> commandIds = devopsHostCommandDTOList.stream().map(v -> v.getId().toString()).collect(Collectors.toSet());
        HostAgentMsgVO hostAgentMsgVO = new HostAgentMsgVO();
        hostAgentMsgVO.setHostId(String.valueOf(hostId));
        hostAgentMsgVO.setType(HostCommandEnum.SYNC_OPERATING_COMMAND_STATUS.value());
        hostAgentMsgVO.setPayload(JsonHelper.marshalByJackson(commandIds));
        LOGGER.info(">>>>>>>>>>>>>>>Sync operating host command, payload is {}.<<<<<<<<<<<<<<<<<<", hostAgentMsgVO.getPayload());
        // 2. 将记录发送给agent
        webSocketHelper.sendByGroup(DevopsHostConstants.GROUP + hostId,
                String.format(DevopsHostConstants.HOST_COMMANDS, hostId),
                JsonHelper.marshalByJackson(hostAgentMsgVO));
    }

    @Override
    public String getType() {
        return HostMsgEventEnum.SYNC_OPERATING_COMMAND_STATUS_EVENT.value();
    }
}
