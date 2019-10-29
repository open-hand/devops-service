package io.choerodon.devops.infra.util;

public class FeignResponseStatusCodeParse {
    private FeignResponseStatusCodeParse() {
    }

    public static Integer parseStatusCode(String message) {
        String[] statusMessageAndContent = message.split(";");
        return Integer.parseInt(statusMessageAndContent[0].split(" ")[1]);
    }
}
