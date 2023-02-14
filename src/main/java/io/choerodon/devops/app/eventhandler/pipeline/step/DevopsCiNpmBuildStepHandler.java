package io.choerodon.devops.app.eventhandler.pipeline.step;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.devops.api.vo.DevopsCiStepVO;
import io.choerodon.devops.api.vo.template.CiTemplateStepVO;
import io.choerodon.devops.app.service.CiNpmBuildConfigService;
import io.choerodon.devops.app.service.CiTplNpmBuildConfigService;
import io.choerodon.devops.infra.dto.CiNpmBuildConfigDTO;
import io.choerodon.devops.infra.dto.CiTplNpmBuildConfigDTO;
import io.choerodon.devops.infra.dto.DevopsCiStepDTO;
import io.choerodon.devops.infra.enums.DevopsCiStepTypeEnum;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.GitlabCiUtil;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/11/29 16:19
 */
@Component
public class DevopsCiNpmBuildStepHandler extends AbstractDevopsCiStepHandler {


    @Autowired
    private CiNpmBuildConfigService ciNpmBuildConfigService;
    @Autowired
    private CiTplNpmBuildConfigService ciTplNpmBuildConfigService;

    @Override
    @Transactional
    public void saveConfig(Long stepId, DevopsCiStepVO devopsCiStepVO) {
        if (devopsCiStepVO.getNpmBuildConfig() != null) {
            CiNpmBuildConfigDTO npmBuildConfig = devopsCiStepVO.getNpmBuildConfig();
            npmBuildConfig.setStepId(stepId);
            ciNpmBuildConfigService.baseCreate(npmBuildConfig);
        }
    }

    @Override
    public void fillTemplateStepConfigInfo(CiTemplateStepVO ciTemplateStepVO) {
        CiTplNpmBuildConfigDTO ciTplNpmBuildConfigDTO = ciTplNpmBuildConfigService.queryByStepId(ciTemplateStepVO.getId());
        if (ciTplNpmBuildConfigDTO != null) {
            ciTemplateStepVO.setNpmBuildConfig(ConvertUtils.convertObject(ciTplNpmBuildConfigDTO, CiNpmBuildConfigDTO.class));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveTemplateStepConfig(CiTemplateStepVO ciTemplateStepVO) {
        CiNpmBuildConfigDTO npmBuildConfigDTO = ciTemplateStepVO.getNpmBuildConfig();
        if (npmBuildConfigDTO == null) {
            return;
        }
        CiTplNpmBuildConfigDTO ciTplNpmBuildConfigDTO = ConvertUtils.convertObject(npmBuildConfigDTO, CiTplNpmBuildConfigDTO.class);
        ciTplNpmBuildConfigDTO.setCiTemplateStepId(ciTemplateStepVO.getId());
        ciTplNpmBuildConfigService.baseCreate(ciTplNpmBuildConfigDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTemplateStepConfig(CiTemplateStepVO ciTemplateStepVO) {
        ciTplNpmBuildConfigService.deleteByTemplateStepId(ciTemplateStepVO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void fillTemplateStepConfigInfo(DevopsCiStepVO devopsCiStepVO) {
        CiTplNpmBuildConfigDTO ciTplNpmBuildConfigDTO = ciTplNpmBuildConfigService.queryByStepId(devopsCiStepVO.getId());
        if (ciTplNpmBuildConfigDTO != null) {
            devopsCiStepVO.setNpmBuildConfig(ConvertUtils.convertObject(ciTplNpmBuildConfigDTO, CiNpmBuildConfigDTO.class));
        }
    }

    @Override
    public void fillStepConfigInfo(DevopsCiStepVO devopsCiStepVO) {
        devopsCiStepVO.setNpmBuildConfig(ciNpmBuildConfigService.queryByStepId(devopsCiStepVO.getId()));
    }

    @Override
    public List<String> buildGitlabCiScript(DevopsCiStepDTO devopsCiStepDTO) {
        List<String> cmds = new ArrayList<>();
        CiNpmBuildConfigDTO ciNpmBuildConfigDTO = ciNpmBuildConfigService.queryByStepId(devopsCiStepDTO.getId());
        if (ciNpmBuildConfigDTO != null && ciNpmBuildConfigDTO.getNpmRepoId() != null) {
            cmds.add("export_npm_push_variable " + ciNpmBuildConfigDTO.getNpmRepoId());
            cmds.add("npm-cli-login -u $NPM_USERNAME -p $NPM_PASSWORD -e $NPM_EMAIL -r \"$NPM_REGISTRY/\" -s @privateNPM --quotes=true");

        }
        cmds.addAll(GitlabCiUtil.filterLines(GitlabCiUtil.splitLinesForShell(devopsCiStepDTO.getScript()), true, true));

        return cmds;
    }

    @Override
    public void batchDeleteConfig(Set<Long> stepIds) {
        ciNpmBuildConfigService.batchDeleteByStepIds(stepIds);
    }

    @Override
    public DevopsCiStepTypeEnum getType() {
        return DevopsCiStepTypeEnum.NPM_BUILD;
    }


}
