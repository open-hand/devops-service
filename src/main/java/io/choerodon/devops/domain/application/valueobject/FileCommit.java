package io.choerodon.devops.domain.application.valueobject;

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
