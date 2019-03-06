package io.choerodon.devops.infra.common.util;

import io.choerodon.asgard.saga.consumer.MockHttpServletRequest;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;

import java.util.Collections;
import java.util.HashMap;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  17:24 2019/3/1
 * Description:
 */
public class CutomerContextUtil {


    public static void setUserId(Long uesrId) {
        try {
            CustomUserDetails customUserDetails = DetailsHelper.getUserDetails();
            customUserDetails.setUserId(uesrId);

            Authentication user = new UsernamePasswordAuthenticationToken("default", "N/A", Collections.emptyList());
            OAuth2Request request = new OAuth2Request(new HashMap<>(0), "", Collections.emptyList(), true,
                    Collections.emptySet(), Collections.emptySet(), null, null, null);
            OAuth2Authentication authentication = new OAuth2Authentication(request, user);
            OAuth2AuthenticationDetails oAuth2AuthenticationDetails = new OAuth2AuthenticationDetails(new MockHttpServletRequest());
            oAuth2AuthenticationDetails.setDecodedDetails(customUserDetails);
            authentication.setDetails(oAuth2AuthenticationDetails);
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            throw new CommonException("change user expection", e);
        }
    }
}
