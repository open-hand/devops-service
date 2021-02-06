package io.choerodon.devops.api.vo;

/**
 * 〈功能简述〉
 * 〈删除节点校验VO〉
 *
 * @author wanghao
 * @since 2020/10/21 22:39
 */
public class NodeDeleteCheckVO {
    private Boolean enableDeleteMaster = true;
    private Boolean enableDeleteEtcd = true;
    private Boolean enableDeleteWorker = true;

    public Boolean getEnableDeleteMaster() {
        return enableDeleteMaster;
    }

    public void setEnableDeleteMaster(Boolean enableDeleteMaster) {
        this.enableDeleteMaster = enableDeleteMaster;
    }

    public Boolean getEnableDeleteEtcd() {
        return enableDeleteEtcd;
    }

    public void setEnableDeleteEtcd(Boolean enableDeleteEtcd) {
        this.enableDeleteEtcd = enableDeleteEtcd;
    }

    public Boolean getEnableDeleteWorker() {
        return enableDeleteWorker;
    }

    public void setEnableDeleteWorker(Boolean enableDeleteWorker) {
        this.enableDeleteWorker = enableDeleteWorker;
    }
}
