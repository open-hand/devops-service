package io.choerodon.devops.app.service.impl;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.AgentNodeInfoVO;
import io.choerodon.devops.api.vo.ClusterNodeInfoVO;
import io.choerodon.devops.app.service.ClusterNodeInfoService;
import io.choerodon.devops.app.service.DevopsClusterNodeService;
import io.choerodon.devops.app.service.DevopsClusterOperatingRecordService;
import io.choerodon.devops.app.service.DevopsClusterService;
import io.choerodon.devops.infra.constant.ClusterCheckConstant;
import io.choerodon.devops.infra.constant.ResourceCheckConstant;
import io.choerodon.devops.infra.dto.DevopsClusterDTO;
import io.choerodon.devops.infra.dto.DevopsClusterNodeDTO;
import io.choerodon.devops.infra.dto.DevopsClusterOperationRecordDTO;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.util.K8sUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * @author zmf
 */
@Service
public class ClusterNodeInfoServiceImpl implements ClusterNodeInfoService {
    private static final String REDIS_CLUSTER_KEY_TEMPLATE = "node_info_project_id_%s_cluster_id_%s";
    private static final String DEVOPS_CLUSTER_GET = "devops.cluster.get";
    private static final String CPU_MEASURE_FORMAT = "%.2f";
    private static final String MEMORY_MEASURE_FORMAT = "%.2f%s";
    private static final String[] MEMORY_MEASURE = {"Ki", "Ki", "Mi", "Gi"};
    private static final String PERCENTAGE_FORMAT = "%.2f%%";
    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterNodeInfoServiceImpl.class);

    @Autowired
    private DevopsClusterService devopsClusterService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private DevopsClusterNodeService devopsClusterNodeService;
    @Autowired
    private DevopsClusterOperatingRecordService devopsClusterOperatingRecordService;
    @Autowired
    private ClusterConnectionHandler clusterConnectionHandler;

    @Override
    public String getRedisClusterKey(Long clusterId) {
        DevopsClusterDTO devopsClusterDTO = devopsClusterService.baseQuery(clusterId);
        if (devopsClusterDTO != null) {
            return getRedisClusterKey(clusterId, devopsClusterDTO.getProjectId());
        } else {
            throw new CommonException(DEVOPS_CLUSTER_GET);
        }
    }

    @Override
    public String getRedisClusterKey(Long clusterId, Long projectId) {
        return String.format(REDIS_CLUSTER_KEY_TEMPLATE, projectId, clusterId);
    }

    @Override
    public void setValueForKey(String redisClusterKey, List<AgentNodeInfoVO> agentNodeInfoVOS) {
        stringRedisTemplate.delete(redisClusterKey);
        stringRedisTemplate.opsForList().rightPushAll(redisClusterKey, agentNodeInfoVOS.stream().map(this::node2JsonString).collect(Collectors.toList()));
    }

    private void setCpuPercentage(ClusterNodeInfoVO node) {
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
    private String node2JsonString(AgentNodeInfoVO raw) {
        ClusterNodeInfoVO node = new ClusterNodeInfoVO();
        // 如果节点没有pod，agent会给PodCount字段返回null
        if (raw.getPodCount() == null) {
            raw.setPodCount(0L);
        }

        BeanUtils.copyProperties(raw, node);
        node.setCpuLimit(String.format(CPU_MEASURE_FORMAT, K8sUtil.getNormalValueFromCpuString(node.getCpuLimit())));
        node.setCpuRequest(String.format(CPU_MEASURE_FORMAT, K8sUtil.getNormalValueFromCpuString(node.getCpuRequest())));
        node.setCpuTotal(String.format(CPU_MEASURE_FORMAT, K8sUtil.getNormalValueFromCpuString(ObjectUtils.isEmpty(raw.getCpuAllocatable()) ? raw.getCpuCapacity() : raw.getCpuAllocatable())));
        node.setPodTotal(ObjectUtils.isEmpty(raw.getPodAllocatable()) ? K8sUtil.getNormalValueFromPodString(raw.getPodCapacity()) : K8sUtil.getNormalValueFromPodString(raw.getPodAllocatable()));
        node.setMemoryTotal(ObjectUtils.isEmpty(raw.getMemoryAllocatable()) ? raw.getMemoryCapacity() : raw.getMemoryAllocatable());

        setMemoryInfo(node);

        node.setPodPercentage(String.format(PERCENTAGE_FORMAT, node.getPodCount() * 1.0 / node.getPodTotal() * 100));

        setCpuPercentage(node);

        try {
            // 截取掉时区信息, 维持时间格式统一
            // 2019-11-13 09:27:08 +0800 CST    变成     2019-11-13 09:27:08
            node.setCreateTime(raw.getCreateTime().substring(0, 19));
        } catch (Exception ex) {
            LOGGER.warn("Exception occurred when parsing creation time: {}", raw.getCreateTime());
        }
        return JSONObject.toJSONString(node);
    }

    /**
     * set the values for memory
     *
     * @param node the node information
     */
    private void setMemoryInfo(ClusterNodeInfoVO node) {
        double total = ((Long) K8sUtil.getByteFromMemoryString(node.getMemoryTotal())).doubleValue();
        long request = K8sUtil.getByteFromMemoryString(node.getMemoryRequest());
        long limit = K8sUtil.getByteFromMemoryString(node.getMemoryLimit());
        node.setMemoryLimitPercentage(String.format(PERCENTAGE_FORMAT, limit / total * 100));
        node.setMemoryRequestPercentage(String.format(PERCENTAGE_FORMAT, request / total * 100));

        node.setMemoryTotal(dealWithMemoryMeasure(total));
        node.setMemoryRequest(dealWithMemoryMeasure(request));
        node.setMemoryLimit(dealWithMemoryMeasure(limit));
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
    public Page<ClusterNodeInfoVO> pageClusterNodeInfo(Long clusterId, Long projectId, PageRequest pageable) {
        Assert.notNull(clusterId, ClusterCheckConstant.ERROR_CLUSTER_ID_IS_NULL);
        Assert.notNull(clusterId, ResourceCheckConstant.DEVOPS_PROJECT_ID_IS_NULL);


        Page<ClusterNodeInfoVO> result = new Page<>();
        DevopsClusterDTO devopsClusterDTO = devopsClusterService.baseQuery(clusterId);
        List<Long> updatedClusterList = clusterConnectionHandler.getUpdatedClusterList();
        String redisKey = getRedisClusterKey(clusterId, projectId);
        List<ClusterNodeInfoVO> nodes;


        if (ClusterTypeEnum.CREATED.value().equals(devopsClusterDTO.getType())) {
            Page<DevopsClusterNodeDTO> nodeDTOS = PageHelper.doPage(pageable, () -> devopsClusterNodeService.queryNodeByClusterIdAndType(clusterId, ClusterNodeTypeEnum.INNER));

            // 查询为node添加id需要的数据
            List<DevopsClusterNodeDTO> outerNodes = devopsClusterNodeService.queryNodeByClusterIdAndType(clusterId, ClusterNodeTypeEnum.OUTTER);
            Map<String, ClusterNodeInfoVO> redisNodeInfoMap = stringRedisTemplate
                    .opsForList()
                    .range(redisKey, 0, nodeDTOS.size())
                    .stream()
                    .map(node -> JSONObject.parseObject(node, ClusterNodeInfoVO.class))
                    .collect(Collectors.toMap(ClusterNodeInfoVO::getNodeName, v -> v));

            List<ClusterNodeInfoVO> nodeInfoVOS = nodeDTOS.stream().map(node -> {
                ClusterNodeInfoVO clusterNodeInfoVO = new ClusterNodeInfoVO();
                clusterNodeInfoVO.setId(node.getId());
                clusterNodeInfoVO.setClusterType(node.getType());
                clusterNodeInfoVO.setNodeName(node.getName());
                clusterNodeInfoVO.setStatus(ClusterStatusEnum.UNKNOWN.value());
                clusterNodeInfoVO.setRole(ClusterNodeRoleEnum.getRoleNamesByFlag(node.getRole()));
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                clusterNodeInfoVO.setCreateTime(simpleDateFormat.format(node.getCreationDate()));
                if (!CollectionUtils.isEmpty(outerNodes)
                        && node.getName().equals(outerNodes.get(0).getInnerNodeName())) {
                    clusterNodeInfoVO.setOuterNodeFlag(true);
                }
                // 添加能否删除节点角色标记
                if (ClusterNodeRoleEnum.isMaster(node.getRole())) {
                    clusterNodeInfoVO.setEnableDeleteMasterRole(true);
                }
                if (ClusterNodeRoleEnum.isEtcd(node.getRole())) {
                    clusterNodeInfoVO.setEnableDeleteEtcdRole(true);
                }
                if (ClusterNodeRoleEnum.WORKER.getMask() == node.getRole()
                        && !node.getName().equals(outerNodes.get(0).getInnerNodeName())) {
                    clusterNodeInfoVO.setEnableDeleteNode(true);
                }
                // 添加失败信息
                DevopsClusterOperationRecordDTO devopsClusterOperationRecordDTO = devopsClusterOperatingRecordService.queryLatestRecordByNodeId(node.getId());
                if (devopsClusterOperationRecordDTO != null
                        && ClusterOperationStatusEnum.FAILED.value().equals(devopsClusterOperationRecordDTO.getStatus())) {
                    clusterNodeInfoVO.setOperatingStatus(ClusterOperationStatusEnum.FAILED.value());
                    clusterNodeInfoVO.setErrorMsg(devopsClusterOperationRecordDTO.getErrorMsg());
                }
                // 如果集群已连接，添加cpu、memory相关信息
                if (updatedClusterList.contains(clusterId)) {
                    ClusterNodeInfoVO redisNodeInfo = redisNodeInfoMap.get(node.getName());
                    if (redisNodeInfo != null) {
                        clusterNodeInfoVO.setStatus(redisNodeInfo.getStatus());
                        clusterNodeInfoVO.setCpuLimit(redisNodeInfo.getCpuLimit());
                        clusterNodeInfoVO.setCpuLimitPercentage(redisNodeInfo.getCpuLimitPercentage());
                        clusterNodeInfoVO.setCpuRequest(redisNodeInfo.getCpuRequest());
                        clusterNodeInfoVO.setCpuRequestPercentage(redisNodeInfo.getCpuRequestPercentage());

                        clusterNodeInfoVO.setCpuTotal(redisNodeInfo.getCpuTotal());
                        clusterNodeInfoVO.setMemoryTotal(redisNodeInfo.getMemoryTotal());
                        clusterNodeInfoVO.setMemoryLimit(redisNodeInfo.getMemoryLimit());
                        clusterNodeInfoVO.setMemoryLimitPercentage(redisNodeInfo.getMemoryLimitPercentage());
                        clusterNodeInfoVO.setMemoryRequest(redisNodeInfo.getMemoryRequest());
                        clusterNodeInfoVO.setMemoryRequestPercentage(redisNodeInfo.getMemoryRequestPercentage());
                        clusterNodeInfoVO.setPodCount(redisNodeInfo.getPodCount());
                        clusterNodeInfoVO.setPodPercentage(redisNodeInfo.getPodPercentage());
                        clusterNodeInfoVO.setPodTotal(redisNodeInfo.getPodTotal());
                    }
                }
                return clusterNodeInfoVO;
            }).collect(Collectors.toList());

            result.setSize(pageable.getSize());
            result.setNumber(pageable.getPage());
            result.setTotalElements(nodeDTOS.getTotalElements());
            result.setContent(nodeInfoVOS);
            return result;
        } else if (ClusterTypeEnum.IMPORTED.value().equals(devopsClusterDTO.getType())) {
            // 现在分页从0开始了
            long start = (long) (pageable.getPage()) * (long) pageable.getSize();
            // stop不怕越界， redis会将边界之前的最后的那些元素返回
            long stop = start + pageable.getSize() - 1;
            nodes = stringRedisTemplate
                    .opsForList()
                    .range(redisKey, start, stop)
                    .stream()
                    .map(node -> JSONObject.parseObject(node, ClusterNodeInfoVO.class))
                    .collect(Collectors.toList());
            long total = stringRedisTemplate.opsForList().size(redisKey);


            if (total < pageable.getSize() * pageable.getPage()) {
                result.setSize(TypeUtil.objToInt(total) - (pageable.getSize() * (pageable.getPage() - 1)));
            } else {
                result.setSize(pageable.getSize());
            }
            result.setSize(pageable.getSize());
            result.setNumber(pageable.getPage());
            result.setTotalElements(total);
            result.setContent(nodes);

            return result;
        }
        return null;

    }

    @Override
    public ClusterNodeInfoVO queryNodeInfo(Long projectId, Long clusterId, String nodeName) {
        if (StringUtils.isEmpty(nodeName)) {
            return null;
        }

        String redisKey = getRedisClusterKey(clusterId, projectId);
        long total = stringRedisTemplate.opsForList().size(redisKey);

        // get all nodes according to the cluster id and filter the node with the certain name
        return stringRedisTemplate
                .opsForList()
                .range(redisKey, 0, total - 1)
                .stream()
                .map(node -> JSONObject.parseObject(node, ClusterNodeInfoVO.class))
                .filter(node -> nodeName.equals(node.getNodeName()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<String> queryNodeName(Long projectId, Long clusterId) {
        DevopsClusterDTO devopsClusterDTO = devopsClusterService.baseQuery(clusterId);

        String rediskey = getRedisClusterKey(clusterId, devopsClusterDTO.getProjectId());

        long total = stringRedisTemplate.opsForList().size(rediskey);

        return Objects.requireNonNull(stringRedisTemplate
                        .opsForList()
                        .range(rediskey, 0, total - 1))
                .stream()
                .map(node -> JSONObject.parseObject(node, ClusterNodeInfoVO.class))
                .map(ClusterNodeInfoVO::getNodeName)
                .collect(Collectors.toList());

    }

    @Override
    public long countNodes(Long projectId, Long clusterId) {
        String key = getRedisClusterKey(clusterId, projectId);
        Long count = stringRedisTemplate.opsForList().size(key);
        return count == null ? 0 : count;
    }
}
