package io.choerodon.devops.app.service.impl;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import io.choerodon.devops.api.vo.AgentPodInfoVO;
import io.choerodon.devops.app.service.AgentPodService;

/**
 * @author zmf
 */
@Service
public class AgentPodInfoServiceImpl implements AgentPodService {
    /**
     * pod--${podName}--${namespace}
     */
    private static final String KEY_PATTERN = "pod--%s--%s";
    /**
     * 在redis中存的实时数据的最大数量
     */
    private static final long RECORD_SIZE = 30;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void handleRealTimePodData(List<AgentPodInfoVO> pods) {
        Date snapshotTime = new Date();
        pods.forEach(pod -> {
            pod.setSnapshotTime(snapshotTime);
            String key = String.format(KEY_PATTERN, pod.getPodName(), pod.getNamespace());
            Long size = stringRedisTemplate.opsForList().size(key);
            if (size >= 20) {
                long stop = size - 1;
                long start = stop - (RECORD_SIZE - 1) + 1;
                stringRedisTemplate.opsForList().trim(key, start, stop);
            }
            stringRedisTemplate.opsForList().rightPush(key, JSON.toJSONString(pod));
        });
    }

    @Override
    public List<AgentPodInfoVO> queryAllPodSnapshots(String podName, String namespace) {
        return stringRedisTemplate.opsForList()
                .range(String.format(KEY_PATTERN, podName, namespace), 0, RECORD_SIZE - 1)
                .stream()
                .map(p -> JSON.parseObject(p, AgentPodInfoVO.class))
                .collect(Collectors.toList());
    }

    @Nullable
    @Override
    public AgentPodInfoVO queryLatestPodSnapshot(String podName, String namespace) {
        String key = String.format(KEY_PATTERN, podName, namespace);
        Long size = stringRedisTemplate.opsForList().size(key);
        return JSON.parseObject(stringRedisTemplate.opsForList().index(key, size - 1), AgentPodInfoVO.class);
    }
}
