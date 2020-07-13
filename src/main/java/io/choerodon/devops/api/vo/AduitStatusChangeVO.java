package io.choerodon.devops.api.vo;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/7/13 14:25
 */
public class AduitStatusChangeVO {
    private Boolean auditStatusChanged;
    private String auditUserName;

    public Boolean getAuditStatusChanged() {
        return auditStatusChanged;
    }

    public void setAuditStatusChanged(Boolean auditStatusChanged) {
        this.auditStatusChanged = auditStatusChanged;
    }

    public String getAuditUserName() {
        return auditUserName;
    }

    public void setAuditUserName(String auditUserName) {
        this.auditUserName = auditUserName;
    }
}
