package io.choerodon.devops.api.vo;

/**
 * 〈功能简述〉
 * 〈节点角色删除校验VO〉
 *
 * @author wanghao
 * @since 2020/10/21 22:30
 */
public class NodeRoleDeleteCheckVO {
    private Boolean enableDeleteRole = false;
    private Boolean enableDeleteMasterRole = false;
    private Boolean enableDeleteEtcdRole = false;

    public Boolean getEnableDeleteRole() {
        return enableDeleteRole;
    }

    public void setEnableDeleteRole(Boolean enableDeleteRole) {
        this.enableDeleteRole = enableDeleteRole;
    }

    public Boolean getEnableDeleteMasterRole() {
        return enableDeleteMasterRole;
    }

    public void setEnableDeleteMasterRole(Boolean enableDeleteMasterRole) {
        this.enableDeleteMasterRole = enableDeleteMasterRole;
    }

    public Boolean getEnableDeleteEtcdRole() {
        return enableDeleteEtcdRole;
    }

    public void setEnableDeleteEtcdRole(Boolean enableDeleteEtcdRole) {
        this.enableDeleteEtcdRole = enableDeleteEtcdRole;
    }
}
