//package io.choerodon.devops.app.eventhandler.pipeline.step;
//
//import java.util.List;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.util.CollectionUtils;
//
//import io.choerodon.devops.api.vo.DevopsCiStepVO;
//import io.choerodon.devops.api.vo.pipeline.CiAuditConfigVO;
//import io.choerodon.devops.api.vo.pipeline.CiTemplateAuditConfigVO;
//import io.choerodon.devops.api.vo.template.CiTemplateStepVO;
//import io.choerodon.devops.app.service.CiAuditConfigService;
//import io.choerodon.devops.app.service.CiAuditUserService;
//import io.choerodon.devops.app.service.CiTemplateAuditService;
//import io.choerodon.devops.infra.dto.CiAuditConfigDTO;
//import io.choerodon.devops.infra.dto.DevopsCiStepDTO;
//import io.choerodon.devops.infra.enums.DevopsCiStepTypeEnum;
//import io.choerodon.devops.infra.util.ConvertUtils;
//
///**
// * 〈功能简述〉
// * 〈〉
// *
// * @author wanghao
// * @since 2022/11/2 11:43
// */
//@Service
//public class DevopsCiChartDeployStepHandlerImpl extends AbstractDevopsCiStepHandler {
//
//    @Autowired
//    private CiAuditConfigService ciAuditConfigService;
//    @Autowired
//    private CiAuditUserService ciAuditUserService;
//    @Autowired
//    private CiTemplateAuditService ciTemplateAuditService;
//
//    @Override
//    public void fillTemplateStepConfigInfo(CiTemplateStepVO ciTemplateStepVO) {
//        ciTemplateStepVO.setCiAuditConfig(ciTemplateAuditService.queryConfigWithUsersByStepId(ciTemplateStepVO.getId()));
//    }
//
//    @Override
//    public void fillTemplateStepConfigInfo(DevopsCiStepVO devopsCiStepVO) {
//        CiTemplateAuditConfigVO ciTemplateAuditConfigVO = ciTemplateAuditService.queryConfigWithUsersByStepId(devopsCiStepVO.getId());
//        CiAuditConfigVO ciAuditConfigVO = ConvertUtils.convertObject(ciTemplateAuditConfigVO, CiAuditConfigVO.class);
//        ciAuditConfigVO.setStepId(ciTemplateAuditConfigVO.getCiTemplateStepId());
//        devopsCiStepVO.setCiAuditConfig(ciAuditConfigVO);
//    }
//
//    @Override
//    public void fillStepConfigInfo(DevopsCiStepVO devopsCiStepVO) {
//        devopsCiStepVO.setCiAuditConfig(ciAuditConfigService.queryConfigWithUserDetailsByStepId(devopsCiStepVO.getId()));
//    }
//
//    @Override
//    public void saveConfig(Long stepId, DevopsCiStepVO devopsCiStepVO) {
//        CiAuditConfigVO ciAuditConfig = devopsCiStepVO.getCiAuditConfig();
//        CiAuditConfigDTO ciAuditConfigDTO = ConvertUtils.convertObject(ciAuditConfig, CiAuditConfigDTO.class);
//        ciAuditConfigDTO.setId(null);
//        ciAuditConfigDTO.setStepId(stepId);
//        ciAuditConfigService.baseCreate(ciAuditConfigDTO);
//
//        ciAuditUserService.batchCreateByConfigIdAndUserIds(ciAuditConfigDTO.getId(), ciAuditConfig.getCdAuditUserIds());
//    }
//
//    @Override
//    public List<String> buildGitlabCiScript(DevopsCiStepDTO devopsCiStepDTO) {
//        return super.buildGitlabCiScript(devopsCiStepDTO);
//    }
//
//    @Override
//    public void batchDeleteConfig(Set<Long> stepIds) {
//        List<CiAuditConfigDTO> ciAuditConfigDTOS = ciAuditConfigService.listByStepIds(stepIds);
//        if (!CollectionUtils.isEmpty(ciAuditConfigDTOS)) {
//            List<Long> configIds = ciAuditConfigDTOS.stream().map(CiAuditConfigDTO::getId).collect(Collectors.toList());
//            ciAuditUserService.batchDeleteByConfigIds(configIds);
//            ciAuditConfigService.batchDeleteByIds(configIds);
//        }
//    }
//
//    @Override
//    public Boolean isComplete(DevopsCiStepVO devopsCiStepVO) {
//        return super.isComplete(devopsCiStepVO);
//    }
//
//    @Override
//    public DevopsCiStepTypeEnum getType() {
//        return DevopsCiStepTypeEnum.AUDIT;
//    }
//}
