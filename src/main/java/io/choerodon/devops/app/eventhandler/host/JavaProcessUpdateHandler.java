package io.choerodon.devops.app.eventhandler.host;

import io.choerodon.devops.api.vo.host.JavaProcessInfoVO;
import io.choerodon.devops.api.vo.host.JavaProcessUpdatePayload;
import io.choerodon.devops.app.service.DevopsJavaInstanceService;
import io.choerodon.devops.infra.dto.DevopsJavaInstanceDTO;
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
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private DevopsJavaInstanceService devopsJavaInstanceService;

    @Override
    @Transactional
    public void handler(String hostId, Long commandId, String payload) {


        JavaProcessUpdatePayload javaProcessUpdatePayload = JsonHelper.unmarshalByJackson(payload, JavaProcessUpdatePayload.class);
        List<DevopsJavaInstanceDTO> devopsJavaInstanceDTOList = devopsJavaInstanceService.listByHostId(Long.valueOf(hostId));
        if (CollectionUtils.isEmpty(devopsJavaInstanceDTOList)) {
            return;
        }
        Map<Long, DevopsJavaInstanceDTO> devopsJavaInstanceDTOMap = devopsJavaInstanceDTOList.stream().collect(Collectors.toMap(DevopsJavaInstanceDTO::getId, Function.identity()));

        // 处理更新的数据
        List<JavaProcessInfoVO> deleteProcessInfos = javaProcessUpdatePayload.getUpdateProcessInfos();
        if (!CollectionUtils.isEmpty(deleteProcessInfos)) {
            deleteProcessInfos.forEach(deleteProcessInfo -> {
                DevopsJavaInstanceDTO devopsJavaInstanceDTO = devopsJavaInstanceDTOMap.get(deleteProcessInfo.getInstanceId());
                devopsJavaInstanceDTO.setStatus(deleteProcessInfo.getStatus());
                devopsJavaInstanceService.baseUpdate(devopsJavaInstanceDTO);
            });
        }

    }

    @Override
    public String getType() {
        return HostMsgEventEnum.JAVA_PROCESS_UPDATE.value();
    }
}
