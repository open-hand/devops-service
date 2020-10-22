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

    public static final String REMOVE_NODE_YAML = "87-remove-node.yml";
}
