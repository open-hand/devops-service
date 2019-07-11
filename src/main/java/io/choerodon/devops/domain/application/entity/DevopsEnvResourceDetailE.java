package io.choerodon.devops.domain.application.entity;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Created by younger on 2018/4/24.
 */
@Scope("prototype")
@Component
public class DevopsEnvResourceDetailE {
    private Long id;
    private String message;

    public DevopsEnvResourceDetailE() {
    }

    public DevopsEnvResourceDetailE(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
