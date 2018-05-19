package io.choerodon.devops.domain.application.valueobject;

/**
 * Created by younger on 2018/3/27.
 */
public class GitRepository {
    private String repoURL;

    public GitRepository(String repoURL) {
        this.repoURL = repoURL;
    }

    public String getRepoURL() {
        return repoURL;
    }
}
