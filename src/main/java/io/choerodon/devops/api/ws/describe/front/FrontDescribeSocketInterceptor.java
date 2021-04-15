package io.choerodon.devops.api.ws.describe.front;

import static io.choerodon.devops.infra.constant.DevOpsWebSocketConstants.*;

import java.util.Map;
import javax.annotation.Resource;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;

import io.choerodon.devops.api.ws.AbstractSocketInterceptor;
import io.choerodon.devops.api.ws.WebSocketTool;
import io.choerodon.devops.app.service.DevopsCustomizeResourceService;

/**
 * @author zmf
 * @since 20-5-9
 */
@Component
public class FrontDescribeSocketInterceptor extends AbstractSocketInterceptor {
    @Resource
    private DevopsCustomizeResourceService devopsCustomizeResourceService;

    @Override
    public String processor() {
        return FRONT_DESCRIBE;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        // 先校验token
        if (!WebSocketTool.preCheckOAuthToken(attributes)) {
            return false;
        }

        WebSocketTool.preProcessAttributeAboutKeyEncryption(attributes);
        //校验ws连接参数是否正确
        WebSocketTool.checkKey(attributes);
        WebSocketTool.checkEnv(attributes);
        WebSocketTool.checkKind(attributes);
        WebSocketTool.checkName(attributes);
        WebSocketTool.checkDescribeId(attributes);
        WebSocketTool.checkClusterId(attributes);
        WebSocketTool.checkProjectId(attributes);

        return checkPermission(attributes);
    }

    private boolean checkPermission(Map<String, Object> attributes) {
        // 校验用户的权限
        Long projectId = Long.parseLong(String.valueOf(attributes.get(PROJECT_ID)));
        Long clusterId = Long.parseLong(String.valueOf(attributes.get(CLUSTER_ID)));
        Long userId = Long.parseLong(String.valueOf(attributes.get(USER_ID)));
        String envCode = String.valueOf(attributes.get(ENV));
        String kind = String.valueOf(attributes.get(KIND));
        String resourceName = String.valueOf(attributes.get(NAME));
        return devopsCustomizeResourceService.checkDescribePermission(projectId, clusterId, envCode, userId, kind, resourceName);
    }
}
