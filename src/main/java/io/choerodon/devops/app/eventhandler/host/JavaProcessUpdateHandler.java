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
import io.choerodon.devops.app.service.DevopsHostAppService;
import io.choerodon.devops.infra.dto.DevopsHostAppDTO;
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
    private DevopsHostAppService devopsHostAppService;

    @Override
    @Transactional
    public void handler(String hostId, Long commandId, String payload) {


        JavaProcessUpdatePayload javaProcessUpdatePayload = JsonHelper.unmarshalByJackson(payload, JavaProcessUpdatePayload.class);
        List<DevopsHostAppDTO> devopsHostAppDTOList = devopsHostAppService.listByHostId(Long.valueOf(hostId));
        if (CollectionUtils.isEmpty(devopsHostAppDTOList)) {
            return;
        }
        Map<Long, DevopsHostAppDTO> devopsJavaInstanceDTOMap = devopsHostAppDTOList.stream().collect(Collectors.toMap(DevopsHostAppDTO::getId, Function.identity()));

        // 处理更新的数据
        List<JavaProcessInfoVO> updateProcessInfos = javaProcessUpdatePayload.getUpdateProcessInfos();
        if (!CollectionUtils.isEmpty(updateProcessInfos)) {
            updateProcessInfos.forEach(updateProcessInfo -> {
                DevopsHostAppDTO devopsHostAppDTO = devopsJavaInstanceDTOMap.get(Long.valueOf(updateProcessInfo.getInstanceId()));
                if (devopsHostAppDTO != null) {
                    devopsHostAppDTO.setStatus(updateProcessInfo.getStatus());
                    // todo
//                    devopsHostAppDTO.setPorts(updateProcessInfo.getPorts());
                    devopsHostAppService.baseUpdate(devopsHostAppDTO);
                }
            });
        }

    }

    @Override
    public String getType() {
        return HostMsgEventEnum.JAVA_PROCESS_UPDATE.value();
    }
}
