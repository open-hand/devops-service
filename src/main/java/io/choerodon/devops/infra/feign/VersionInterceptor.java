//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package io.choerodon.devops.infra.feign;

import javax.servlet.http.HttpServletRequest;

import feign.RequestTemplate;
import org.apache.commons.lang3.StringUtils;
import org.hzero.feign.interceptor.FeignRequestInterceptor;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class VersionInterceptor implements FeignRequestInterceptor {
    private static final String HEADER_VERSION = "version";

    public VersionInterceptor() {
    }

    public void apply(RequestTemplate template) {
        if (RequestContextHolder.getRequestAttributes() != null && RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes) {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String version = request.getHeader(HEADER_VERSION);
            if (StringUtils.isNotBlank(version)) {
                template.header(HEADER_VERSION, new String[]{version});
            }
        }

    }
}
