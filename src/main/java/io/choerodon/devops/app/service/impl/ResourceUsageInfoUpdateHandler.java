package io.choerodon.devops.app.service.impl;

import io.choerodon.devops.app.service.HostMsgHandler;
import io.choerodon.devops.infra.enums.host.HostMsgEventEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/6/27 20:49
 */
@Component
public class ResourceUsageInfoUpdateHandler implements HostMsgHandler {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void handler(Long hostId, Long commandId, String payload) {

    }

    @Override
    public String getType() {
        return HostMsgEventEnum.RESOURCE_USAGE_INFO_UPDATE.value();
    }
}
