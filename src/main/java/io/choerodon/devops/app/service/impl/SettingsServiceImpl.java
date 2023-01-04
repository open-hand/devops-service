package io.choerodon.devops.app.service.impl;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import io.choerodon.devops.app.service.SettingsService;

@Service
public class SettingsServiceImpl implements SettingsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SettingsServiceImpl.class);

    @Value("${services.gitlab.resetPasswordUrl:}")
    private String resetPasswordUrl;

    @PostConstruct
    public void postConstruct() {
        if (ObjectUtils.isEmpty(resetPasswordUrl)) {
            LOGGER.warn("WARN!WARN!WARN! The gitlab reset password url can't be empty.But this won't stop devops-service from starting");
        }
    }

    @Override
    public String getGitlabResetPasswordUrl() {
        return resetPasswordUrl;
    }
}
