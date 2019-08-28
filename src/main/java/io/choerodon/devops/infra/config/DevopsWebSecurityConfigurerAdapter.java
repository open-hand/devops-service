package io.choerodon.devops.infra.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * Created by Sheep on 2019/4/3.
 */


@Configuration
@EnableWebSecurity
@Order(1)
public class DevopsWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

    private static  final String WEBHOOK_ALL="/webhook/**";

    @Override
    public void configure(WebSecurity web) throws Exception {
        web
                .ignoring()
                .antMatchers("/workflow/**", "/sonar/**", "/v1/**", "/ci", "/sonar/info", "/v2/api-docs", "/agent/**", "/ws/**", "/gitlab/email", WEBHOOK_ALL, "/v2/choerodon/**", "/choerodon/**", "/actuator/**", "/prometheus");
    }


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests().antMatchers("/workflow/**", "/sonar/info", "/sonar/**", "/ci", WEBHOOK_ALL, "/v2/api-docs", "/agent/**", "/ws/**", "/gitlab/email").permitAll();
    }
}
