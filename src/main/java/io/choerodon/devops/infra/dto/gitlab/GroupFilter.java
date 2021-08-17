package io.choerodon.devops.infra.dto.gitlab;

import java.util.List;

/**
 *  This class is used to filter Projects when getting lists of projects for a specified group.
 */
public class GroupFilter {

    private List<Integer> skipGroups;
    private Boolean allAvailable;
    private String search;
    private String orderBy;
    private String sort;
    private Boolean statistics;
    private Boolean withCustomAttributes;
    private Boolean owned;
    private Integer accessLevel;

    public List<Integer> getSkipGroups() {
        return skipGroups;
    }

    public void setSkipGroups(List<Integer> skipGroups) {
        this.skipGroups = skipGroups;
    }

    public Boolean getAllAvailable() {
        return allAvailable;
    }

    public void setAllAvailable(Boolean allAvailable) {
        this.allAvailable = allAvailable;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
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

    public Boolean getStatistics() {
        return statistics;
    }

    public void setStatistics(Boolean statistics) {
        this.statistics = statistics;
    }

    public Boolean getWithCustomAttributes() {
        return withCustomAttributes;
    }

    public void setWithCustomAttributes(Boolean withCustomAttributes) {
        this.withCustomAttributes = withCustomAttributes;
    }

    public Boolean getOwned() {
        return owned;
    }

    public void setOwned(Boolean owned) {
        this.owned = owned;
    }

    public Integer getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(Integer accessLevel) {
        this.accessLevel = accessLevel;
    }
}
