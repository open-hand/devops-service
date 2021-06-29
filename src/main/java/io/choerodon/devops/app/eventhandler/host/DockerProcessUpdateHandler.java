package io.choerodon.devops.app.eventhandler.host;

import io.choerodon.devops.api.vo.host.DockerProcessInfoVO;
import io.choerodon.devops.api.vo.host.DockerProcessUpdatePayload;
import io.choerodon.devops.infra.constant.DevopsHostConstants;
import io.choerodon.devops.infra.enums.host.HostMsgEventEnum;
import io.choerodon.devops.infra.util.JsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/6/27 20:35
 */
@Component
public class DockerProcessUpdateHandler implements HostMsgHandler {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void handler(Long hostId, Long commandId, String payload) {
        Map<Object, Object> processInfoMap = redisTemplate.opsForHash().entries(String.format(DevopsHostConstants.HOST_DOCKER_PROCESS_INFO_KEY, hostId));

        DockerProcessUpdatePayload dockerProcessUpdatePayload = JsonHelper.unmarshalByJackson(payload, DockerProcessUpdatePayload.class);
        // 处理删除的数据
        List<DockerProcessInfoVO> deleteProcessInfos = dockerProcessUpdatePayload.getDeleteProcessInfos();
        if (!CollectionUtils.isEmpty(deleteProcessInfos)) {
            deleteProcessInfos.forEach(deleteProcessInfo -> {
                processInfoMap.remove(deleteProcessInfo.getContainerId());
            });
        }

        // 处理更新的数据
        List<DockerProcessInfoVO> updateProcessInfos = dockerProcessUpdatePayload.getUpdateProcessInfos();
        updateProcessInfos.forEach(addProcessInfo -> {
            // todo 完善部署者、部署时间信息

            processInfoMap.put(addProcessInfo.getContainerId(), addProcessInfo);
        });

        // 更新缓存
        redisTemplate.opsForHash().putAll(String.format(DevopsHostConstants.HOST_DOCKER_PROCESS_INFO_KEY, hostId), processInfoMap);

    }

    @Override
    public String getType() {
        return HostMsgEventEnum.DOCKER_PROCESS_UPDATE.value();
    }
}
