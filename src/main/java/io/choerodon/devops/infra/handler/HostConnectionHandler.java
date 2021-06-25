package io.choerodon.devops.infra.handler;

import io.choerodon.devops.app.service.DevopsHostService;
import io.choerodon.devops.infra.dto.DevopsHostDTO;
import io.choerodon.devops.infra.util.KeyParseUtil;
import io.choerodon.devops.infra.util.LogUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

import static io.choerodon.devops.infra.constant.DevOpsWebSocketConstants.*;

@Component
public class HostConnectionHandler {

    @Autowired
    private DevopsHostService devopsHostService;

    private static final Logger LOGGER = LoggerFactory.getLogger(HostConnectionHandler.class);

    public boolean validConnectionParameter(HttpServletRequest request) {

        //校验ws连接参数是否正确
        String key = request.getParameter(KEY);
        String hostId = request.getParameter(HOST_ID);
        String token = request.getParameter(TOKEN);

        if (key == null || key.trim().isEmpty()) {
            LOGGER.warn("Agent Handshake : Key is null");
            return false;
        }
        if (!KeyParseUtil.matchPattern(key)) {
            LOGGER.warn("Agent Handshake : Key not match the pattern");
            return false;
        }
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
        // todo 校验token
//        if (!token.equals(devopsHostDTO.getToken())) {
//            LOGGER.warn("Cluster with id {} exists but its token doesn't match the token that agent offers as {}", clusterId, token);
//            return false;
//        }

        return true;
    }
}
