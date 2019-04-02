package io.choerodon.devops.domain.application.entity.gitlab;

/**
 * Created by younger on 2018/3/28.
 */
public class GitlabProjectE {
    private Integer id;
    private String name;
    private String path;
    private String repoURL;


    public GitlabProjectE() {
    }

    public GitlabProjectE(String repoURL) {
        this.repoURL = repoURL;
    }

    public GitlabProjectE(Integer id) {
        this.id = id;
    }

    public GitlabProjectE(Integer id, String repoURL) {
        this.id = id;
        this.repoURL = repoURL;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getRepoURL() {
        return repoURL;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setRepoURL(String repoURL) {
        this.repoURL = repoURL;
    }
}
