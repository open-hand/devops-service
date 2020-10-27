package io.choerodon.devops.infra.constant;

/**
 * @author lihao
 * 集群相关操作命令
 */
public class DevopsClusterCommandConstants {
    /**
     * devops中ansible文件保存目录模板
     */
    public static final String ANSIBLE_CONFIG_BASE_DIR_TEMPLATE = "/Users/lihao/ansible/%s";
    /**
     * 节点中ansible文件保存目录
     */
    public static final String ANSIBLE_CONFIG_TARGET_BASE_DIR = "/tmp/";

    /**
     * docker安装命令模板
     */
    public static final String INSTALL_DOCKER_COMMAND= "curl -fsSL https://get.docker.com/ | bash -s docker --mirror Aliyun\n" +
            "echo =====================install completed\n" +
            "systemctl restart docker && systemctl enable docker";

    /**
     * ansible命令模板，需要指定执行的yml
     */
    public static final String ANSIBLE_COMMAND_TEMPLATE = "docker run --rm --name ansible \\\n" +
            "-w /root/kubeadm-ha/ \\\n" +
            "-v /tmp/inventory.ini:/tmp/inventory.ini \\\n" +
            "registry.cn-hangzhou.aliyuncs.com/elem-lihao/ansible:1.1 \\\n" +
            "ansible-playbook -i /tmp/inventory.ini %s";

    /**
     * 移除master节点
     */
    public static final String REMOVE_MASTER_YAML = "85-remove-master.yml";
    /**
     * 移除etcd节点
     */
    public static final String REMOVE_ETCD_YAML = "86-remove-etcd.yml";
    /**
     * 移除节点
     */
    public static final String REMOVE_NODE_YAML = "87-remove-node.yml";
    /**
     * 获取检查日志
     */
    public static final String GET_LOG = "cat %s";

    /**
     * 检查节点
     */
    public static final String CHECK_NODE = "01-base.yml";

    /**
     * 安装集群
     */
    public static final String INSTALL_K8S = "90-init-cluster.yml";

    /**
     * 配置检查
     */
    public static final String VARIABLE = "01.1-variables.yml";

    /**
     * 节点系统检查
     */
    public static final String SYSTEM = "01.2-system.yml";
    /**
     * CPU检查
     */
    public static final String CPU = "01.3-cpu.yml";
    /**
     * 内存检查
     */
    public static final String MEMORY = "01.4-memory.yml";
}
