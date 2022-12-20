//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package io.choerodon.devops.infra.config;

import com.yqcloud.core.oauth.ZKnowDetailsHelper;
import feign.RequestTemplate;
import org.hzero.feign.interceptor.FeignRequestInterceptor;
import org.hzero.feign.interceptor.JwtRequestInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;

@Component
public class ProductSourceRequestInterceptor implements FeignRequestInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtRequestInterceptor.class);


    public int getOrder() {
        return -900;
    }

    public void apply(RequestTemplate template) {
        CustomUserDetails userDetails = DetailsHelper.getUserDetails();

        try {
            if (userDetails != null) {
                userDetails.getAdditionInfo().putIfAbsent("request-source", ZKnowDetailsHelper.VALUE_CHOERODON);
            } else {
                CustomUserDetails anonymousDetails = DetailsHelper.getAnonymousDetails();
                anonymousDetails.getAdditionInfo().putIfAbsent("request-source", ZKnowDetailsHelper.VALUE_CHOERODON);
                DetailsHelper.setCustomUserDetails(anonymousDetails);
            }
        } catch (Exception e) {
            LOGGER.error("============================Add product source failed=============================", e);
        }
    }
}
