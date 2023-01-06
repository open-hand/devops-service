package io.choerodon.devops.infra.util;

import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;
import org.springframework.util.ObjectUtils;

import io.choerodon.core.convertor.ApplicationContextHelper;


public class EurekaInstanceUtil {

    private EurekaInstanceUtil() {
    }

    private static String instanceId;

    public static String getInstanceId() {
        if (ObjectUtils.isEmpty(instanceId)) {
            synchronized (GitUserNameUtil.class) {
                if (ObjectUtils.isEmpty(instanceId)) {

                    instanceId = ApplicationContextHelper.getContext().getBean(EurekaInstanceConfigBean.class).getInstanceId();
                }
                return instanceId;
            }
        } else {
            return instanceId;
        }
    }
}
