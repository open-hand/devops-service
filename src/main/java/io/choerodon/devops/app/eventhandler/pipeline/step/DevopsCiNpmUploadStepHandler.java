package io.choerodon.devops.app.eventhandler.pipeline.step;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.devops.api.vo.DevopsCiStepVO;
import io.choerodon.devops.api.vo.template.CiTemplateStepVO;
import io.choerodon.devops.app.service.CiNpmPublishConfigService;
import io.choerodon.devops.app.service.CiTplNpmPublishConfigService;
import io.choerodon.devops.infra.dto.CiNpmPublishConfigDTO;
import io.choerodon.devops.infra.dto.CiTplNpmPublishConfigDTO;
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
public class DevopsCiNpmUploadStepHandler extends AbstractDevopsCiStepHandler {


    @Autowired
    private CiNpmPublishConfigService ciNpmPublishConfigService;
    @Autowired
    private CiTplNpmPublishConfigService ciTplNpmPublishConfigService;

    @Override
    @Transactional
    public void saveConfig(Long stepId, DevopsCiStepVO devopsCiStepVO) {
        CiNpmPublishConfigDTO npmPublishConfig = devopsCiStepVO.getNpmPublishConfig();
        npmPublishConfig.setStepId(stepId);
        ciNpmPublishConfigService.baseCreate(npmPublishConfig);
    }

    @Override
    public void fillTemplateStepConfigInfo(CiTemplateStepVO ciTemplateStepVO) {
        CiTplNpmPublishConfigDTO ciTplNpmPublishConfigDTO = ciTplNpmPublishConfigService.queryByStepId(ciTemplateStepVO.getId());
        ciTemplateStepVO.setNpmPublishConfig(ConvertUtils.convertObject(ciTplNpmPublishConfigDTO, CiNpmPublishConfigDTO.class));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveTemplateStepConfig(CiTemplateStepVO ciTemplateStepVO) {
        CiNpmPublishConfigDTO npmPublishConfig = ciTemplateStepVO.getNpmPublishConfig();
        if (npmPublishConfig == null) {
            return;
        }
        CiTplNpmPublishConfigDTO ciTplNpmPublishConfigDTO = ConvertUtils.convertObject(npmPublishConfig, CiTplNpmPublishConfigDTO.class);
        ciTplNpmPublishConfigDTO.setCiTemplateStepId(ciTemplateStepVO.getId());
        ciTplNpmPublishConfigService.baseCreate(ciTplNpmPublishConfigDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTemplateStepConfig(CiTemplateStepVO ciTemplateStepVO) {
        ciTplNpmPublishConfigService.deleteByTemplateStepId(ciTemplateStepVO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void fillTemplateStepConfigInfo(DevopsCiStepVO devopsCiStepVO) {
        CiTplNpmPublishConfigDTO ciTplNpmPublishConfigDTO = ciTplNpmPublishConfigService.queryByStepId(devopsCiStepVO.getId());
        devopsCiStepVO.setNpmPublishConfig(ConvertUtils.convertObject(ciTplNpmPublishConfigDTO, CiNpmPublishConfigDTO.class));
    }

    @Override
    public void fillStepConfigInfo(DevopsCiStepVO devopsCiStepVO) {
        CiNpmPublishConfigDTO ciNpmPublishConfigDTO = ciNpmPublishConfigService.queryByStepId(devopsCiStepVO.getId());
        devopsCiStepVO.setNpmPublishConfig(ciNpmPublishConfigDTO);
    }

    @Override
    public List<String> buildGitlabCiScript(DevopsCiStepDTO devopsCiStepDTO) {
        List<String> cmds = new ArrayList<>();
        CiNpmPublishConfigDTO ciNpmPublishConfigDTO = ciNpmPublishConfigService.queryByStepId(devopsCiStepDTO.getId());
        if (ciNpmPublishConfigDTO != null && ciNpmPublishConfigDTO.getNpmPushRepoId() != null) {
            cmds.add("export_npm_push_variable " + ciNpmPublishConfigDTO.getNpmPushRepoId());
        }
        cmds.addAll(GitlabCiUtil.filterLines(GitlabCiUtil.splitLinesForShell(devopsCiStepDTO.getScript()), true, true));

        return cmds;
    }

    @Override
    public void batchDeleteConfig(Set<Long> stepIds) {
        ciNpmPublishConfigService.batchDeleteByStepIds(stepIds);
    }

    @Override
    public DevopsCiStepTypeEnum getType() {
        return DevopsCiStepTypeEnum.NPM_UPLOAD;
    }


}
