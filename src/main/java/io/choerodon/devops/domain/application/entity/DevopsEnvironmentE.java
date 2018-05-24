package io.choerodon.devops.domain.application.entity;

import java.util.List;
import java.util.LongSummaryStatistics;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Created by younger on 2018/4/9.
 */

@Component
@Scope("prototype")
public class DevopsEnvironmentE {
    private Long id;
    private ProjectE projectE;
    private String name;
    private String code;
    private String token;
    private Long sequence;
    private String description;
    private Boolean isConnected;
    private Boolean isActive;
    private Boolean isUpdate;
    private String updateMessage;
    /**
     * 重写构造方法
     */
    public DevopsEnvironmentE(Long id, String name, String namespace, String description, Boolean isConnect, Boolean isActive, String code) {

        this.id = id;
        this.name = name;
        this.description = description;
        this.isConnected = isConnect;
        this.isActive = isActive;
        this.code = code;
    }

    /**
     * 构造函数
     */
    public DevopsEnvironmentE(Long id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
    }


    public DevopsEnvironmentE() {
    }

    public DevopsEnvironmentE(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public Boolean getConnect() {
        return isConnected;
    }

    public void setConnect(Boolean connect) {
        isConnected = connect;
    }

    public ProjectE getProjectE() {
        return projectE;
    }

    public void setProjectE(ProjectE projectE) {
        this.projectE = projectE;
    }

    public void initProjectE(Long id) {
        this.projectE = new ProjectE(id);
    }


    public void initActive(Boolean active) {
        this.isActive = active;
    }

    public void initConnect(Boolean connect) {
        this.isConnected = connect;

    }

    public Boolean getUpdate() {
        return isUpdate;
    }

    public void setUpdate(Boolean update) {
        isUpdate = update;
    }

    public String getUpdateMessage() {
        return updateMessage;
    }

    public void setUpdateMessage(String updateMessage) {
        this.updateMessage = updateMessage;
    }

    public Long getSequence() {
        return sequence;
    }

    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }

    public void initToken(String token) {
        this.token = token;
    }

    /**
     * 初始化序列
     *
     */
    public void initSequence(List<DevopsEnvironmentE> devopsEnvironmentES) {
        this.sequence = 1L;
        if (!devopsEnvironmentES.isEmpty()) {
            LongSummaryStatistics stats = devopsEnvironmentES
                    .parallelStream()
                    .mapToLong(DevopsEnvironmentE::getSequence)
                    .summaryStatistics();
            this.sequence = stats.getMax() + 1;
        }
    }
}
