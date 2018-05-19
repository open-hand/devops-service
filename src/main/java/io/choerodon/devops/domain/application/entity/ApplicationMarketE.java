package io.choerodon.devops.domain.application.entity;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class ApplicationMarketE {

    private Long id;
    private ApplicationE applicationE;
    private String name;
    private String code;
    private String publishLevel;
    private String imgUrl;
    private String contributor;
    private String description;

    public ApplicationMarketE() {

    }

    public ApplicationMarketE(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ApplicationE getApplicationE() {
        return applicationE;
    }

    public void setApplicationE(ApplicationE applicationE) {
        this.applicationE = applicationE;
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

    public String getPublishLevel() {
        return publishLevel;
    }

    public void setPublishLevel(String publishLevel) {
        this.publishLevel = publishLevel;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getContributor() {
        return contributor;
    }

    public void setContributor(String contributor) {
        this.contributor = contributor;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void initApplicationEById(Long id) {
        this.applicationE = new ApplicationE(id);
    }

}
