package io.choerodon.devops.infra.dto.gitlab.ci;

import io.swagger.annotations.ApiModelProperty;

/**
 * 参考 https://docs.gitlab.com/ee/ci/yaml/README.html#include
 *  local file remote template 四个属性的值最少有一个，有file就要有ref和project
 *
 * @author zmf
 * @since 20-4-2
 */
public class Include {
    @ApiModelProperty("localPath: eg: /templates/.gitlab-ci-template.yml")
    private String local;
    @ApiModelProperty("include:remote can be used to include a file from a different location, using HTTP/HTTPS, referenced by using the full URL.")
    private String remote;
    @ApiModelProperty("include:template can be used to include .gitlab-ci.yml templates that are shipped with GitLab.")
    private String template;

    @ApiModelProperty("To include files from another private project under the same GitLab instance")
    private String file;
    @ApiModelProperty("Git ref")
    private String ref;
    @ApiModelProperty("my-group/my-project")
    private String project;

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public String getRemote() {
        return remote;
    }

    public void setRemote(String remote) {
        this.remote = remote;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }
}
