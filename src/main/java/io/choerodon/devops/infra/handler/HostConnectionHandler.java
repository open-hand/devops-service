package io.choerodon.devops.infra.handler;

import static io.choerodon.devops.infra.constant.DevOpsWebSocketConstants.HOST_ID;
import static io.choerodon.devops.infra.constant.DevOpsWebSocketConstants.TOKEN;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.host.HostSessionVO;
import io.choerodon.devops.app.service.DevopsHostService;
import io.choerodon.devops.infra.constant.DevopsHostConstants;
import io.choerodon.devops.infra.dto.DevopsHostDTO;
import io.choerodon.devops.infra.util.LogUtil;
import io.choerodon.devops.infra.util.TypeUtil;

@Component
public class HostConnectionHandler {

    @Value("${devops.host.agent-version}")
    private String agentVersion;

    @Autowired
    private DevopsHostService devopsHostService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final Logger LOGGER = LoggerFactory.getLogger(HostConnectionHandler.class);

    public boolean validConnectionParameter(HttpServletRequest request) {

        //校验ws连接参数是否正确
        String hostId = request.getParameter(HOST_ID);
        String token = request.getParameter(TOKEN);

        if (hostId == null || hostId.trim().isEmpty()) {
            LOGGER.warn("Agent Handshake : hostId is null");
            return false;
        }
        if (token == null || token.trim().isEmpty()) {
            LOGGER.warn("Agent Handshake : Token is null");
            return false;
        }

        //检验连接过来的agent和集群是否匹配
        DevopsHostDTO devopsHostDTO = devopsHostService.baseQuery(TypeUtil.objToLong(hostId));
        if (devopsHostDTO == null) {
            LogUtil.loggerWarnObjectNullWithId("hostId", TypeUtil.objToLong(hostId), LOGGER);
            return false;
        }
        if (!token.equals(devopsHostDTO.getToken())) {
            LOGGER.warn("Host with id {} exists but its token doesn't match the token that agent offers as {}", hostId, token);
            return false;
        }

        return true;
    }

    /**
     * 检查集群的环境是否链接
     *
     * @param hostId 环境ID
     */
    public void checkHostConnection(Long hostId) {
        if (!getHostConnectionStatus(hostId)) {
            throw new CommonException("devops.host.disconnect");
        }
    }

    /**
     * 不需要进行升级的已连接的集群 up-to-date
     * 版本相等就认为不需要升级
     *
     * @return 环境更新列表
     */
    public List<Long> getUpdatedHostList() {
        Map<String, HostSessionVO> clusterSessions = (Map<String, HostSessionVO>) (Map) redisTemplate.opsForHash().entries(DevopsHostConstants.HOST_SESSION);
        return clusterSessions.values().stream()
                .filter(clusterSessionVO -> agentVersion.equals(clusterSessionVO.getVersion()))
                .map(HostSessionVO::getHostId)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * 检查主机是否链接
     *
     * @param hostId 主机ID
     * @return true 表示已连接
     */
    public boolean getHostConnectionStatus(Long hostId) {
        Map<String, HostSessionVO> clusterSessions = (Map<String, HostSessionVO>) (Map) redisTemplate.opsForHash().entries(DevopsHostConstants.HOST_SESSION);
        return clusterSessions.values().stream()
                .anyMatch(t -> hostId.equals(t.getHostId())
                        && agentVersion.equals(t.getVersion()));
    }
}
