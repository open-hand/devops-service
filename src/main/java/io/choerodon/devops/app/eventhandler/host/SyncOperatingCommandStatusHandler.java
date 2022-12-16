package io.choerodon.devops.app.eventhandler.host;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.devops.api.vo.host.CommandResultVO;
import io.choerodon.devops.app.service.DevopsHostCommandService;
import io.choerodon.devops.infra.enums.host.HostMsgEventEnum;
import io.choerodon.devops.infra.util.JsonHelper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/7/23 15:23
 */
@Component
public class SyncOperatingCommandStatusHandler implements HostMsgHandler {

    @Autowired
    private DevopsHostCommandService devopsHostCommandService;

    @Autowired
    private CommandResultHandler commandResultHandler;

//    @Autowired
//    private DevopsCdPipelineService devopsCdPipelineService;

    @Override
    @Transactional
    public void handler(String hostId, Long commandId, String payload) {
        List<CommandResultVO> commandResultVOS = JsonHelper.unmarshalByJackson(payload, new TypeReference<List<CommandResultVO>>() {
        });
        // 1. 将所有agent丢失的命令状态改为超时
        Set<Long> missCommands = commandResultVOS.stream().filter(CommandResultVO::getNotExist).map(CommandResultVO::getCommandId).collect(Collectors.toSet());
        if (!CollectionUtils.isEmpty(missCommands)) {
            devopsHostCommandService.batchUpdateTimeoutCommand(missCommands);
            // 更新流水线的状态
//            List<DevopsHostCommandDTO> devopsHostCommandDTOS = devopsHostCommandService.listByIds(missCommands);
//            devopsHostCommandDTOS.forEach(devopsHostCommandDTO ->
//            {
//                if (devopsHostCommandDTO.getCdJobRecordId() != null) {
//                    devopsCdPipelineService.hostDeployStatusUpdate(devopsHostCommandDTO.getId(), devopsHostCommandDTO.getCdJobRecordId(), false, "timeout");
//                }
//            });
        }
        // 2. 同步devops丢失的命令
        List<CommandResultVO> unSyncCommands = commandResultVOS.stream().filter(v -> Boolean.FALSE.equals(v.getNotExist())).collect(Collectors.toList());
        unSyncCommands.forEach(unSyncCommand -> commandResultHandler.handler(hostId, unSyncCommand.getCommandId(), unSyncCommand));
    }

    @Override
    public String getType() {
        return HostMsgEventEnum.SYNC_OPERATING_COMMAND_STATUS.value();
    }
}
