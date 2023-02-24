package io.choerodon.devops.infra.dto.repo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * choerodon 版本返回DTO
 *
 * @author weisen.yang@hand-china.com 2020/7/2
 */
@ApiModel("版本返回DTO")

public class C7nNexusComponentDTO {
    @ApiModelProperty(value = "id")
    private String id;
    @ApiModelProperty(value = "仓库名称")
    private String repository;
    @ApiModelProperty(value = "format")
    private String format;
    @ApiModelProperty(value = "groupId")
    private String group;
    @ApiModelProperty(value = "artifactId")
    private String name;
    @ApiModelProperty(value = "版本")
    private String version;
    @ApiModelProperty(value = "下载地址")
    private String downloadUrl;
    @ApiModelProperty(value = "extension: pom、jar、war等")
    private String extension;

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    @Override
    public String toString() {
        return "C7nNexusComponentDTO{" +
                "id='" + id + '\'' +
                ", repository='" + repository + '\'' +
                ", format='" + format + '\'' +
                ", group='" + group + '\'' +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", downloadUrl='" + downloadUrl + '\'' +
                '}';
    }
}
