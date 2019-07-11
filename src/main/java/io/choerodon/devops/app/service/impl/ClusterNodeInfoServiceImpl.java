package io.choerodon.devops.app.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.AgentNodeInfoDTO;
import io.choerodon.devops.api.vo.ClusterNodeInfoDTO;
import io.choerodon.devops.app.service.ClusterNodeInfoService;
import io.choerodon.devops.domain.application.entity.DevopsClusterE;
import io.choerodon.devops.domain.application.repository.DevopsClusterRepository;
import io.choerodon.devops.infra.util.TypeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * @author zmf
 */
@Service
public class ClusterNodeInfoServiceImpl implements ClusterNodeInfoService {
    private static final String REDIS_CLUSTER_KEY_TEMPLATE = "node_info_org_id_%s_cluster_id_%s";
    private static final String CPU_MEASURE_FORMAT = "%.3f";
    private static final String MEMORY_MEASURE_FORMAT = "%.3f%s";
    private static final String[] MEMORY_MEASURE = {"Ki", "Ki", "Mi", "Gi"};
    private static final String PERCENTAGE_FORMAT = "%.2f%%";
    /**
     * 如果出现时间的解析失误，可能是并发问题，不建议作为局部变量，可以考虑使用Joda-Time库，
     * 目前考虑Agent发送消息的间隔不会产生并发问题，当前日期(201901018)
     */
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterNodeInfoServiceImpl.class);

    @Autowired
    private DevopsClusterRepository devopsClusterRepository;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public String getRedisClusterKey(Long clusterId) {
        DevopsClusterE devopsClusterE = devopsClusterRepository.query(clusterId);
        if (devopsClusterE != null) {
            return getRedisClusterKey(clusterId, devopsClusterE.getOrganizationId());
        } else {
            throw new CommonException("error.cluster.get");
        }
    }

    @Override
    public String getRedisClusterKey(Long clusterId, Long organizationId) {
        return String.format(REDIS_CLUSTER_KEY_TEMPLATE, organizationId, clusterId);
    }

    @Override
    public void setValueForKey(String redisClusterKey, List<AgentNodeInfoDTO> agentNodeInfoDTOS) {
        stringRedisTemplate.delete(redisClusterKey);
        stringRedisTemplate.opsForList().rightPushAll(redisClusterKey, agentNodeInfoDTOS.stream().map(this::node2JsonString).collect(Collectors.toList()));
    }

    private String toNormalCpuValue(String cpuAmount) {
        if (cpuAmount.endsWith("m")) {
            double amount = Long.parseLong(cpuAmount.substring(0, cpuAmount.length() - 1)) / 1000.0;
            return String.format(CPU_MEASURE_FORMAT, amount);
        }
        return cpuAmount;
    }

    private void setCpuPercentage(ClusterNodeInfoDTO node) {
        double total = Double.parseDouble(node.getCpuTotal());
        double limit = Double.parseDouble(node.getCpuLimit());
        double request = Double.parseDouble(node.getCpuRequest());
        node.setCpuLimitPercentage(String.format(PERCENTAGE_FORMAT, limit / total * 100));
        node.setCpuRequestPercentage(String.format(PERCENTAGE_FORMAT, request / total * 100));
    }

    /**
     * deal with the raw node information from the agent
     * Don't change the execution order unless you know about what you do.
     *
     * @param raw the node information
     * @return the json string of the node information
     */
    private String node2JsonString(AgentNodeInfoDTO raw) {
        ClusterNodeInfoDTO node = new ClusterNodeInfoDTO();
        BeanUtils.copyProperties(raw, node);
        node.setCpuLimit(toNormalCpuValue(node.getCpuLimit()));
        node.setCpuRequest(toNormalCpuValue(node.getCpuRequest()));
        node.setCpuTotal(toNormalCpuValue(StringUtils.isEmpty(raw.getCpuAllocatable()) ? raw.getCpuCapacity() : raw.getCpuAllocatable()));
        node.setPodTotal(Long.parseLong(StringUtils.isEmpty(raw.getPodAllocatable()) ? raw.getPodCapacity() : raw.getPodAllocatable()));
        node.setMemoryTotal(StringUtils.isEmpty(raw.getMemoryAllocatable()) ? raw.getMemoryCapacity() : raw.getMemoryAllocatable());

        setMemoryInfo(node);

        node.setPodPercentage(String.format(PERCENTAGE_FORMAT, node.getPodCount() * 1.0 / node.getPodTotal() * 100));

        setCpuPercentage(node);

        try {
            node.setCreateTime(simpleDateFormat.format(simpleDateFormat.parse(raw.getCreateTime())));
        } catch (ParseException e) {
            LOGGER.info("date: {} failed to be formatted", raw.getCreateTime());
        }
        return JSONObject.toJSONString(node);
    }

    /**
     * set the values for memory
     *
     * @param node the node information
     */
    private void setMemoryInfo(ClusterNodeInfoDTO node) {
        double total = ((Long) getByteOfMemory(node.getMemoryTotal())).doubleValue();
        long request = getByteOfMemory(node.getMemoryRequest());
        long limit = getByteOfMemory(node.getMemoryLimit());
        node.setMemoryLimitPercentage(String.format(PERCENTAGE_FORMAT, limit / total * 100));
        node.setMemoryRequestPercentage(String.format(PERCENTAGE_FORMAT, request / total * 100));

        node.setMemoryTotal(dealWithMemoryMeasure(total));
        node.setMemoryRequest(dealWithMemoryMeasure(request));
        node.setMemoryLimit(dealWithMemoryMeasure(limit));
    }

    /**
     * get byte value from memory string of other measure format
     *
     * @param memory the memory string
     * @return byte value
     */
    private long getByteOfMemory(String memory) {
        int index;
        if ((index = memory.indexOf('K')) != -1) {
            return Long.parseLong(memory.substring(0, index)) << 10;
        } else if ((index = memory.indexOf('M')) != -1) {
            return Long.parseLong(memory.substring(0, index)) << 20;
        } else if ((index = memory.indexOf('G')) != -1) {
            return Long.parseLong(memory.substring(0, index)) << 30;
        } else if (memory.matches("^\\d+$")) {
            return Long.parseLong(memory);
        } else if ((index = memory.indexOf('m')) != -1) {
            return Long.parseLong(memory.substring(0, index)) / 1000;
        } else {
            return 0;
        }
    }

    /**
     * from byte to M or G
     *
     * @param memory the memory string
     * @return the memory string
     */
    private String dealWithMemoryMeasure(final double memory) {
        double value = memory;
        int count = 0;
        while (value >= 1024 && count < MEMORY_MEASURE.length - 1) {
            value /= 1024;
            count++;
        }

        if (count == 0) {
            value /= 1024;
        }
        return String.format(MEMORY_MEASURE_FORMAT, value, MEMORY_MEASURE[count]);
    }

    @Override
    public PageInfo<ClusterNodeInfoDTO> pageQueryClusterNodeInfo(Long clusterId, Long organizationId, PageRequest pageRequest) {
        long start = (long) (pageRequest.getPage() - 1) * (long) pageRequest.getSize();
        long stop = start + (long) pageRequest.getSize() - 1;
        String redisKey = getRedisClusterKey(clusterId, organizationId);

        long total = stringRedisTemplate.opsForList().size(redisKey);
        List<ClusterNodeInfoDTO> nodes = stringRedisTemplate
                .opsForList()
                .range(redisKey, start, stop)
                .stream()
                .map(node -> JSONObject.parseObject(node, ClusterNodeInfoDTO.class))
                .collect(Collectors.toList());
        PageInfo<ClusterNodeInfoDTO> result = new PageInfo();
        if (total < pageRequest.getSize() * pageRequest.getPage()) {
            result.setSize(TypeUtil.objToInt(total) - (pageRequest.getSize() * (pageRequest.getPage() - 1)));
        } else {
            result.setSize(pageRequest.getSize());
        }
        result.setPageSize(pageRequest.getSize());
        result.setPageNum(pageRequest.getPage());
        result.setTotal(total);
        result.setList(nodes);
        return result;
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
