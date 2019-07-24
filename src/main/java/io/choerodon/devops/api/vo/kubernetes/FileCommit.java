package io.choerodon.devops.api.vo.kubernetes;

public class FileCommit {

    private String file;
    private String commit;

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getCommit() {
        return commit;
    }

    public void setCommit(String commit) {
        this.commit = commit;
    }
}
