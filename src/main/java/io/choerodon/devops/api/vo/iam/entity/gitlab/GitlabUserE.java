package io.choerodon.devops.api.vo.iam.entity.gitlab;

/**
 * Created by Zenger on 2018/3/28.
 */
public class GitlabUserE {

    private Integer id;
    private String username;

    public GitlabUserE() {

    }

    public GitlabUserE(Integer id, String username) {
        this.id = id;
        this.username = username;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
