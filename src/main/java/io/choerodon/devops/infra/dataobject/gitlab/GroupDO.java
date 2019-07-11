package io.choerodon.devops.infra.dataobject.gitlab;

import io.choerodon.devops.infra.enums.Visibility;

/**
 * Created by younger on 2018/4/2.
 */
public class GroupDO {
    private Integer id;
    private String name;
    private String path;
    private String description;
    private Boolean membershipLock;
    private Boolean shareWithGroupLock;
    private Visibility visibility;
    private Boolean lfsEnabled;
    private Boolean requestAccessEnabled;
    private Integer parentId;
    private Integer sharedRunnersMinutesLimit;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getMembershipLock() {
        return membershipLock;
    }

    public void setMembershipLock(Boolean membershipLock) {
        this.membershipLock = membershipLock;
    }

    public Boolean getShareWithGroupLock() {
        return shareWithGroupLock;
    }

    public void setShareWithGroupLock(Boolean shareWithGroupLock) {
        this.shareWithGroupLock = shareWithGroupLock;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public Boolean getLfsEnabled() {
        return lfsEnabled;
    }

    public void setLfsEnabled(Boolean lfsEnabled) {
        this.lfsEnabled = lfsEnabled;
    }

    public Boolean getRequestAccessEnabled() {
        return requestAccessEnabled;
    }

    public void setRequestAccessEnabled(Boolean requestAccessEnabled) {
        this.requestAccessEnabled = requestAccessEnabled;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public Integer getSharedRunnersMinutesLimit() {
        return sharedRunnersMinutesLimit;
    }

    public void setSharedRunnersMinutesLimit(Integer sharedRunnersMinutesLimit) {
        this.sharedRunnersMinutesLimit = sharedRunnersMinutesLimit;
    }
}
