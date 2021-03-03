package io.choerodon.devops.infra.constant;

import static org.hzero.core.base.BaseConstants.Symbol.SLASH;

import org.hzero.core.util.StringPool;

/**
 * @author lihao
 * 集群相关操作命令
 */
public class DevopsClusterCommandConstants {
    /**
     * 密码模式 节点名称 主机 端口 用户 登录密码
     */
    public static final String INVENTORY_INI_TEMPLATE_FOR_ALL_PASSWORD_TYPE = "%s ansible_host=%s ansible_port=%s ansible_user=%s ansible_ssh_pass=%s";

    /**
     * 密钥模式 节点名称 主机 端口 用户 密钥文件路径
     */
    public static final String INVENTORY_INI_TEMPLATE_FOR_ALL_PRIVATE_KEY_TYPE = "%s ansible_host=%s ansible_port=%s ansible_user=%s ansible_ssh_private_key_file=%s";

    /**
     * 保存密钥命令 密钥内容 密钥保存地址
     */
    public static final String SAVE_PRIVATE_KEY_TEMPLATE = "echo \"%s\" > %s";


    /**
     * docker安装命令模板
     */
    public static final String INSTALL_DOCKER_COMMAND = "curl -fsSL https://get.docker.com/ | bash -s docker --mirror Aliyun >> /tmp/install-docker.log 2>&1 \n" +
            "sudo systemctl restart docker >> /tmp/install-docker.log 2>&1 && sudo systemctl enable docker >> /tmp/install-docker.log 2>&1 ";

    public static final String RESTART_DOCKER_PROGRESS = "sudo systemctl restart docker >> /tmp/restart-docker.log 2>&1 && sudo systemctl enable docker >> /tmp/restart-docker.log 2>&1 ";

    /**
     * ansible命令模板，需要指定执行的yml
     */
    public static final String ANSIBLE_COMMAND_TEMPLATE = "cd /tmp/kubeadm-ha && ansible-playbook -i /tmp/inventory.ini %s";

    /**
     * 获取指定目录内容
     */
    public static final String CAT_FILE = "cat %s";

    /**
     * 删除文件或者目录
     */
    public static final String DELETE_FILE = "rm -rf %s";

    /**
     * 添加worker节点
     */
    public static final String ADD_WORKER_YML = "81-add-worker.yml";
    /**
     * 添加master节点
     */
    public static final String ADD_MASTER_YML = "82-add-master.yml";

    /**
     * 命令后台执行模板，需要指定具体命令以及exitCode保存位置
     * 第一个%s： 需要执行的命令
     * 第二个%s： 命令的日志输出
     */
    public static final String BACKGROUND_COMMAND_TEMPLATE = "nohup bash %s > %s 2>&1 &";

    /**
     * 命令执行模板，需要指定具体命令以及exitCode保存位置
     * 第一个%s： 需要执行的命令
     * 第二个%s： 命令的日志输出
     */
    public static final String BASH_COMMAND_TEMPLATE = "bash %s";

    /**
     * 安装helm模版, curl -L -o helm.tar.gz 文件下载地址
     */
    public static final String INSTALL_HELM_TEMPLATE = "curl -L -o helm.tar.gz %s && tar -zxvf helm.tar.gz && sudo mv linux-amd64/helm /usr/bin/helm";

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
    /**
     * 添加节点
     */
    public static final String ADD_NODE = "add_node";
    /**
     * 删除节点
     */
    public static final String DELETE_NODE = "delete_node";
    /**
     * 删除节点角色
     */
    public static final String DELETE_NODE_ROLE = "delete_node_role";

    /**
     * devops中ansible文件保存目录模板
     */
    public static final String ANSIBLE_CONFIG_BASE_DIR_TEMPLATE = "/choerodon/ansible/%s";

    /**
     * 基准目录
     */
    public static final String BASE_DIR = "/tmp";

    /**
     * 密钥保存位置模版
     */
    public static final String PRIVATE_KEY_SAVE_PATH_TEMPLATE = "/tmp/ansible/ssh-key/id_rsa-%s";

    /**
     * 执行状态码保存位置
     */
    public static final String EXIT_CODE_FILE_TEMPLATE = BASE_DIR + SLASH + "exit-code-%s";

    /**
     * k8s安装日志文件
     */
    public static final String INSTALL_K8S_LOG = BASE_DIR + StringPool.SLASH + "install.log";

    /**
     * pre-kubeadm-ha.sh文件路径
     */
    public static final String PRE_KUBEADM_HA_SH = BASE_DIR + StringPool.SLASH + "pre-kubeadm-ha.sh";

    /**
     * 安装k8s的shell文件
     */
    public static final String INSTALL_K8S_SHELL = BASE_DIR + StringPool.SLASH + INSTALL_K8S;

    /**
     * bash命令日志输出文件
     */
    public static final String BASH_LOG_OUTPUT = "/tmp/bash.log";
}
