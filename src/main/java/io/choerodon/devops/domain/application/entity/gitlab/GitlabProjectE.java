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

}
