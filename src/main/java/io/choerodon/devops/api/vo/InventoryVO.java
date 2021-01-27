package io.choerodon.devops.api.vo;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/10/22 15:32
 */
public class InventoryVO {
    StringBuilder all = new StringBuilder();
    StringBuilder kubeMaster = new StringBuilder();
    StringBuilder kubeWorker = new StringBuilder();
    StringBuilder etcd = new StringBuilder();
    StringBuilder newMaster = new StringBuilder();
    StringBuilder newEtcd = new StringBuilder();
    StringBuilder newWorker = new StringBuilder();
    StringBuilder delMaster = new StringBuilder();
    StringBuilder delEtcd = new StringBuilder();
    StringBuilder delWorker = new StringBuilder();
    StringBuilder delNode = new StringBuilder();

    public StringBuilder getAll() {
        return all;
    }

    public void setAll(StringBuilder all) {
        this.all = all;
    }

    public StringBuilder getKubeMaster() {
        return kubeMaster;
    }

    public void setKubeMaster(StringBuilder kubeMaster) {
        this.kubeMaster = kubeMaster;
    }

    public StringBuilder getKubeWorker() {
        return kubeWorker;
    }

    public void setKubeWorker(StringBuilder kubeWorker) {
        this.kubeWorker = kubeWorker;
    }

    public StringBuilder getEtcd() {
        return etcd;
    }

    public void setEtcd(StringBuilder etcd) {
        this.etcd = etcd;
    }

    public StringBuilder getNewMaster() {
        return newMaster;
    }

    public void setNewMaster(StringBuilder newMaster) {
        this.newMaster = newMaster;
    }

    public StringBuilder getNewEtcd() {
        return newEtcd;
    }

    public void setNewEtcd(StringBuilder newEtcd) {
        this.newEtcd = newEtcd;
    }

    public StringBuilder getNewWorker() {
        return newWorker;
    }

    public void setNewWorker(StringBuilder newWorker) {
        this.newWorker = newWorker;
    }

    public StringBuilder getDelMaster() {
        return delMaster;
    }

    public void setDelMaster(StringBuilder delMaster) {
        this.delMaster = delMaster;
    }

    public StringBuilder getDelEtcd() {
        return delEtcd;
    }

    public void setDelEtcd(StringBuilder delEtcd) {
        this.delEtcd = delEtcd;
    }

    public StringBuilder getDelWorker() {
        return delWorker;
    }

    public void setDelWorker(StringBuilder delWorker) {
        this.delWorker = delWorker;
    }

    public StringBuilder getDelNode() {
        return delNode;
    }

    public void setDelNode(StringBuilder delNode) {
        this.delNode = delNode;
    }
}
