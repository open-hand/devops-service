package io.choerodon.devops.infra.ws;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.socket.WebSocketSession;

/**
 * Created by Sheep on 2019/7/25.
 */
public class WebSocketTool {


    public static Map<String, Object> getAttribute(WebSocketSession webSocketSession) {
        Map<String, Object> attribute = new HashMap<>();
        Arrays.asList(webSocketSession.getUri().getQuery().split("&")).forEach(s -> {
            String[] result = s.split("=");
            attribute.put(result[0], result[1]);
        });
        return attribute;
    }

}
