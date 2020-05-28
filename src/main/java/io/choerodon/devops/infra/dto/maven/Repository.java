package io.choerodon.devops.infra.dto.maven;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author zmf
 * @since 20-4-14
 */
public class Repository {
    @ApiModelProperty("仓库id")
    private String id;
    @ApiModelProperty("仓库名称")
    private String name;
    @ApiModelProperty("仓库url")
    private String url;
    @ApiModelProperty("release策略")
    private RepositoryPolicy releases;
    @ApiModelProperty("快照策略")
    private RepositoryPolicy snapshots;

    public Repository() {
    }

    public Repository(String id, String name, String url, RepositoryPolicy releases, RepositoryPolicy snapshots) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.releases = releases;
        this.snapshots = snapshots;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public RepositoryPolicy getReleases() {
        return releases;
    }

    public void setReleases(RepositoryPolicy releases) {
        this.releases = releases;
    }

    public RepositoryPolicy getSnapshots() {
        return snapshots;
    }

    public void setSnapshots(RepositoryPolicy snapshots) {
        this.snapshots = snapshots;
    }

    @Override
    public String toString() {
        return "Repository{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", releases=" + releases +
                ", snapshots=" + snapshots +
                '}';
    }
}
