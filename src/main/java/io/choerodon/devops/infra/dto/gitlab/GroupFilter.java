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

    /**
     * Do not include the provided groups IDs.
     *
     * @param skipGroups List of group IDs to not include in the search
     * @return the reference to this GroupFilter instance
     */
    public GroupFilter withSkipGroups(List<Integer> skipGroups) {
        this.skipGroups = skipGroups;
        return (this);  
    }

    /**
     * Show all the groups you have access to (defaults to false for authenticated users, true for admin).
     * Attributes owned and min_access_level have precedence
     *
     * @param allAvailable if true show all avauilable groups
     * @return the reference to this GroupFilter instance
     */
    public GroupFilter withAllAvailabley(Boolean allAvailable) {
        this.allAvailable = allAvailable;
        return (this);       
    }

    /**
     * Return list of groups matching the search criteria.
     *
     * @param search the search criteria
     * @return the reference to this GroupFilter instance
     */
    public GroupFilter withSearch(String search) {
        this.search = search;
        return (this);
    }



    /**
     * Include group statistics (admins only).
     *
     * @param statistics if true, return statistics with the results
     * @return the reference to this GroupFilter instance
     */
    public GroupFilter withStatistics(Boolean statistics) {
        this.statistics = statistics;
        return (this);
    }

    /**
     *  Include custom attributes in response (admins only).
     * 
     * @param withCustomAttributes if true, include custom attributes in the response
     * @return the reference to this GroupFilter instance
     */
    public GroupFilter withCustomAttributes(Boolean withCustomAttributes) {
        this.withCustomAttributes = withCustomAttributes;
        return (this);
    }

    /**
     * Limit by groups explicitly owned by the current user
     *
     * @param owned if true, limit to groups explicitly owned by the current user
     * @return the reference to this GroupFilter instance
     */
    public GroupFilter withOwned(Boolean owned) {
        this.owned = owned;
        return (this);
    }

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
