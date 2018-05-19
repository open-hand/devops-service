package io.choerodon.devops.domain.application.entity.gitlab;

public class GitlabCommitE {

    private String id;
    private String authorName;
    private String shortId;
    private String title;

    public GitlabCommitE() {

    }

    /**
     * 构造函数
     */
    public GitlabCommitE(String authorName, String shortId, String title) {
        this.authorName = authorName;
        this.shortId = shortId;
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getShortId() {
        return shortId;
    }

    public void setShortId(String shortId) {
        this.shortId = shortId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}