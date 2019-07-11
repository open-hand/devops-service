package io.choerodon.devops.api.vo.iam.entity.gitlab;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 合并请求改动
 */
public class DiffE {

    @JsonInclude(JsonInclude.Include.ALWAYS)
    @JsonProperty("a_mode")
    private String aMode;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    @JsonProperty("b_mode")
    private String bMode;

    private Boolean deletedFile;
    private String diff;
    private Boolean newFile;
    private String newPath;
    private String oldPath;
    private Boolean renamedFile;

    public String getaMode() {
        return aMode;
    }

    public void setaMode(String aMode) {
        this.aMode = aMode;
    }

    public String getbMode() {
        return bMode;
    }

    public void setbMode(String bMode) {
        this.bMode = bMode;
    }

    public Boolean getDeletedFile() {
        return this.deletedFile;
    }

    public void setDeletedFile(Boolean deletedFile) {
        this.deletedFile = deletedFile;
    }

    public String getDiff() {
        return this.diff;
    }

    public void setDiff(String diff) {
        this.diff = diff;
    }

    public Boolean getNewFile() {
        return this.newFile;
    }

    public void setNewFile(Boolean newFile) {
        this.newFile = newFile;
    }

    public String getNewPath() {
        return this.newPath;
    }

    public void setNewPath(String newPath) {
        this.newPath = newPath;
    }

    public String getOldPath() {
        return this.oldPath;
    }

    public void setOldPath(String oldPath) {
        this.oldPath = oldPath;
    }

    public Boolean getRenamedFile() {
        return this.renamedFile;
    }

    public void setRenamedFile(Boolean renamedFile) {
        this.renamedFile = renamedFile;
    }
}
