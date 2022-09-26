package io.choerodon.devops.app.eventhandler.pipeline.step;

import org.springframework.stereotype.Service;

import io.choerodon.devops.infra.enums.DevopsCiStepTypeEnum;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/12/14 17:52
 */
@Service
public class DevopsCiMavenUnitTestStepHandler extends DevopsCiMavenBuildStepHandler {

//    @Autowired
//    private DevopsCiMavenBuildConfigService devopsCiMavenBuildConfigService;
//    @Autowired
//    private CiTemplateMavenBuildService ciTemplateMavenBuildService;

    @Override
    public DevopsCiStepTypeEnum getType() {
        return DevopsCiStepTypeEnum.MAVEN_UNIT_TEST;
    }

//    @Override
//    public void fillTemplateStepConfigInfo(CiTemplateStepVO ciTemplateStepVO) {
//        ciTemplateStepVO.setMavenBuildConfig(ciTemplateMavenBuildService.baseQueryById(ciTemplateStepVO.getId()));
//    }
//
//    @Override
//    public void fillTemplateStepConfigInfo(DevopsCiStepVO devopsCiStepVO) {
//        CiTemplateMavenBuildDTO ciTemplateMavenBuildDTO = ciTemplateMavenBuildService.baseQueryById(devopsCiStepVO.getId());
//        if (ciTemplateMavenBuildDTO != null) {
//            DevopsCiMavenBuildConfigDTO devopsCiMavenBuildConfigDTO = ConvertUtils.convertObject(ciTemplateMavenBuildDTO, DevopsCiMavenBuildConfigDTO.class);
//            devopsCiStepVO.setMavenBuildConfig(devopsCiMavenBuildConfigService.dtoToVo(devopsCiMavenBuildConfigDTO));
//        }
//    }
//
//    @Override
//    public void fillStepConfigInfo(DevopsCiStepVO devopsCiStepVO) {
//        DevopsCiMavenBuildConfigDTO devopsCiMavenBuildConfigDTO = devopsCiMavenBuildConfigService.queryByStepId(devopsCiStepVO.getId());
//        if (devopsCiMavenBuildConfigDTO != null) {
//            devopsCiStepVO.setMavenBuildConfig(devopsCiMavenBuildConfigService.dtoToVo(devopsCiMavenBuildConfigDTO));
//        }
//    }
//
//    @Override
//    protected Boolean isConfigComplete(DevopsCiStepVO devopsCiStepVO) {
//        return super.isConfigComplete(devopsCiStepVO);
//    }
//
//    @Override
//    public void saveConfig(Long stepId, DevopsCiStepVO devopsCiStepVO) {
//        DevopsCiMavenBuildConfigVO mavenBuildConfig = devopsCiStepVO.getMavenBuildConfig();
//        if (mavenBuildConfig != null) {
//            DevopsCiMavenBuildConfigDTO devopsCiMavenBuildConfigDTO = devopsCiMavenBuildConfigService.voToDto(mavenBuildConfig);
//            devopsCiMavenBuildConfigDTO.setStepId(stepId);
//            devopsCiMavenBuildConfigDTO.setId(null);
//            devopsCiMavenBuildConfigService.baseCreate(devopsCiMavenBuildConfigDTO);
//        }
//    }
//
//    @Override
//    public List<String> buildGitlabCiScript(DevopsCiStepDTO devopsCiStepDTO) {
//        Long projectId = devopsCiStepDTO.getProjectId();
//        Long devopsCiJobId = devopsCiStepDTO.getDevopsCiJobId();
//        // 处理settings文件
//        DevopsCiMavenBuildConfigVO devopsCiMavenBuildConfigVO = devopsCiMavenBuildConfigService.queryUnmarshalByStepId(devopsCiStepDTO.getId());
//        DevopsCiMavenSettingsDTO devopsCiMavenSettingsDTO = null;
//        if (devopsCiMavenBuildConfigVO != null) {
//            DevopsCiPipelineAdditionalValidator.validateMavenBuildStep(devopsCiMavenBuildConfigVO);
//            devopsCiMavenSettingsDTO = devopsCiMavenBuildConfigService.buildAndSaveMavenSettings(projectId,
//                    devopsCiJobId,
//                    devopsCiStepDTO.getSequence(),
//                    devopsCiMavenBuildConfigVO);
//        }
//
//        return GitlabCiUtil.buildMavenScripts(
//                projectId,
//                devopsCiStepDTO,
//                devopsCiMavenSettingsDTO);
//    }
//
//    @Override
//    public void batchDeleteConfig(Set<Long> stepIds) {
//        devopsCiMavenBuildConfigService.batchDeleteByStepIds(stepIds);
//    }
}
