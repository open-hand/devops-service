package io.choerodon.devops.infra.constant;

/**
 * @author lihao
 * 集群相关操作命令
 */
public class DevopsClusterCommandConstants {

    public static final String DOCKER_INSTALL_COMMAND = "curl -fsSL https://get.docker.com/ | bash -s docker --mirror Aliyun >> /tmp/check.log\n" +
            "systemctl restart docker && systemctl enable docker >> /tmp/check.log";

    /**
     * ansible命令模板，需要指定执行的yml
     */
    public static final String ANSIBLE_COMMAND_TEMPLATE = "docker run --name ansible \\\n" +
            "-v /tmp/inventory.ini:/tmp/inventory.ini\n" +
            "registry.cn-shanghai.aliyuncs.com/kubeadm-ha/setzero_ansible:2.8.5-nginx-1.17.6-alpine \\\n" +
            "ansible-playbook -i /tmp/inventory.ini %s > /tmp/ansible.log";

    public static final String REMOVE_MASTER_YAML = "85-remove-master.yml";
    public static final String REMOVE_ETCD_YAML = "86-remove-etcd.yml";
    public static final String REMOVE_NODE_YAML = "87-remove-node.yml";
    /**
     * 获取检查日志
     */
    public static final String GET_LOG = "cat /tmp/check.log";

    /**
     * 检查节点
     */
    public static final String CHECK_NODE = "01-base.yml";

    /**
     * 安装集群
     */
    public static final String INSTALL_K8S="90-init-cluster.yml";
}
