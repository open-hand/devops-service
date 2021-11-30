package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.validator.DevopsCiPipelineAdditionalValidator;
import io.choerodon.devops.api.vo.DevopsCiMavenBuildConfigVO;
import io.choerodon.devops.api.vo.MavenRepoVO;
import io.choerodon.devops.app.service.AbstractDevopsCiStepHandler;
import io.choerodon.devops.app.service.DevopsCiMavenBuildConfigService;
import io.choerodon.devops.infra.constant.GitOpsConstants;
import io.choerodon.devops.infra.dto.DevopsCiMavenSettingsDTO;
import io.choerodon.devops.infra.dto.DevopsCiStepDTO;
import io.choerodon.devops.infra.dto.maven.Repository;
import io.choerodon.devops.infra.dto.maven.RepositoryPolicy;
import io.choerodon.devops.infra.dto.maven.Server;
import io.choerodon.devops.infra.dto.repo.NexusMavenRepoDTO;
import io.choerodon.devops.infra.enums.DevopsCiStepTypeEnum;
import io.choerodon.devops.infra.feign.operator.RdupmClientOperator;
import io.choerodon.devops.infra.mapper.DevopsCiMavenSettingsMapper;
import io.choerodon.devops.infra.util.Base64Util;
import io.choerodon.devops.infra.util.GitlabCiUtil;
import io.choerodon.devops.infra.util.MapperUtil;
import io.choerodon.devops.infra.util.MavenSettingsUtil;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/11/29 17:52
 */
@Service
public class DevopsMavenBuildStepHandler extends AbstractDevopsCiStepHandler {

    private static final String ERROR_CI_MAVEN_REPOSITORY_TYPE = "error.ci.maven.repository.type";
    private static final String ERROR_CI_MAVEN_SETTINGS_INSERT = "error.maven.settings.insert";

    protected DevopsCiStepTypeEnum type = DevopsCiStepTypeEnum.MAVEN_BUILD;

    @Autowired
    private DevopsCiMavenBuildConfigService devopsCiMavenBuildConfigService;

    @Autowired
    private DevopsCiMavenSettingsMapper devopsCiMavenSettingsMapper;

    @Autowired
    private RdupmClientOperator rdupmClientOperator;

    @Override
    public List<String> buildGitlabCiScript(DevopsCiStepDTO devopsCiStepDTO) {
        Long projectId = devopsCiStepDTO.getProjectId();
        Long devopsCiJobId = devopsCiStepDTO.getDevopsCiJobId();
        // 处理settings文件
        DevopsCiMavenBuildConfigVO devopsCiMavenBuildConfigVO = devopsCiMavenBuildConfigService.queryById(devopsCiStepDTO.getConfigId());


        DevopsCiPipelineAdditionalValidator.validateMavenBuildStep(devopsCiMavenBuildConfigVO);
        boolean hasSettings = buildAndSaveMavenSettings(projectId,
                devopsCiJobId,
                devopsCiStepDTO.getSequence(),
                devopsCiMavenBuildConfigVO);

        return buildMavenScripts(projectId,
                devopsCiJobId,
                devopsCiStepDTO,
                devopsCiMavenBuildConfigVO,
                hasSettings);
    }

    /**
     * 生成并存储maven settings到数据库
     *
     * @param projectId          项目id
     * @param jobId              job id
     * @param devopsCiMavenBuildConfigVO 配置信息
     * @return true表示有settings配置，false表示没有
     */
    private boolean buildAndSaveMavenSettings(Long projectId, Long jobId, Long sequence, DevopsCiMavenBuildConfigVO devopsCiMavenBuildConfigVO) {
        // settings文件内容
        String settings;
        final List<MavenRepoVO> repos = new ArrayList<>();

        // 是否有手动填写仓库表单
        final boolean hasManualRepos = !CollectionUtils.isEmpty(devopsCiMavenBuildConfigVO.getRepos());
        // 是否有选择已有的maven仓库
        final boolean hasNexusRepos = !CollectionUtils.isEmpty(devopsCiMavenBuildConfigVO.getNexusMavenRepoIds());

        if (!StringUtils.isEmpty(devopsCiMavenBuildConfigVO.getMavenSettings())) {
            // 使用用户提供的xml内容，不进行内容的校验
            settings = Base64Util.getBase64DecodedString(devopsCiMavenBuildConfigVO.getMavenSettings());
        } else if (hasManualRepos || hasNexusRepos) {
            if (hasNexusRepos) {
                // 用户选择的已有的maven仓库
                List<NexusMavenRepoDTO> nexusMavenRepoDTOs = rdupmClientOperator.getRepoUserByProject(null, projectId, devopsCiMavenBuildConfigVO.getNexusMavenRepoIds());
                repos.addAll(nexusMavenRepoDTOs.stream().map(DevopsMavenBuildStepHandler::convertRepo).collect(Collectors.toList()));
            }

            if (hasManualRepos) {
                // 由用户填写的表单构建xml文件内容
                repos.addAll(devopsCiMavenBuildConfigVO.getRepos());
            }

            // 构建settings文件
            settings = buildSettings(repos);
        } else {
            // 没有填关于settings的信息
            return false;
        }

        // 这里存储的ci setting文件内容是解密后的
        DevopsCiMavenSettingsDTO devopsCiMavenSettingsDTO = new DevopsCiMavenSettingsDTO(jobId, sequence, settings);
        MapperUtil.resultJudgedInsert(devopsCiMavenSettingsMapper, devopsCiMavenSettingsDTO, ERROR_CI_MAVEN_SETTINGS_INSERT);
        return true;
    }

    /**
     * 生成maven构建相关的脚本
     *
     * @param projectId          项目id
     * @param jobId              job id
     * @param devopsCiStepDTO
     * @param devopsCiMavenBuildConfigVO maven构建阶段的信息
     * @param hasSettings        这个阶段是否有配置settings
     * @return 生成的shell脚本
     */
    private List<String> buildMavenScripts(final Long projectId, final Long jobId, DevopsCiStepDTO devopsCiStepDTO, DevopsCiMavenBuildConfigVO devopsCiMavenBuildConfigVO, boolean hasSettings) {
        List<String> shells = GitlabCiUtil.filterLines(GitlabCiUtil.splitLinesForShell(devopsCiStepDTO.getScript()), true, true);
        if (hasSettings) {
            // 插入shell指令将配置的settings文件下载到项目目录下
            shells.add(0, GitlabCiUtil.downloadMavenSettings(projectId, jobId, devopsCiStepDTO.getSequence()));
        }
        return shells;
    }

    private static MavenRepoVO convertRepo(NexusMavenRepoDTO nexusMavenRepoDTO) {
        MavenRepoVO mavenRepoVO = new MavenRepoVO();
        mavenRepoVO.setName(nexusMavenRepoDTO.getName());
        mavenRepoVO.setPrivateRepo(Boolean.TRUE);
        if ("MIXED".equals(nexusMavenRepoDTO.getVersionPolicy())) {
            mavenRepoVO.setType(GitOpsConstants.SNAPSHOT + "," + GitOpsConstants.RELEASE);
        } else {
            // group 类型的仓库没有版本类型
            mavenRepoVO.setType(nexusMavenRepoDTO.getVersionPolicy() == null ? null : nexusMavenRepoDTO.getVersionPolicy().toLowerCase());
        }
        mavenRepoVO.setUrl(nexusMavenRepoDTO.getUrl());
        mavenRepoVO.setUsername(nexusMavenRepoDTO.getNeUserId());
        mavenRepoVO.setPassword(nexusMavenRepoDTO.getNeUserPassword());
        return mavenRepoVO;
    }

    private static String buildSettings(List<MavenRepoVO> mavenRepoList) {
        List<Server> servers = new ArrayList<>();
        List<Repository> repositories = new ArrayList<>();

        mavenRepoList.forEach(m -> {
            if (m.getType() != null) {
                String[] types = m.getType().split(GitOpsConstants.COMMA);
                if (types.length > 2) {
                    throw new CommonException(ERROR_CI_MAVEN_REPOSITORY_TYPE, m.getType());
                }
            }
            if (Boolean.TRUE.equals(m.getPrivateRepo())) {
                servers.add(new Server(Objects.requireNonNull(m.getName()), Objects.requireNonNull(m.getUsername()), Objects.requireNonNull(m.getPassword())));
            }
            repositories.add(new Repository(
                    Objects.requireNonNull(m.getName()),
                    Objects.requireNonNull(m.getName()),
                    Objects.requireNonNull(m.getUrl()),
                    m.getType() == null ? null : new RepositoryPolicy(m.getType().contains(GitOpsConstants.RELEASE)),
                    m.getType() == null ? null : new RepositoryPolicy(m.getType().contains(GitOpsConstants.SNAPSHOT))));
        });
        return MavenSettingsUtil.generateMavenSettings(servers, repositories);
    }

}
