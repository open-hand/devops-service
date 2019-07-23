package io.choerodon.devops.api.vo;

import io.choerodon.devops.infra.dto.CommitDTO;
import io.choerodon.devops.infra.dto.gitlab.ReleaseDO;
import io.choerodon.devops.infra.dto.gitlab.TagDTO;
import org.springframework.beans.BeanUtils;

/**
 * Creator: Runge
 * Date: 2018/7/6
 * Time: 10:22
 * Description:
 */
public class TagVO {

    private CommitDTO commit;
    private String commitUserImage;
    private String message;
    private String tagName;
    private ReleaseDO release;

    public TagVO() {
    }

    public TagVO(TagDTO t) {
        BeanUtils.copyProperties(t, this);
        this.tagName = t.getName();
    }

    public String getCommitUserImage() {
        return commitUserImage;
    }

    public void setCommitUserImage(String commitUserImage) {
        this.commitUserImage = commitUserImage;
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

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public ReleaseDO getRelease() {
        return release;
    }

    public void setRelease(ReleaseDO release) {
        this.release = release;
    }
}
