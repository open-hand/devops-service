package io.choerodon.devops.infra.handler;

import static io.choerodon.devops.infra.constant.DevOpsWebSocketConstants.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.ClusterSessionVO;
import io.choerodon.devops.app.service.DevopsClusterService;
import io.choerodon.devops.infra.dto.DevopsClusterDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.dto.iam.Tenant;
import io.choerodon.devops.infra.enums.EnvironmentType;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.util.*;

/**
 * Creator: Runge
 * Date: 2018/6/1
 * Time: 15:47
 * Description:
 */

@Service
public class ClusterConnectionHandler {
    private static final String CLUSTER_ID = "clusterId";
    public static final String CLUSTER_SESSION = "cluster-sessions-cache";
    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterConnectionHandler.class);

    private Pattern pattern = Pattern.compile("^[-+]?[\\d]*$");
    @Value("${local.test:false}")
    private Boolean localTest;

    @Value("${agent.version}")
    private String agentExpectVersion;
    @Value("${services.gitlab.sshUrl}")
    private String gitlabSshUrl;
    @Value("${services.gitlab.internalsshUrl:}")
    private String gitlabInternalsshUrl;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private GitUtil gitUtil;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private DevopsClusterService devopsClusterService;

    /**
     * 检查集群的环境是否链接
     *
     * @param clusterId 环境ID
     */
    public void checkEnvConnection(Long clusterId) {
        // 这里加判断为方便本地调试，没有其他用途
        if (localTest) {
            return;
        }
        if (!getEnvConnectionStatus(clusterId)) {
            throw new CommonException("devops.env.disconnect");
        }
    }

    /**
     * 检查集群的环境是否链接
     *
     * @param clusterId 环境ID
     * @return true 表示已连接
     */
    private boolean getEnvConnectionStatus(Long clusterId) {
        Map<String, ClusterSessionVO> clusterSessions = (Map<String, ClusterSessionVO>) (Map) redisTemplate.opsForHash().entries(CLUSTER_SESSION);
        return clusterSessions.values().stream()
                .anyMatch(t -> clusterId.equals(t.getClusterId())
                        && agentExpectVersion.equals(t.getVersion()));
    }

    /**
     * 不需要进行升级的已连接的集群 up-to-date
     * 版本相等就认为不需要升级
     *
     * @return 环境更新列表
     */
    public List<Long> getUpdatedClusterList() {
        Map<String, ClusterSessionVO> clusterSessions = (Map<String, ClusterSessionVO>) (Map) redisTemplate.opsForHash().entries(CLUSTER_SESSION);
        return clusterSessions.values().stream()
                .filter(clusterSessionVO -> agentExpectVersion.equals(clusterSessionVO.getVersion()))
                .map(ClusterSessionVO::getClusterId)
                .collect(Collectors.toCollection(ArrayList::new));
    }


    public String handDevopsEnvGitRepository(Long projectId, String envCode, Long envId, String envRsa, String envType, String clusterCode) {
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);
        Tenant organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
        //本地路径
        String path = GitOpsUtil.getLocalPathToStoreEnv(organizationDTO.getTenantNum(), projectDTO.getDevopsComponentCode(), clusterCode, envCode, envId);
        //生成环境git仓库ssh地址
        String url = GitUtil.getGitlabSshUrl(pattern, gitUtil.getSshUrl(), organizationDTO.getTenantNum(),
                projectDTO.getDevopsComponentCode(), envCode, EnvironmentType.forValue(envType), clusterCode);
        // 相同目录下，git并发拉取会有问题，所以对目录加锁
        synchronized (path.intern()) {
            File file = new File(path);
            if (!file.exists()) {
                gitUtil.cloneBySsh(path, url, envRsa);
            } else {
                String localPath = String.format("%s%s", path, "/.git");
                // 如果文件夾存在并且文件夹不为空,去拉取新的配置
                // 反之克隆远程的仓库的文件
                if (file.isDirectory() && file.listFiles().length > 0) {
                    try {
                        gitUtil.pullBySsh(localPath, envRsa);
                    } catch (Exception e) {
                        // 有时本地文件和远端gitops库文件冲突可能导致pull 代码库失败，所以添加以下补偿逻辑
                        if (e instanceof CheckoutConflictException) {
                            // 删除本地gitops文件，然后重新clone
                            FileUtil.deleteDirectory(file);
                            gitUtil.cloneBySsh(path, url, envRsa);
                        } else {
                            throw new CommonException("devops.git.pull", e);
                        }
                    }
                } else {
                    gitUtil.cloneBySsh(path, url, envRsa);
                }
            }
        }

        return path;
    }

    /**
     * 校验ws连接参数是否正确
     *
     * @param request 请求
     * @return true表示正确，false表示不正确
     */
    public boolean validConnectionParameter(HttpServletRequest request) {
        //校验ws连接参数是否正确
        String key = request.getParameter(KEY);
        String clusterId = request.getParameter(CLUSTER_ID);
        String token = request.getParameter(TOKEN);
        String version = request.getParameter(VERSION);

        if (key == null || key.trim().isEmpty()) {
            LOGGER.warn("Agent Handshake : Key is null");
            return false;
        }
        if (!KeyParseUtil.matchPattern(key)) {
            LOGGER.warn("Agent Handshake : Key not match the pattern");
            return false;
        }
        if (clusterId == null || clusterId.trim().isEmpty()) {
            LOGGER.warn("Agent Handshake : ClusterId is null");
            return false;
        }
        if (token == null || token.trim().isEmpty()) {
            LOGGER.warn("Agent Handshake : Token is null");
            return false;
        }
        if (version == null || version.trim().isEmpty()) {
            LOGGER.warn("Agent Handshake : Version is null");
            return false;
        }
        //检验连接过来的agent和集群是否匹配
        DevopsClusterDTO devopsClusterDTO = devopsClusterService.baseQuery(TypeUtil.objToLong(clusterId));
        if (devopsClusterDTO == null) {
            LogUtil.loggerWarnObjectNullWithId("Cluster", TypeUtil.objToLong(clusterId), LOGGER);
            return false;
        }
        if (!token.equals(devopsClusterDTO.getToken())) {
            LOGGER.warn("Cluster with id {} exists but its token doesn't match the token that agent offers as {}", clusterId, token);
            return false;
        }

        return true;
    }

    /**
     * 对0.21.x版本的agent校验参数
     *
     * @param request 请求
     * @return true表示校验通过
     */
    public boolean validElderAgentGitOpsParameters(HttpServletRequest request) {
        //校验ws连接参数是否正确
        String key = request.getParameter("key");
        String clusterId = request.getParameter(CLUSTER_ID);
        String token = request.getParameter("token");
        String version = request.getParameter("version");
        if (key == null || key.trim().isEmpty()) {
            LOGGER.warn("Agent Handshake : Key is null");
            return false;
        }
        if (!KeyParseUtil.matchPattern(key)) {
            LOGGER.warn("Agent Handshake : Key not match the pattern");
            return false;
        }
        if (clusterId == null || clusterId.trim().isEmpty()) {
            LOGGER.warn("Agent Handshake : ClusterId is null");
            return false;
        }
        if (token == null || token.trim().isEmpty()) {
            LOGGER.warn("Agent Handshake : Token is null");
            return false;
        }
        if (version == null || version.trim().isEmpty()) {
            LOGGER.warn("Agent Handshake : Version is null");
            return false;
        }
        //检验连接过来的agent和集群是否匹配
        DevopsClusterDTO devopsClusterDTO = devopsClusterService.baseQuery(TypeUtil.objToLong(clusterId));
        if (devopsClusterDTO == null) {
            LogUtil.loggerWarnObjectNullWithId("Cluster", TypeUtil.objToLong(clusterId), LOGGER);
            return false;
        }
        if (!token.equals(devopsClusterDTO.getToken())) {
            LOGGER.warn("Cluster with id {} exists but its token doesn't match the token that agent offers as {}", clusterId, token);
            return false;
        }

        return true;
    }
}
