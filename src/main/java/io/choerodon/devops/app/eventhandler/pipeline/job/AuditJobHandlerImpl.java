package io.choerodon.devops.app.eventhandler.pipeline.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.CiJobWebHookVO;
import io.choerodon.devops.api.vo.DevopsCiJobVO;
import io.choerodon.devops.api.vo.pipeline.CiAuditConfigVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.CiAuditConfigDTO;
import io.choerodon.devops.infra.dto.CiAuditRecordDTO;
import io.choerodon.devops.infra.dto.DevopsCiJobDTO;
import io.choerodon.devops.infra.dto.DevopsCiJobRecordDTO;
import io.choerodon.devops.infra.dto.gitlab.ci.CiJob;
import io.choerodon.devops.infra.enums.CiJobTypeEnum;
import io.choerodon.devops.infra.util.ConvertUtils;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/11/3 9:06
 */
@Service
public class AuditJobHandlerImpl extends AbstractJobHandler {

    private static final String DEVOPS_AUDIT_CONFIG_EMPTY = "devops.audit.config.empty";
    private static final String DEVOPS_AUDIT_USER_EMPTY = "devops.audit.user.empty";


    @Autowired
    private CiAuditConfigService ciAuditConfigService;
    @Autowired
    private CiAuditRecordService ciAuditRecordService;
    @Autowired
    private CiAuditUserRecordService ciAuditUserRecordService;
    @Autowired
    private CiAuditUserService ciAuditUserService;
    @Autowired
    private CiTemplateAuditService ciTemplateAuditService;



    @Override
    protected void checkConfigInfo(Long projectId, DevopsCiJobVO devopsCiJobVO) {
        CiAuditConfigVO ciAuditConfig = devopsCiJobVO.getCiAuditConfig();
        if (ciAuditConfig == null) {
            throw new CommonException(DEVOPS_AUDIT_CONFIG_EMPTY, devopsCiJobVO.getName());
        }
        if (CollectionUtils.isEmpty(ciAuditConfig.getCdAuditUserIds())) {
            throw new CommonException(DEVOPS_AUDIT_USER_EMPTY, devopsCiJobVO.getName());
        }
    }

    @Override
    public void deleteCdInfo(DevopsCiJobVO devopsCiJobVO) {
        devopsCiJobVO.setCiAuditConfig(null);
        devopsCiJobVO.setCompleted(false);
    }

    @Override
    public void fillJobAdditionalInfo(DevopsCiJobDTO devopsCiJobDTO, CiJobWebHookVO job) {
        CiAuditConfigVO ciAuditConfigVO = ciAuditConfigService.queryConfigWithUsersById(devopsCiJobDTO.getConfigId());
        job.setCiAuditConfigVO(ciAuditConfigVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAdditionalRecordInfo(Long ciPipelineId, DevopsCiJobRecordDTO devopsCiJobRecordDTO, Long gitlabPipelineId, CiJobWebHookVO ciJobWebHookVO) {
        String jobName = ciJobWebHookVO.getName();
        CiAuditConfigVO ciAuditConfigVO = ciJobWebHookVO.getCiAuditConfigVO();

        saveAuditRecordInfo(ciPipelineId, devopsCiJobRecordDTO, gitlabPipelineId, jobName, ciAuditConfigVO);
    }

    private void saveAuditRecordInfo(Long ciPipelineId, DevopsCiJobRecordDTO devopsCiJobRecordDTO, Long gitlabPipelineId, String jobName, CiAuditConfigVO ciAuditConfigVO) {
        CiAuditRecordDTO ciAuditRecordDTO = ciAuditRecordService.queryByUniqueOption(devopsCiJobRecordDTO.getAppServiceId(),
                gitlabPipelineId,
                jobName);
        if (ciAuditRecordDTO == null) {
            // 初始化审核记录
            ciAuditRecordDTO = new CiAuditRecordDTO(ciPipelineId,
                    devopsCiJobRecordDTO.getAppServiceId(),
                    devopsCiJobRecordDTO.getId(),
                    gitlabPipelineId,
                    jobName,
                    ciAuditConfigVO.getCountersigned());
            ciAuditRecordService.baseCreate(ciAuditRecordDTO);
            // 初始化用户审核记录
            ciAuditUserRecordService.initAuditRecord(ciPipelineId, ciAuditRecordDTO.getId(), ciAuditConfigVO.getCdAuditUserIds());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAdditionalRecordInfo(Long ciPipelineId, DevopsCiJobRecordDTO devopsCiJobRecordDTO, Long gitlabPipelineId, DevopsCiJobDTO existDevopsCiJobDTO) {
        String jobName = existDevopsCiJobDTO.getName();
        CiAuditConfigVO ciAuditConfigVO = ciAuditConfigService.queryConfigWithUsersById(existDevopsCiJobDTO.getConfigId());
        saveAuditRecordInfo(ciPipelineId, devopsCiJobRecordDTO, gitlabPipelineId, jobName, ciAuditConfigVO);
    }

    @Override
    protected Long saveConfig(Long ciPipelineId, DevopsCiJobVO devopsCiJobVO) {
        CiAuditConfigVO ciAuditConfig = devopsCiJobVO.getCiAuditConfig();
        CiAuditConfigDTO ciAuditConfigDTO = ConvertUtils.convertObject(ciAuditConfig, CiAuditConfigDTO.class);
        ciAuditConfigDTO.setId(null);
        ciAuditConfigDTO.setCiPipelineId(ciPipelineId);


        ciAuditConfigService.baseCreate(ciAuditConfigDTO);

        ciAuditUserService.batchCreateByConfigIdAndUserIds(ciAuditConfigDTO.getId(), ciAuditConfig.getCdAuditUserIds());
        return ciAuditConfigDTO.getId();
    }


    @Override
    public void fillJobConfigInfo(DevopsCiJobVO devopsCiJobVO) {
        devopsCiJobVO.setCiAuditConfig(ciAuditConfigService.queryConfigWithUserDetailsById(devopsCiJobVO.getConfigId()));
    }

    @Override
    public void fillJobTemplateConfigInfo(DevopsCiJobVO devopsCiJobVO) {
        devopsCiJobVO.setCiAuditConfig(ciTemplateAuditService.queryConfigWithUserDetailsById(devopsCiJobVO.getConfigId()));
    }

    @Override
    public List<String> buildScript(Long organizationId, Long projectId, DevopsCiJobDTO devopsCiJobDTO) {
        List<String> result = new ArrayList<>();

        result.add("process_audit");

        return result;
    }

    @Override
    public void setCiJobConfig(DevopsCiJobDTO job, CiJob ciJob) {
        Map<String, String> variables = new HashMap<>();
        variables.put("GIT_STRATEGY", "none");
        ciJob.setVariables(variables);
        ciJob.setWhen("manual");
        ciJob.setAllowFailure(false);
    }

    @Override
    public CiJobTypeEnum getType() {
        return CiJobTypeEnum.AUDIT;
    }
}
