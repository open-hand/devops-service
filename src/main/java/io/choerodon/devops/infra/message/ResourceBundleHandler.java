package io.choerodon.devops.infra.message;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.ResourceBundle;

public class ResourceBundleHandler {


    private ResourceBundle bundle;


    private ResourceBundleHandler() {

        this.bundle = ResourceBundle.getBundle("messages/messages", Locale.SIMPLIFIED_CHINESE);
    }

    public static ResourceBundleHandler getInstance() {

        return ResourceBundleHandlerHolder.resourceBundleHandler;
    }

    public String getValue(String key) {
        return new String(bundle.getString(key).getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
    }

    private static class ResourceBundleHandlerHolder {
        private static ResourceBundleHandler resourceBundleHandler = new ResourceBundleHandler();
    }
}
