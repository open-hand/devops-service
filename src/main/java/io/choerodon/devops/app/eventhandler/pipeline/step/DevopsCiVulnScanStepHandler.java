package io.choerodon.devops.app.eventhandler.pipeline.step;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.devops.api.vo.DevopsCiStepVO;
import io.choerodon.devops.api.vo.template.CiTemplateStepVO;
import io.choerodon.devops.app.service.CiTplVulnScanConfigService;
import io.choerodon.devops.app.service.CiVulnScanConfigService;
import io.choerodon.devops.infra.dto.CiTplVulnScanConfigDTO;
import io.choerodon.devops.infra.dto.CiVulnScanConfigDTO;
import io.choerodon.devops.infra.dto.DevopsCiStepDTO;
import io.choerodon.devops.infra.enums.DevopsCiStepTypeEnum;
import io.choerodon.devops.infra.util.ConvertUtils;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/11/29 16:19
 */
@Component
public class DevopsCiVulnScanStepHandler extends AbstractDevopsCiStepHandler {


    @Autowired
    private CiVulnScanConfigService ciVulnScanConfigService;
    @Autowired
    private CiTplVulnScanConfigService ciTplVulnScanConfigService;

    @Override
    @Transactional
    public void saveConfig(Long stepId, DevopsCiStepVO devopsCiStepVO) {
        if (devopsCiStepVO.getVulnScanConfig() != null) {
            CiVulnScanConfigDTO vulnScanConfig = devopsCiStepVO.getVulnScanConfig();
            vulnScanConfig.setStepId(stepId);
            ciVulnScanConfigService.baseCreate(vulnScanConfig);
        }
    }

    @Override
    public void fillTemplateStepConfigInfo(CiTemplateStepVO ciTemplateStepVO) {
        CiTplVulnScanConfigDTO ciTplVulnScanConfigDTO = ciTplVulnScanConfigService.queryByStepId(ciTemplateStepVO.getId());
        if (ciTplVulnScanConfigDTO != null) {
            ciTemplateStepVO.setVulnScanConfig(ConvertUtils.convertObject(ciTplVulnScanConfigDTO, CiVulnScanConfigDTO.class));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveTemplateStepConfig(CiTemplateStepVO ciTemplateStepVO) {
        CiVulnScanConfigDTO vulnScanConfig = ciTemplateStepVO.getVulnScanConfig();
        if (vulnScanConfig == null) {
            return;
        }
        CiTplVulnScanConfigDTO ciTplVulnScanConfigDTO = ConvertUtils.convertObject(vulnScanConfig, CiTplVulnScanConfigDTO.class);
        ciTplVulnScanConfigDTO.setCiTemplateStepId(ciTemplateStepVO.getId());
        ciTplVulnScanConfigService.baseCreate(ciTplVulnScanConfigDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTemplateStepConfig(CiTemplateStepVO ciTemplateStepVO) {
        ciTplVulnScanConfigService.deleteByTemplateStepId(ciTemplateStepVO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void fillTemplateStepConfigInfo(DevopsCiStepVO devopsCiStepVO) {
        CiTplVulnScanConfigDTO ciTplVulnScanConfigDTO = ciTplVulnScanConfigService.queryByStepId(devopsCiStepVO.getId());
        if (ciTplVulnScanConfigDTO != null) {
            devopsCiStepVO.setVulnScanConfig(ConvertUtils.convertObject(ciTplVulnScanConfigDTO, CiVulnScanConfigDTO.class));
        }
    }

    @Override
    public void fillStepConfigInfo(DevopsCiStepVO devopsCiStepVO) {
        devopsCiStepVO.setVulnScanConfig(ciVulnScanConfigService.queryByStepId(devopsCiStepVO.getId()));
    }

    @Override
    public List<String> buildGitlabCiScript(DevopsCiStepDTO devopsCiStepDTO) {
        List<String> cmds = new ArrayList<>();
        CiVulnScanConfigDTO ciVulnScanConfigDTO = ciVulnScanConfigService.queryByStepId(devopsCiStepDTO.getId());
        String scanCmd = "trivy fs --scanners vuln --skip-java-db-update -f json -o vulnerability.json %s";
        cmds.add(String.format(scanCmd, ciVulnScanConfigDTO.getPath()));
        cmds.add("upload_vuln_result");
        return cmds;
    }

    @Override
    public void batchDeleteConfig(Set<Long> stepIds) {
        ciVulnScanConfigService.batchDeleteByStepIds(stepIds);
    }

    @Override
    public DevopsCiStepTypeEnum getType() {
        return DevopsCiStepTypeEnum.VULN_SCAN;
    }


}
