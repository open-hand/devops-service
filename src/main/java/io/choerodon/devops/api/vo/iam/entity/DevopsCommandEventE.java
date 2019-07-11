package io.choerodon.devops.api.vo.iam.entity;

import java.util.Date;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class DevopsCommandEventE {

    private Long id;
    private DevopsEnvCommandE devopsEnvCommandE;
    private String type;
    private String name;
    private String message;
    private Date eventCreationTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DevopsEnvCommandE getDevopsEnvCommandE() {
        return devopsEnvCommandE;
    }

    public void initDevopsEnvCommandE(Long id) {
        this.devopsEnvCommandE = new DevopsEnvCommandE(id);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getEventCreationTime() {
        return eventCreationTime;
    }

    public void setEventCreationTime(Date eventCreationTime) {
        this.eventCreationTime = eventCreationTime;
    }
}
