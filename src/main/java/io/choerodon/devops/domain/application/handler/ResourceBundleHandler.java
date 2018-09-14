package io.choerodon.devops.domain.application.handler;

import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.ResourceBundle;

public class ResourceBundleHandler {


    private ResourceBundle bundle;


    private ResourceBundleHandler() {

        this.bundle = ResourceBundle.getBundle("messages/messages",Locale.SIMPLIFIED_CHINESE);
    }

    public static ResourceBundleHandler getInstance() {

        return ResourceBundleHandlerHolder.resourceBundleHandler;
    }

    public String getValue(String key) throws UnsupportedEncodingException {
        return new String(bundle.getString(key).getBytes("ISO-8859-1"), "UTF8");
    }

    private static class ResourceBundleHandlerHolder {
        private static ResourceBundleHandler resourceBundleHandler = new ResourceBundleHandler();
    }
}
