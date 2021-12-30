package io.choerodon.devops.app.eventhandler.pipeline.step;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.validator.DevopsCiPipelineAdditionalValidator;
import io.choerodon.devops.api.vo.DevopsCiMavenBuildConfigVO;
import io.choerodon.devops.api.vo.DevopsCiStepVO;
import io.choerodon.devops.api.vo.MavenRepoVO;
import io.choerodon.devops.api.vo.template.CiTemplateStepVO;
import io.choerodon.devops.app.service.CiTemplateMavenBuildService;
import io.choerodon.devops.app.service.DevopsCiMavenBuildConfigService;
import io.choerodon.devops.infra.constant.GitOpsConstants;
import io.choerodon.devops.infra.dto.CiTemplateMavenBuildDTO;
import io.choerodon.devops.infra.dto.DevopsCiMavenBuildConfigDTO;
import io.choerodon.devops.infra.dto.DevopsCiMavenSettingsDTO;
import io.choerodon.devops.infra.dto.DevopsCiStepDTO;
import io.choerodon.devops.infra.dto.maven.Repository;
import io.choerodon.devops.infra.dto.maven.RepositoryPolicy;
import io.choerodon.devops.infra.dto.maven.Server;
import io.choerodon.devops.infra.dto.repo.NexusMavenRepoDTO;
import io.choerodon.devops.infra.enums.DevopsCiStepTypeEnum;
import io.choerodon.devops.infra.feign.operator.RdupmClientOperator;
import io.choerodon.devops.infra.mapper.DevopsCiMavenSettingsMapper;
import io.choerodon.devops.infra.util.*;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/11/29 17:52
 */
@Service
public class DevopsCiMavenBuildStepHandler extends AbstractDevopsCiStepHandler {

    private static final String ERROR_CI_MAVEN_REPOSITORY_TYPE = "error.ci.maven.repository.type";
    private static final String ERROR_CI_MAVEN_SETTINGS_INSERT = "error.maven.settings.insert";


    @Autowired
    private DevopsCiMavenBuildConfigService devopsCiMavenBuildConfigService;
    @Autowired
    private CiTemplateMavenBuildService ciTemplateMavenBuildService;

    @Autowired
    private DevopsCiMavenSettingsMapper devopsCiMavenSettingsMapper;

    @Autowired
    private RdupmClientOperator rdupmClientOperator;

    @Override
    @Transactional
    public void saveConfig(Long stepId, DevopsCiStepVO devopsCiStepVO) {
        DevopsCiMavenBuildConfigVO mavenBuildConfig = devopsCiStepVO.getMavenBuildConfig();
        if (mavenBuildConfig != null) {
            DevopsCiMavenBuildConfigDTO devopsCiMavenBuildConfigDTO = voToDto(mavenBuildConfig);
            devopsCiMavenBuildConfigDTO.setStepId(stepId);
            devopsCiMavenBuildConfigDTO.setId(null);
            devopsCiMavenBuildConfigService.baseCreate(devopsCiMavenBuildConfigDTO);
        }

    }

    @Override
    public void batchDeleteConfig(Set<Long> stepIds) {
        devopsCiMavenBuildConfigService.batchDeleteByStepIds(stepIds);
    }

    @Override
    public void fillTemplateStepConfigInfo(CiTemplateStepVO ciTemplateStepVO) {
        ciTemplateStepVO.setMavenBuildConfig(ciTemplateMavenBuildService.baseQueryById(ciTemplateStepVO.getId()));
    }

    @Override
    public void fillTemplateStepConfigInfo(DevopsCiStepVO devopsCiStepVO) {
        CiTemplateMavenBuildDTO ciTemplateMavenBuildDTO = ciTemplateMavenBuildService.baseQueryById(devopsCiStepVO.getId());
        if (ciTemplateMavenBuildDTO != null) {
            DevopsCiMavenBuildConfigDTO devopsCiMavenBuildConfigDTO = ConvertUtils.convertObject(ciTemplateMavenBuildDTO, DevopsCiMavenBuildConfigDTO.class);
            devopsCiStepVO.setMavenBuildConfig(dtoToVo(devopsCiMavenBuildConfigDTO));
        }
    }

    @Override
    public void fillStepConfigInfo(DevopsCiStepVO devopsCiStepVO) {
        DevopsCiMavenBuildConfigDTO devopsCiMavenBuildConfigDTO = devopsCiMavenBuildConfigService.queryByStepId(devopsCiStepVO.getId());
        if (devopsCiMavenBuildConfigDTO != null) {
            devopsCiStepVO.setMavenBuildConfig(dtoToVo(devopsCiMavenBuildConfigDTO));
        }
    }

    @Override
    public List<String> buildGitlabCiScript(DevopsCiStepDTO devopsCiStepDTO) {
        Long projectId = devopsCiStepDTO.getProjectId();
        Long devopsCiJobId = devopsCiStepDTO.getDevopsCiJobId();
        // 处理settings文件
        DevopsCiMavenBuildConfigVO devopsCiMavenBuildConfigVO = devopsCiMavenBuildConfigService.queryUnmarshalByStepId(devopsCiStepDTO.getId());


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
                repos.addAll(nexusMavenRepoDTOs.stream().map(DevopsCiMavenBuildStepHandler::convertRepo).collect(Collectors.toList()));
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
        DevopsCiMavenSettingsDTO devopsCiMavenSettingsRecordDTO = new DevopsCiMavenSettingsDTO(jobId, sequence);
        DevopsCiMavenSettingsDTO devopsCiMavenSettingsDTO = devopsCiMavenSettingsMapper.selectOne(devopsCiMavenSettingsRecordDTO);
        if (devopsCiMavenSettingsDTO == null) {
            MapperUtil.resultJudgedInsert(devopsCiMavenSettingsMapper, new DevopsCiMavenSettingsDTO(jobId, sequence, settings), ERROR_CI_MAVEN_SETTINGS_INSERT);
        } else {
            devopsCiMavenSettingsDTO.setMavenSettings(settings);
            MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsCiMavenSettingsMapper, devopsCiMavenSettingsDTO, ERROR_CI_MAVEN_SETTINGS_INSERT);
        }
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

    @Nullable
    private DevopsCiMavenBuildConfigVO dtoToVo(DevopsCiMavenBuildConfigDTO devopsCiMavenBuildConfigDTO) {
        DevopsCiMavenBuildConfigVO devopsCiMavenBuildConfigVO = ConvertUtils.convertObject(devopsCiMavenBuildConfigDTO, DevopsCiMavenBuildConfigVO.class);
        if (org.springframework.util.StringUtils.hasText(devopsCiMavenBuildConfigDTO.getNexusMavenRepoIdStr())) {
            devopsCiMavenBuildConfigVO.setNexusMavenRepoIds(JsonHelper.unmarshalByJackson(devopsCiMavenBuildConfigDTO.getNexusMavenRepoIdStr(), new TypeReference<Set<Long>>() {
            }));
        }
        if (org.springframework.util.StringUtils.hasText(devopsCiMavenBuildConfigDTO.getRepoStr())) {
            devopsCiMavenBuildConfigVO.setRepos(JsonHelper.unmarshalByJackson(devopsCiMavenBuildConfigDTO.getRepoStr(), new TypeReference<List<MavenRepoVO>>() {
            }));
        }
        return devopsCiMavenBuildConfigVO;
    }


    @Nullable
    private DevopsCiMavenBuildConfigDTO voToDto(DevopsCiMavenBuildConfigVO mavenBuildConfigVO) {
        DevopsCiMavenBuildConfigDTO devopsCiMavenBuildConfigDTO = ConvertUtils.convertObject(mavenBuildConfigVO, DevopsCiMavenBuildConfigDTO.class);
        if (!CollectionUtils.isEmpty(mavenBuildConfigVO.getNexusMavenRepoIds())) {
            devopsCiMavenBuildConfigDTO.setNexusMavenRepoIdStr(JsonHelper.marshalByJackson(mavenBuildConfigVO.getNexusMavenRepoIds()));
        }
        if (!CollectionUtils.isEmpty(mavenBuildConfigVO.getRepos())) {
            devopsCiMavenBuildConfigDTO.setRepoStr(JsonHelper.marshalByJackson(mavenBuildConfigVO.getRepos()));
        }
        return devopsCiMavenBuildConfigDTO;
    }

    @Override
    public DevopsCiStepTypeEnum getType() {
        return DevopsCiStepTypeEnum.MAVEN_BUILD;
    }

}
