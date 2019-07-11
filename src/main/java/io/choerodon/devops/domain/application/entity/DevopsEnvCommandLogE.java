package io.choerodon.devops.domain.application.entity;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Created by younger on 2018/4/24.
 */
@Scope("prototype")
@Component
public class DevopsEnvCommandLogE {
    private Long id;
    private String log;
    private DevopsEnvCommandE devopsEnvCommandE;

    public DevopsEnvCommandLogE() {
    }

    public DevopsEnvCommandLogE(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public DevopsEnvCommandE getDevopsEnvCommandE() {
        return devopsEnvCommandE;
    }

    public void initDevopsEnvCommandE(Long id) {
        this.devopsEnvCommandE = new DevopsEnvCommandE(id);
    }
}
