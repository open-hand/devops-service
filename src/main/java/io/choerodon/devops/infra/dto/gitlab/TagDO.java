package io.choerodon.devops.infra.dto.gitlab;

/**
 * Created by Zenger on 2018/4/8.
 */
public class TagDO {

    private CommitDO commit;
    private String message;
    private String name;
    private ReleaseDO release;

    public CommitDO getCommit() {
        return commit;
    }

    public void setCommit(CommitDO commit) {
        this.commit = commit;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ReleaseDO getRelease() {
        return release;
    }

    public void setRelease(ReleaseDO release) {
        this.release = release;
    }
}