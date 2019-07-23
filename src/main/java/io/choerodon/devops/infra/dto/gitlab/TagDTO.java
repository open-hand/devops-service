package io.choerodon.devops.infra.dto.gitlab;

import org.springframework.beans.BeanUtils;

import io.choerodon.devops.infra.dto.CommitDTO;

/**
 * Created by Zenger on 2018/4/8.
 */
public class TagDTO {

    private CommitDTO commit;
    private String message;
    private String commitUserImage;
    private String name;
    private ReleaseDO release;

    public TagDTO() {
    }

    public TagDTO(TagDTO t) {
        BeanUtils.copyProperties(t, this);
        this.name = t.getName();
    }

    public CommitDTO getCommit() {
        return commit;
    }

    public void setCommit(CommitDTO commit) {
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

    public String getCommitUserImage() {
        return commitUserImage;
    }

    public void setCommitUserImage(String commitUserImage) {
        this.commitUserImage = commitUserImage;
    }
}