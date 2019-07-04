package io.choerodon.devops.domain.application.entity;

import java.util.Date;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import io.choerodon.devops.domain.application.valueobject.ApplicationVersionReadmeV;

@Component
@Scope("prototype")
public class ApplicationVersionE {

    private Long id;
    private String version;
    private String commit;
    private String image;
    private Date creationDate;
    private ApplicationE applicationE;
    private String repository;
    private Long isPublish;
    private Date publishTime;
    private ApplicationVersionValueE applicationVersionValueE;
    private ApplicationVersionReadmeV applicationVersionReadmeV;

    public ApplicationVersionE() {

    }

    public Date getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(Date publishTime) {
        this.publishTime = publishTime;
    }

    public ApplicationVersionE(Long id) {
        this.id = id;
    }

    public ApplicationVersionE(Long id, String version) {
        this.id = id;
        this.version = version;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCommit() {
        return commit;
    }

    public void setCommit(String commit) {
        this.commit = commit;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public ApplicationE getApplicationE() {
        return applicationE;
    }

    public void setApplicationE(ApplicationE applicationE) {
        this.applicationE = applicationE;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public void initApplicationE(Long id, String code, String name, Boolean active) {
        this.applicationE = new ApplicationE(id, code, name, active);
    }

    public void initApplicationEById(Long id) {
        this.applicationE = new ApplicationE(id);
    }

    public ApplicationVersionValueE getApplicationVersionValueE() {
        return applicationVersionValueE;
    }

    public void setApplicationVersionValueE(ApplicationVersionValueE applicationVersionValueE) {
        this.applicationVersionValueE = applicationVersionValueE;
    }

    public void initApplicationVersionValueE(Long id) {
        this.applicationVersionValueE = new ApplicationVersionValueE(id);
    }

    public ApplicationVersionReadmeV getApplicationVersionReadmeV() {
        return applicationVersionReadmeV;
    }

    public void setApplicationVersionReadmeV(ApplicationVersionReadmeV applicationVersionReadmeV) {
        this.applicationVersionReadmeV = applicationVersionReadmeV;
    }

    public void initApplicationVersionReadmeV(String readme) {
        this.applicationVersionReadmeV = new ApplicationVersionReadmeV(readme);
    }


    public void initApplicationVersionReadmeVById(Long id) {
        this.applicationVersionReadmeV = new ApplicationVersionReadmeV(id);
    }

    public Long getIsPublish() {
        return isPublish;
    }

    public void setIsPublish(Long isPublish) {
        this.isPublish = isPublish;
    }
}
