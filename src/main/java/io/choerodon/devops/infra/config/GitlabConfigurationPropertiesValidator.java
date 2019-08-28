package io.choerodon.devops.infra.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * @author zmf
 */
@Configuration
public class GitlabConfigurationPropertiesValidator implements Validator {
    private Logger logger = LoggerFactory.getLogger(GitlabConfigurationPropertiesValidator.class);

    @Override
    public boolean supports(Class<?> clazz) {
        return GitlabConfigurationProperties.class == clazz;
    }

    @Override
    public void validate(Object target, Errors errors) {
        GitlabConfigurationProperties properties = (GitlabConfigurationProperties) target;
        if (properties.getPassword() == null || properties.getPassword().length() < 8) {
            logger.error("===============================================ERROR======================================================");
            logger.error("Error: The gitlab password in gitlab configuration must not be null and it's length should not less than 8");
            logger.error("===============================================ERROR======================================================");
            errors.rejectValue("services.gitlab.password", "error.password.length");
        }
    }


    @Bean
    public static Validator configurationPropertiesValidator() {
        return new GitlabConfigurationPropertiesValidator();
    }
}
