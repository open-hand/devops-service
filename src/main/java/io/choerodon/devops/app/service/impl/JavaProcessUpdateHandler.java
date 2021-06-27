package io.choerodon.devops.app.service.impl;

import io.choerodon.devops.api.vo.host.JavaProcessInfoVO;
import io.choerodon.devops.api.vo.host.JavaProcessUpdatePayload;
import io.choerodon.devops.app.service.HostMsgHandler;
import io.choerodon.devops.infra.constant.DevopsHostConstants;
import io.choerodon.devops.infra.enums.HostMsgEventEnum;
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
 * @Date 2021/6/25 14:25
 */
@Component
public class JavaProcessUpdateHandler implements HostMsgHandler {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void handler(Long hostId, Long commandId, String payload) {

        Map<Object, Object> processInfoMap = redisTemplate.opsForHash().entries(String.format(DevopsHostConstants.HOST_JAVA_PROCESS_INFO_KEY, hostId));

        JavaProcessUpdatePayload javaProcessUpdatePayload = JsonHelper.unmarshalByJackson(payload, JavaProcessUpdatePayload.class);
        // 处理删除的数据
        List<JavaProcessInfoVO> deleteProcessInfos = javaProcessUpdatePayload.getDeleteProcessInfos();
        if (!CollectionUtils.isEmpty(deleteProcessInfos)) {
            deleteProcessInfos.forEach(deleteProcessInfo -> {
                processInfoMap.remove(deleteProcessInfo.getPid());
            });
        }

        // 处理新增的数据
        List<JavaProcessInfoVO> addProcessInfos = javaProcessUpdatePayload.getAddProcessInfos();
        addProcessInfos.forEach(addProcessInfo -> {
            processInfoMap.put(addProcessInfo.getPid(), addProcessInfo);
        });

        // 更新缓存
        redisTemplate.opsForHash().putAll(String.format(DevopsHostConstants.HOST_JAVA_PROCESS_INFO_KEY, hostId), processInfoMap);

    }

    @Override
    public String getType() {
        return HostMsgEventEnum.JAVA_PROCESS_UPDATE.value();
    }
}
