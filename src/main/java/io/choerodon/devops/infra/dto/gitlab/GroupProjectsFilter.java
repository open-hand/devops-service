package io.choerodon.devops.infra.dto.gitlab;

/**
 *  This class is used to filter Projects when getting lists of projects for a specified group.
 */
public class GroupProjectsFilter {

    private Boolean archived;
    private String visibility;
    private String orderBy;
    private String sort;
    private String search;
    private Boolean simple;
    private Boolean owned;
    private Boolean starred;
    private Boolean withCustomAttributes;
    private Boolean withIssuesEnabled;
    private Boolean withMergeRequestsEnabled;
    private Boolean withShared;
    private Boolean includeSubGroups;
    private Integer page;
    private Integer perPage;

    public Boolean getArchived() {
        return archived;
    }

    public void setArchived(Boolean archived) {
        this.archived = archived;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public Boolean getSimple() {
        return simple;
    }

    public void setSimple(Boolean simple) {
        this.simple = simple;
    }

    public Boolean getOwned() {
        return owned;
    }

    public void setOwned(Boolean owned) {
        this.owned = owned;
    }

    public Boolean getStarred() {
        return starred;
    }

    public void setStarred(Boolean starred) {
        this.starred = starred;
    }

    public Boolean getWithCustomAttributes() {
        return withCustomAttributes;
    }

    public void setWithCustomAttributes(Boolean withCustomAttributes) {
        this.withCustomAttributes = withCustomAttributes;
    }

    public Boolean getWithIssuesEnabled() {
        return withIssuesEnabled;
    }

    public void setWithIssuesEnabled(Boolean withIssuesEnabled) {
        this.withIssuesEnabled = withIssuesEnabled;
    }

    public Boolean getWithMergeRequestsEnabled() {
        return withMergeRequestsEnabled;
    }

    public void setWithMergeRequestsEnabled(Boolean withMergeRequestsEnabled) {
        this.withMergeRequestsEnabled = withMergeRequestsEnabled;
    }

    public Boolean getWithShared() {
        return withShared;
    }

    public void setWithShared(Boolean withShared) {
        this.withShared = withShared;
    }

    public Boolean getIncludeSubGroups() {
        return includeSubGroups;
    }

    public void setIncludeSubGroups(Boolean includeSubGroups) {
        this.includeSubGroups = includeSubGroups;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPerPage() {
        return perPage;
    }

    public void setPerPage(Integer perPage) {
        this.perPage = perPage;
    }
}
