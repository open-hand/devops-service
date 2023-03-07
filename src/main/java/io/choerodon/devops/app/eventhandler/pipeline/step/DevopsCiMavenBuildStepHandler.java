package io.choerodon.devops.app.eventhandler.pipeline.step;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.devops.api.validator.DevopsCiPipelineAdditionalValidator;
import io.choerodon.devops.api.vo.DevopsCiMavenBuildConfigVO;
import io.choerodon.devops.api.vo.DevopsCiStepVO;
import io.choerodon.devops.api.vo.template.CiTemplateStepVO;
import io.choerodon.devops.app.service.CiTemplateMavenBuildService;
import io.choerodon.devops.app.service.DevopsCiMavenBuildConfigService;
import io.choerodon.devops.infra.dto.CiTemplateMavenBuildDTO;
import io.choerodon.devops.infra.dto.DevopsCiMavenBuildConfigDTO;
import io.choerodon.devops.infra.dto.DevopsCiMavenSettingsDTO;
import io.choerodon.devops.infra.dto.DevopsCiStepDTO;
import io.choerodon.devops.infra.enums.DevopsCiStepTypeEnum;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.GitlabCiUtil;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/11/29 17:52
 */
@Service
public class DevopsCiMavenBuildStepHandler extends AbstractDevopsCiStepHandler {

    @Autowired
    protected DevopsCiMavenBuildConfigService devopsCiMavenBuildConfigService;
    @Autowired
    private CiTemplateMavenBuildService ciTemplateMavenBuildService;


    @Override
    @Transactional
    public void saveConfig(Long stepId, DevopsCiStepVO devopsCiStepVO) {
        DevopsCiMavenBuildConfigVO mavenBuildConfig = devopsCiStepVO.getMavenBuildConfig();
        if (mavenBuildConfig != null) {
            devopsCiMavenBuildConfigService.baseCreate(stepId, mavenBuildConfig);
        }

    }

    @Override
    public void batchDeleteConfig(Set<Long> stepIds) {
        devopsCiMavenBuildConfigService.batchDeleteByStepIds(stepIds);
    }

    @Override
    public void fillTemplateStepConfigInfo(CiTemplateStepVO ciTemplateStepVO) {
        CiTemplateMavenBuildDTO templateMavenBuildDTO = ciTemplateMavenBuildService.baseQueryById(ciTemplateStepVO.getId());
        if (templateMavenBuildDTO != null) {
            ciTemplateStepVO.setMavenBuildConfig(ciTemplateMavenBuildService.dtoToVo(templateMavenBuildDTO));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveTemplateStepConfig(CiTemplateStepVO ciTemplateStepVO) {
        CiTemplateMavenBuildDTO mavenBuildConfig = ciTemplateStepVO.getMavenBuildConfig();
        if (mavenBuildConfig != null) {
            ciTemplateMavenBuildService.baseCreate(ciTemplateStepVO.getId(), mavenBuildConfig);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTemplateStepConfig(CiTemplateStepVO ciTemplateStepVO) {
        ciTemplateMavenBuildService.deleteByTemplateStepId(ciTemplateStepVO.getId());
    }

    @Override
    public void fillTemplateStepConfigInfo(DevopsCiStepVO devopsCiStepVO) {
        CiTemplateMavenBuildDTO ciTemplateMavenBuildDTO = ciTemplateMavenBuildService.baseQueryById(devopsCiStepVO.getId());
        if (ciTemplateMavenBuildDTO != null) {
            DevopsCiMavenBuildConfigDTO devopsCiMavenBuildConfigDTO = ConvertUtils.convertObject(ciTemplateMavenBuildDTO, DevopsCiMavenBuildConfigDTO.class);
            devopsCiStepVO.setMavenBuildConfig(devopsCiMavenBuildConfigService.dtoToVo(devopsCiMavenBuildConfigDTO));
        }
    }

    @Override
    public void fillStepConfigInfo(DevopsCiStepVO devopsCiStepVO) {
        DevopsCiMavenBuildConfigDTO devopsCiMavenBuildConfigDTO = devopsCiMavenBuildConfigService.queryByStepId(devopsCiStepVO.getId());
        if (devopsCiMavenBuildConfigDTO != null) {
            devopsCiStepVO.setMavenBuildConfig(devopsCiMavenBuildConfigService.dtoToVo(devopsCiMavenBuildConfigDTO));
        }
    }

    @Override
    public List<String> buildGitlabCiScript(DevopsCiStepDTO devopsCiStepDTO) {
        Long projectId = devopsCiStepDTO.getProjectId();
        Long devopsCiJobId = devopsCiStepDTO.getDevopsCiJobId();
        // 处理settings文件
        DevopsCiMavenBuildConfigVO devopsCiMavenBuildConfigVO = devopsCiMavenBuildConfigService.queryUnmarshalByStepId(devopsCiStepDTO.getId());
        DevopsCiMavenSettingsDTO devopsCiMavenSettingsDTO = null;
        if (devopsCiMavenBuildConfigVO != null) {
            DevopsCiPipelineAdditionalValidator.validateMavenBuildStep(devopsCiMavenBuildConfigVO);
            devopsCiMavenSettingsDTO = devopsCiMavenBuildConfigService.buildAndSaveMavenSettings(projectId,
                    devopsCiJobId,
                    devopsCiStepDTO.getSequence(),
                    devopsCiMavenBuildConfigVO);
        }

        return buildMavenScripts(
                projectId,
                devopsCiStepDTO,
                devopsCiMavenSettingsDTO);
    }

    /**
     * 生成maven构建相关的脚本
     *
     * @param projectId
     * @param devopsCiStepDTO
     * @param devopsCiMavenSettingsDTO 这个阶段是否有配置settings
     * @return 生成的shell脚本
     */
    protected List<String> buildMavenScripts(Long projectId, DevopsCiStepDTO devopsCiStepDTO, DevopsCiMavenSettingsDTO devopsCiMavenSettingsDTO) {
        List<String> shells = GitlabCiUtil.filterLines(GitlabCiUtil.splitLinesForShell(devopsCiStepDTO.getScript()), true, true);
        if (devopsCiMavenSettingsDTO != null) {
            // 插入shell指令将配置的settings文件下载到项目目录下
            shells.add(0, GitlabCiUtil.downloadMavenSettings(projectId, devopsCiMavenSettingsDTO.getId()));
        }
        return shells;
    }

//    /**
//     * 生成maven构建相关的脚本
//     *
//     * @param projectId       项目id
//     * @param jobId           job id
//     * @param devopsCiStepDTO
//     * @param hasSettings     这个阶段是否有配置settings
//     * @return 生成的shell脚本
//     */
//    private List<String> buildMavenScripts(final Long projectId, final Long jobId, DevopsCiStepDTO devopsCiStepDTO, boolean hasSettings) {
//        List<String> shells = GitlabCiUtil.filterLines(GitlabCiUtil.splitLinesForShell(devopsCiStepDTO.getScript()), true, true);
//        if (hasSettings) {
//            // 插入shell指令将配置的settings文件下载到项目目录下
//            shells.add(0, GitlabCiUtil.downloadMavenSettings(projectId, jobId, devopsCiStepDTO.getSequence()));
//        }
//        return shells;
//    }

//    private static MavenRepoVO convertRepo(NexusMavenRepoDTO nexusMavenRepoDTO) {
//        MavenRepoVO mavenRepoVO = new MavenRepoVO();
//        mavenRepoVO.setName(nexusMavenRepoDTO.getName());
//        mavenRepoVO.setPrivateRepo(Boolean.TRUE);
//        if ("MIXED".equals(nexusMavenRepoDTO.getVersionPolicy())) {
//            mavenRepoVO.setType(GitOpsConstants.SNAPSHOT + "," + GitOpsConstants.RELEASE);
//        } else {
//            // group 类型的仓库没有版本类型
//            mavenRepoVO.setType(nexusMavenRepoDTO.getVersionPolicy() == null ? null : nexusMavenRepoDTO.getVersionPolicy().toLowerCase());
//        }
//        mavenRepoVO.setUrl(nexusMavenRepoDTO.getUrl());
//        mavenRepoVO.setUsername(nexusMavenRepoDTO.getNeUserId());
//        mavenRepoVO.setPassword(nexusMavenRepoDTO.getNeUserPassword());
//        return mavenRepoVO;
//    }


//    private static String buildSettings(List<MavenRepoVO> mavenRepoList) {
//        List<Server> servers = new ArrayList<>();
//        List<Repository> repositories = new ArrayList<>();
//
//        mavenRepoList.forEach(m -> {
//            if (m.getType() != null) {
//                String[] types = m.getType().split(GitOpsConstants.COMMA);
//                if (types.length > 2) {
//                    throw new CommonException(ERROR_CI_MAVEN_REPOSITORY_TYPE, m.getType());
//                }
//            }
//            if (Boolean.TRUE.equals(m.getPrivateRepo())) {
//                servers.add(new Server(Objects.requireNonNull(m.getName()), Objects.requireNonNull(m.getUsername()), Objects.requireNonNull(m.getPassword())));
//            }
//            repositories.add(new Repository(
//                    Objects.requireNonNull(m.getName()),
//                    Objects.requireNonNull(m.getName()),
//                    Objects.requireNonNull(m.getUrl()),
//                    m.getType() == null ? null : new RepositoryPolicy(m.getType().contains(GitOpsConstants.RELEASE)),
//                    m.getType() == null ? null : new RepositoryPolicy(m.getType().contains(GitOpsConstants.SNAPSHOT))));
//        });
//        return MavenSettingsUtil.generateMavenSettings(servers, repositories);
//    }

    @Override
    public DevopsCiStepTypeEnum getType() {
        return DevopsCiStepTypeEnum.MAVEN_BUILD;
    }

}
