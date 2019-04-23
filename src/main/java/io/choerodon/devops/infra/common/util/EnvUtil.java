package io.choerodon.devops.infra.common.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.GitConfigDTO;
import io.choerodon.devops.api.dto.GitEnvConfigDTO;
import io.choerodon.devops.domain.application.entity.DevopsEnvironmentE;
import io.choerodon.devops.domain.application.entity.ProjectE;
import io.choerodon.devops.domain.application.repository.DevopsEnvironmentRepository;
import io.choerodon.devops.domain.application.repository.IamRepository;
import io.choerodon.devops.domain.application.valueobject.Organization;
import io.choerodon.websocket.helper.EnvListener;
import io.choerodon.websocket.helper.EnvSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * Creator: Runge
 * Date: 2018/6/1
 * Time: 15:47
 * Description:
 */

@Service
public class EnvUtil {

    Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
    @Value("${agent.version}")
    private String agentExpectVersion;
    @Value("${services.gitlab.sshUrl}")
    private String gitlabSshUrl;

    @Autowired
    private IamRepository iamRepository;
    @Autowired
    private GitUtil gitUtil;
    @Autowired(required = false)
    @Lazy
    private EnvListener envListener;


    public static int compareVersion(String a, String b) {
        if (!a.contains("-") && !b.contains("-")) {
            return compareTag(a, b);
        } else if (a.contains("-") && b.contains("-")) {
            String[] a1 = a.split("-");
            String[] b1 = b.split("-");
            int compareResult = compareTag(a1[0], b1[0]);
            if (compareResult == 0) {
                if (TypeUtil.objToLong(b1[1]) > TypeUtil.objToLong(a1[1])) {
                    return 1;
                } else if (TypeUtil.objToLong(b1[1]) < TypeUtil.objToLong(a1[1])) {
                    return -1;
                } else {
                    return 0;
                }
            } else {
                return compareResult;
            }
        }
        return 1;
    }

    public static int compareTag(String a, String b) {
        String[] a1 = a.split("\\.");
        String[] b1 = b.split("\\.");
        if (TypeUtil.objToLong(b1[0]) > TypeUtil.objToLong(a1[0])) {
            return 1;
        } else if (TypeUtil.objToLong(b1[0]) < TypeUtil.objToLong(a1[0])) {
            return -1;
        } else {
            if (TypeUtil.objToLong(b1[1]) > TypeUtil.objToLong(a1[1])) {
                return 1;
            } else if (TypeUtil.objToLong(b1[1]) < TypeUtil.objToLong(a1[1])) {
                return -1;
            } else {
                if (TypeUtil.objToLong(b1[2]) > TypeUtil.objToLong(a1[2])) {
                    return 1;
                } else if (TypeUtil.objToLong(b1[2]) < TypeUtil.objToLong(a1[2])) {
                    return -1;
                } else {
                    return 0;
                }
            }
        }
    }

    /**
     * 检查环境是否链接
     *
     * @param clusterId 环境ID
     */
    public void checkEnvConnection(Long clusterId) {
        Map<String, EnvSession> connectedEnv = envListener.connectedEnv();
        boolean envConnected = connectedEnv.entrySet().stream()
                .anyMatch(t -> clusterId.equals(t.getValue().getClusterId())
                        && compareVersion(t.getValue().getVersion() == null ? "0" : t.getValue().getVersion(), agentExpectVersion) != 1);
        if (!envConnected) {
            throw new CommonException("error.env.disconnect");
        }
    }

    /**
     * 环境链接列表
     *
     * @return 环境链接列表
     */
    public List<Long> getConnectedEnvList() {
        Map<String, EnvSession> connectedEnv = envListener.connectedEnv();
        return connectedEnv.entrySet().stream()
                .map(t -> t.getValue().getClusterId())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * 环境更新列表
     *
     * @return 环境更新列表
     */
    public List<Long> getUpdatedEnvList() {
        Map<String, EnvSession> connectedEnv = envListener.connectedEnv();
        return connectedEnv.entrySet().stream()
                .filter(t -> compareVersion(t.getValue().getVersion() == null ? "0" : t.getValue().getVersion(), agentExpectVersion) != 1)
                .map(t -> t.getValue().getClusterId())
                .collect(Collectors.toCollection(ArrayList::new));
    }


    public String handDevopsEnvGitRepository(DevopsEnvironmentE devopsEnvironmentE) {
        ProjectE projectE = iamRepository.queryIamProject(devopsEnvironmentE.getProjectE().getId());
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        //本地路径
        String path = String.format("gitops/%s/%s/%s",
                organization.getCode(), projectE.getCode(), devopsEnvironmentE.getCode());
        //生成环境git仓库ssh地址
        String url = GitUtil.getGitlabSshUrl(pattern, gitlabSshUrl, organization.getCode(),
                projectE.getCode(), devopsEnvironmentE.getCode());

        File file = new File(path);
        gitUtil.setSshKey(devopsEnvironmentE.getEnvIdRsa());
        if (!file.exists()) {
            gitUtil.cloneBySsh(path, url);
        }
        return path;
    }
}
