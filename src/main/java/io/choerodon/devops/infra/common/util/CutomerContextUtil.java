package io.choerodon.devops.infra.common.util;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

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

            OAuth2Authentication user = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
            user.setDetails(customUserDetails);
            SecurityContextHolder.getContext().setAuthentication(user);
        } catch (Exception e) {
            throw new CommonException("change user expection", e);
        }
    }
}
