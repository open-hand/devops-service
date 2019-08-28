package io.choerodon.devops.api.ws;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.socket.WebSocketSession;

/**
 * Created by Sheep on 2019/7/25.
 */
public class WebSocketTool {

    private WebSocketTool() {
    }

    public static Map<String, Object> getAttribute(WebSocketSession webSocketSession) {
        Map<String, Object> attribute = new HashMap<>();
        Arrays.asList(webSocketSession.getUri().getQuery().split("&")).forEach(s -> {
            String[] result = s.split("=");
            attribute.put(result[0], result[1]);
        });
        return attribute;
    }

    /**
     * 解决shell里面前端遇到\r不能换行的问题。目前解决方案是将返回结果中除了首尾部的\r替换成\r\n
     *
     * @param a
     * @param index
     * @return
     */
    public static String replaceR(StringBuilder a, int index) {
        int lastIndex = a.lastIndexOf("\r");
        if (lastIndex == -1 || index >= a.length() - 1) {
            return a.toString();
        }
        int indexResult = a.indexOf("\r", index);
        if (indexResult >= 0) {
            if (indexResult != a.length() - 1) {
                String r = a.substring(indexResult + 1, indexResult + 2);
                if (!r.equals("\n")) {
                    if (indexResult > 0) {
                        a = a.replace(indexResult, indexResult + 1, "\r\n");
                    }
                    return replaceR(a, indexResult + 1);
                } else {
                    return replaceR(a, indexResult + 1);
                }
            } else {
                a = a.replace(indexResult, indexResult + 1, "\r\n");
            }
        }
        return a.toString();
    }


}
