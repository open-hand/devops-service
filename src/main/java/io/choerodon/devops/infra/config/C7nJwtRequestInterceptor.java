//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package io.choerodon.devops.infra.config;

import javax.annotation.PostConstruct;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yqcloud.core.oauth.ZKnowDetailsHelper;
import feign.RequestTemplate;
import org.apache.commons.lang3.StringUtils;
import org.hzero.core.properties.CoreProperties;
import org.hzero.core.variable.RequestVariableHolder;
import org.hzero.feign.interceptor.JwtRequestInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.MacSigner;
import org.springframework.security.jwt.crypto.sign.Signer;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;

import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;

@Primary
public class C7nJwtRequestInterceptor extends JwtRequestInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtRequestInterceptor.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String OAUTH_TOKEN_PREFIX = "Bearer ";
    private Signer signer;
    private CoreProperties coreProperties;

    public C7nJwtRequestInterceptor(CoreProperties coreProperties) {
        super(coreProperties);
    }


    @PostConstruct
    private void init() {
        this.signer = new MacSigner(this.coreProperties.getOauthJwtKey());
    }

    public int getOrder() {
        return -1000;
    }

    public void apply(RequestTemplate template) {
        String token = null;

        try {
            if (SecurityContextHolder.getContext() != null && SecurityContextHolder.getContext().getAuthentication() != null) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication.getDetails() instanceof OAuth2AuthenticationDetails) {
                    OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) authentication.getDetails();
                    if (StringUtils.isNoneBlank(new CharSequence[]{details.getTokenType(), details.getTokenValue()})) {
                        token = details.getTokenType() + " " + details.getTokenValue();
                    } else if (details.getDecodedDetails() instanceof CustomUserDetails) {
                        token = "Bearer " + JwtHelper.encode(OBJECT_MAPPER.writeValueAsString(details.getDecodedDetails()), this.signer).getEncoded();
                    }
                } else if (authentication.getPrincipal() instanceof CustomUserDetails) {
                    token = "Bearer " + JwtHelper.encode(OBJECT_MAPPER.writeValueAsString(authentication.getPrincipal()), this.signer).getEncoded();
                }
            }

            if (token == null) {
                LOGGER.debug("Feign request set Header Jwt_Token, no member token found, use AnonymousUser default.");
                CustomUserDetails anonymousDetails = DetailsHelper.getAnonymousDetails();
                ZKnowDetailsHelper.setRequestSource(anonymousDetails, ZKnowDetailsHelper.VALUE_CHOERODON);
                token = "Bearer " + JwtHelper.encode(OBJECT_MAPPER.writeValueAsString(anonymousDetails), this.signer).getEncoded();
            }
        } catch (Exception var5) {
            LOGGER.error("generate jwt token failed {}", var5.getMessage());
        }

        template.header("Jwt_Token", new String[]{token});
        this.setLabel(template);
    }

    private void setLabel(RequestTemplate template) {
        String label = (String) RequestVariableHolder.LABEL.get();
        if (label != null && label.trim().length() > 0) {
            template.header("X-Eureka-Label", new String[]{label});
        }

    }
}
