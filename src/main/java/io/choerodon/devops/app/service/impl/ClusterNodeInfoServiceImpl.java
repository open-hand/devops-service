package io.choerodon.devops.app.service.impl;

import com.alibaba.fastjson.JSONObject;
import io.choerodon.core.domain.Page;
import io.choerodon.core.domain.PageInfo;
import io.choerodon.devops.api.dto.ClusterNodeInfoDTO;
import io.choerodon.devops.app.service.ClusterNodeInfoService;
import io.choerodon.devops.domain.application.repository.DevopsClusterRepository;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zmf
 */
@Service
public class ClusterNodeInfoServiceImpl implements ClusterNodeInfoService {
    private static final String REDIS_CLUSTER_KEY_TEMPLATE = "node_info_org_id_%s_cluster_id_%s";
    private static final String CPU_MEASURE_FORMAT = "%.3f";

    @Autowired
    private DevopsClusterRepository devopsClusterRepository;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public String getRedisClusterKey(Long clusterId) {
        return getRedisClusterKey(clusterId, devopsClusterRepository.query(clusterId).getOrganizationId());
    }

    @Override
    public String getRedisClusterKey(Long clusterId, Long organizationId) {
        return String.format(REDIS_CLUSTER_KEY_TEMPLATE, organizationId, clusterId);
    }

    @Override
    public void setValueForKey(String redisClusterKey, List<ClusterNodeInfoDTO> clusterNodeInfoDTOList) {
        stringRedisTemplate.delete(redisClusterKey);
        stringRedisTemplate.opsForList().rightPushAll(redisClusterKey, clusterNodeInfoDTOList.stream().map(node -> {
            node.setCpuAllocatable(dealCpuMeasure(node.getCpuAllocatable()));
            node.setCpuCapacity(dealCpuMeasure(node.getCpuCapacity()));
            node.setCpuLimit(dealCpuMeasure(node.getCpuLimit()));
            node.setCpuRequest(dealCpuMeasure(node.getCpuRequest()));
            return JSONObject.toJSONString(node);
        }).collect(Collectors.toList()));
    }

    private String dealCpuMeasure(String cpuAmount) {
        if (cpuAmount.endsWith("m")) {
            double amount = Long.parseLong(cpuAmount.substring(0, cpuAmount.length() - 1)) / 1000.0;
            return String.format(CPU_MEASURE_FORMAT, amount);
        }
        return cpuAmount;
    }

    @Override
    public Page<ClusterNodeInfoDTO> pageQueryClusterNodeInfo(Long clusterId, Long organizationId, PageRequest pageRequest) {
        long start = (long) pageRequest.getPage() * (long) pageRequest.getSize();
        long stop = start + (long) pageRequest.getSize() - 1;
        String redisKey = getRedisClusterKey(clusterId, organizationId);

        long total = stringRedisTemplate.opsForList().size(redisKey);
        List<ClusterNodeInfoDTO> nodes = stringRedisTemplate
                .opsForList()
                .range(redisKey, start, stop)
                .stream()
                .map(node -> JSONObject.parseObject(node, ClusterNodeInfoDTO.class))
                .collect(Collectors.toList());

        return new Page<>(nodes, new PageInfo(pageRequest.getPage(), pageRequest.getSize()), total);
    }

    @Override
    public ClusterNodeInfoDTO getNodeInfo(Long organizationId, Long clusterId, String nodeName) {
        if (StringUtils.isEmpty(nodeName)) {
            return null;
        }

        String redisKey = getRedisClusterKey(clusterId, organizationId);
        long total = stringRedisTemplate.opsForList().size(redisKey);

        // get all nodes according to the cluster id and filter the node with the certain name
        return stringRedisTemplate
                .opsForList()
                .range(redisKey, 0, total - 1)
                .stream()
                .map(node -> JSONObject.parseObject(node, ClusterNodeInfoDTO.class))
                .filter(node -> nodeName.equals(node.getNodeName()))
                .findFirst()
                .orElse(null);
    }
}
