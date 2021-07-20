package io.choerodon.devops.app.eventhandler.host;

import io.choerodon.devops.api.vo.host.JavaProcessInfoVO;
import io.choerodon.devops.api.vo.host.JavaProcessUpdatePayload;
import io.choerodon.devops.app.service.DevopsNormalInstanceService;
import io.choerodon.devops.infra.dto.DevopsNormalInstanceDTO;
import io.choerodon.devops.infra.enums.host.HostMsgEventEnum;
import io.choerodon.devops.infra.util.JsonHelper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private DevopsNormalInstanceService devopsNormalInstanceService;

    @Override
    @Transactional
    public void handler(String hostId, Long commandId, String payload) {


        JavaProcessUpdatePayload javaProcessUpdatePayload = JsonHelper.unmarshalByJackson(payload, JavaProcessUpdatePayload.class);
        List<DevopsNormalInstanceDTO> devopsNormalInstanceDTOList = devopsNormalInstanceService.listByHostId(Long.valueOf(hostId));
        if (CollectionUtils.isEmpty(devopsNormalInstanceDTOList)) {
            return;
        }
        Map<Long, DevopsNormalInstanceDTO> devopsJavaInstanceDTOMap = devopsNormalInstanceDTOList.stream().collect(Collectors.toMap(DevopsNormalInstanceDTO::getId, Function.identity()));

        // 处理更新的数据
        List<JavaProcessInfoVO> updateProcessInfos = javaProcessUpdatePayload.getUpdateProcessInfos();
        if (!CollectionUtils.isEmpty(updateProcessInfos)) {
            updateProcessInfos.forEach(updateProcessInfo -> {
                DevopsNormalInstanceDTO devopsNormalInstanceDTO = devopsJavaInstanceDTOMap.get(Long.valueOf(updateProcessInfo.getInstanceId()));
                if (devopsNormalInstanceDTO != null) {
                    devopsNormalInstanceDTO.setStatus(updateProcessInfo.getStatus());
                    devopsNormalInstanceDTO.setPort(updateProcessInfo.getPort());
                    devopsNormalInstanceService.baseUpdate(devopsNormalInstanceDTO);
                }
            });
        }

    }

    @Override
    public String getType() {
        return HostMsgEventEnum.JAVA_PROCESS_UPDATE.value();
    }
}
