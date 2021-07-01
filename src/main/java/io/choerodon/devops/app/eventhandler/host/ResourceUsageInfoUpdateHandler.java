package io.choerodon.devops.app.eventhandler.host;

import io.choerodon.devops.api.vo.host.ResourceUsageInfoVO;
import io.choerodon.devops.infra.constant.DevopsHostConstants;
import io.choerodon.devops.infra.enums.host.HostMsgEventEnum;
import io.choerodon.devops.infra.util.ConvertUtils;
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
        ResourceUsageInfoVO resourceUsageInfoVO = ConvertUtils.convertObject(payload, ResourceUsageInfoVO.class);
        redisTemplate.opsForValue().set(String.format(DevopsHostConstants.HOST_RESOURCE_INFO_KEY, hostId), resourceUsageInfoVO);
    }

    @Override
    public String getType() {
        return HostMsgEventEnum.RESOURCE_USAGE_INFO_UPDATE.value();
    }
}
