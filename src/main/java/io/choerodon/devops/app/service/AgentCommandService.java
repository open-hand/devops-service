package io.choerodon.devops.app.service;

import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

import io.choerodon.devops.api.vo.ConfigVO;
import io.choerodon.devops.api.vo.DescribeResourceVO;
import io.choerodon.devops.api.vo.PipeRequestVO;
import io.choerodon.devops.api.vo.kubernetes.Command;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.AppServiceVersionDTO;
import io.choerodon.devops.infra.dto.DevopsClusterDTO;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;


/**
 * Created by younger on 2018/4/18.
 */
public interface AgentCommandService {
    void sendCommand(DevopsEnvironmentDTO devopsEnvironmentDTO);

    void deploy(AppServiceDTO applicationDTO, AppServiceVersionDTO appServiceVersionDTO,
                String releaseName, DevopsEnvironmentDTO devopsEnvironmentDTO, String values,
                Long commandId, String secretCode);

    void initCluster(Long clusterId, WebSocketSession webSocketSession);

    void deleteEnv(Long envId, String code, Long clusterId);

    void initEnv(DevopsEnvironmentDTO devopsEnvironmentDTO, Long clusterId);

    void deployTestApp(AppServiceDTO applicationDTO, AppServiceVersionDTO appServiceVersionDTO, String releaseName, String secretName, Long clusterId, String values);

    void getTestAppStatus(Map<Long, List<String>> testReleases);

    void upgradeCluster(DevopsClusterDTO devopsClusterDTO, WebSocketSession webSocketSession);

    void newUpgradeCluster(DevopsClusterDTO devopsClusterDTO, WebSocketSession webSocketSession);

    /**
     * 安装CertManager
     *
     * @param certManagerRepoUrl     CertManager的chart仓库的地址
     * @param clusterId              集群id
     * @param certManagerReleaseName CertManager的实例名称
     * @param namespace              CertManager安装的namespace
     * @param chartVersion           CertManager要安装的版本
     */
    void installCertManager(String certManagerRepoUrl, Long clusterId, String certManagerReleaseName, String namespace, String chartVersion);

    void operatePodCount(String kind, String name, String namespace, Long clusterId, Long count, String commandId);

    void deleteHelmHookJob(String name, String namespace, Long clusterId);

    void operateSecret(Long clusterId, String namespace, String secretName, ConfigVO configVO);

    void gitopsSyncCommandStatus(Long clusterId, String envCode, Long envId, List<Command> commands);

    void startOrStopInstance(String payload, String name,
                             String type, String namespace,
                             Long commandId, Long envId,
                             Long clusterId);

    /**
     * 通过GitOps连接通知agent发起一个新的ws连接用于日志或者exec命令
     *
     * @param type        这个消息的类型
     * @param key         形如: cluster:123.log:ad122451ea
     * @param pipeRequest 参数
     * @param clusterId   集群id
     */
    void startLogOrExecConnection(String type, String key, PipeRequestVO pipeRequest, Long clusterId);

    /**
     * 通过GitOps连接通知agent发起一个新的ws连接用于describe命令
     *
     * @param key                形如: cluster:123.log:ad122451ea
     * @param describeResourceVO 参数
     * @param clusterId          集群id
     */
    void startDescribeConnection(String key, DescribeResourceVO describeResourceVO, Long clusterId);

    void deletePod(String podName, String namespace, Long clusterId);

    /**
     * 卸载 CertManager
     *
     * @param clusterId              集群id
     * @param certManagerReleaseName CertManager的实例名称
     * @param namespace              CertManager所在的namespace
     */
    void unloadCertManager(Long clusterId, String certManagerReleaseName, String namespace);

    /**
     * polaris扫描集群或集群某个namespace
     *
     * @param clusterId 集群id
     * @param recordId  关联的扫描纪录id
     * @param namespace namespace
     *                  为null时表示扫描整个集群所有的namespace，
     *                  有值时表示扫描一个指定的namespace
     */
    void scanCluster(Long clusterId, Long recordId, @Nullable String namespace);

    /**
     * 发送chartMuseum的认证信息(包含url, 用户名密码)
     *
     * @param clusterId 集群id
     * @param configVO  配置信息
     */
    void sendChartMuseumAuthentication(Long clusterId, ConfigVO configVO);
}
