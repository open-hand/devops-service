package io.choerodon.devops.domain.application.entity;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:19 2019/2/26
 * Description:
 */
@Component
@Scope("prototype")
public class DevopsAutoDeployValueE {
    private Long id;
    private String value;

    public DevopsAutoDeployValueE() {
    }

    public DevopsAutoDeployValueE(String value) {
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
