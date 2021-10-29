package io.choerodon.devops.app.aop;


import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import io.choerodon.core.exception.CommonException;
import io.choerodon.limiter.RedisRateLimiter;
import io.choerodon.limiter.constant.LimiterConstants;


@Aspect
@Component
public class RedisLimitAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisLimitAspect.class);
    private RedisRateLimiter defaultGitlabRedisLimitAspect;

    public RedisLimitAspect(RedisRateLimiter defaultGitlabRedisLimitAspect) {
        this.defaultGitlabRedisLimitAspect = defaultGitlabRedisLimitAspect;
    }

    @Before(value = "execution(* io.choerodon.devops.infra.util.GitUtil.*(..))")
    public void rateLimit() {
        boolean acquired = true;
        try {
            acquired = defaultGitlabRedisLimitAspect.tryAcquire("gitlab.limiter", 1, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOGGER.error("Error acquire permit.", e);
        }
        if (!acquired) {
            throw new CommonException(LimiterConstants.ERROR_SERVICE_IS_BUSY);
        }
    }

}
