package io.choerodon.devops.app.service.impl;

import io.choerodon.devops.api.vo.host.DockerProcessInfoVO;
import io.choerodon.devops.api.vo.host.InitInfoVO;
import io.choerodon.devops.api.vo.host.JavaProcessInfoVO;
import io.choerodon.devops.app.service.HostMsgHandler;
import io.choerodon.devops.infra.constant.DevopsHostConstants;
import io.choerodon.devops.infra.enums.host.HostMsgEventEnum;
import io.choerodon.devops.infra.util.JsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 〈功能简述〉
 * 〈agent 启动事件，重新初始化缓存〉
 *
 * @author wanghao
 * @Date 2021/6/27 21:03
 */
@Component
public class InitHandler implements HostMsgHandler {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void handler(Long hostId, Long commandId, String payload) {

        InitInfoVO initInfoVO = JsonHelper.unmarshalByJackson(payload, InitInfoVO.class);

        // 初始化java进程
        List<JavaProcessInfoVO> javaProcessInfos = initInfoVO.getJavaProcessInfos();
        if (!CollectionUtils.isEmpty(javaProcessInfos)) {
            Map<Object, Object> processInfoMap = javaProcessInfos.stream().collect(Collectors.toMap(JavaProcessInfoVO::getPid, Function.identity()));
            redisTemplate.opsForHash().putAll(String.format(DevopsHostConstants.HOST_JAVA_PROCESS_INFO_KEY, hostId), processInfoMap);
        }
        // 初始化docker进程
        List<DockerProcessInfoVO> dockerProcessInfos = initInfoVO.getDockerProcessInfos();
        if (!CollectionUtils.isEmpty(dockerProcessInfos)) {
            Map<Object, Object> processInfoMap = dockerProcessInfos.stream().collect(Collectors.toMap(DockerProcessInfoVO::getContainerId, Function.identity()));
            redisTemplate.opsForHash().putAll(String.format(DevopsHostConstants.HOST_DOCKER_PROCESS_INFO_KEY, hostId), processInfoMap);
        }
        // todo 初始化资源使用率

    }

    @Override
    public String getType() {
        return HostMsgEventEnum.INIT.value();
    }
}
