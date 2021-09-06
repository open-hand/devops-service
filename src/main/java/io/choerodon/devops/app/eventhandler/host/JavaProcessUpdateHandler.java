package io.choerodon.devops.app.eventhandler.host;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.devops.api.vo.host.JavaProcessInfoVO;
import io.choerodon.devops.api.vo.host.JavaProcessUpdatePayload;
import io.choerodon.devops.app.service.DevopsHostAppInstanceService;
import io.choerodon.devops.infra.dto.DevopsHostAppInstanceDTO;
import io.choerodon.devops.infra.enums.host.HostMsgEventEnum;
import io.choerodon.devops.infra.util.JsonHelper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/6/25 14:25
 */
@Component
public class JavaProcessUpdateHandler implements HostMsgHandler {

    @Autowired
    private DevopsHostAppInstanceService devopsHostAppInstanceService;

    @Override
    @Transactional
    public void handler(String hostId, Long commandId, String payload) {


        JavaProcessUpdatePayload javaProcessUpdatePayload = JsonHelper.unmarshalByJackson(payload, JavaProcessUpdatePayload.class);
        List<DevopsHostAppInstanceDTO> devopsHostAppInstanceDTOS = devopsHostAppInstanceService.listByHostId(Long.valueOf(hostId));
        if (CollectionUtils.isEmpty(devopsHostAppInstanceDTOS)) {
            return;
        }
        Map<Long, DevopsHostAppInstanceDTO> devopsJavaInstanceDTOMap = devopsHostAppInstanceDTOS.stream().collect(Collectors.toMap(DevopsHostAppInstanceDTO::getId, Function.identity()));

        // 处理更新的数据
        List<JavaProcessInfoVO> updateProcessInfos = javaProcessUpdatePayload.getUpdateProcessInfos();
        if (!CollectionUtils.isEmpty(updateProcessInfos)) {
            updateProcessInfos.forEach(updateProcessInfo -> {
                DevopsHostAppInstanceDTO devopsHostAppInstanceDTO = devopsJavaInstanceDTOMap.get(Long.valueOf(updateProcessInfo.getInstanceId()));
                if (devopsHostAppInstanceDTO != null) {
                    devopsHostAppInstanceDTO.setStatus(updateProcessInfo.getStatus());
                    devopsHostAppInstanceService.baseUpdate(devopsHostAppInstanceDTO);
                }
            });
        }

    }

    @Override
    public String getType() {
        return HostMsgEventEnum.JAVA_PROCESS_UPDATE.value();
    }
}
