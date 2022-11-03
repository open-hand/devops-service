package io.choerodon.devops.app.eventhandler.pipeline.job;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.devops.api.vo.CiJobWebHookVO;
import io.choerodon.devops.api.vo.DevopsCiJobVO;
import io.choerodon.devops.api.vo.pipeline.CiAuditConfigVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.CiAuditConfigDTO;
import io.choerodon.devops.infra.dto.CiAuditRecordDTO;
import io.choerodon.devops.infra.dto.DevopsCiJobDTO;
import io.choerodon.devops.infra.dto.DevopsCiJobRecordDTO;
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
    public void fillJobAdditionalInfo(DevopsCiJobDTO devopsCiJobDTO, CiJobWebHookVO job) {
        CiAuditConfigVO ciAuditConfigVO = ciAuditConfigService.queryConfigWithUsersById(devopsCiJobDTO.getConfigId());
        job.setCiAuditConfigVO(ciAuditConfigVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAdditionalRecordInfo(DevopsCiJobRecordDTO devopsCiJobRecordDTO, Long gitlabPipelineId, CiJobWebHookVO ciJobWebHookVO) {
        CiAuditConfigVO ciAuditConfigVO = ciJobWebHookVO.getCiAuditConfigVO();

        CiAuditRecordDTO ciAuditRecordDTO = ciAuditRecordService.queryByUniqueOption(devopsCiJobRecordDTO.getAppServiceId(),
                gitlabPipelineId,
                ciJobWebHookVO.getName());
        if (ciAuditRecordDTO == null) {
            // 初始化审核记录
            ciAuditRecordDTO = new CiAuditRecordDTO(devopsCiJobRecordDTO.getAppServiceId(),
                    devopsCiJobRecordDTO.getId(),
                    gitlabPipelineId,
                    ciJobWebHookVO.getName(),
                    ciAuditConfigVO.getCountersigned());
            ciAuditRecordService.baseCreate(ciAuditRecordDTO);
            // 初始化用户审核记录
            ciAuditUserRecordService.initAuditRecord(ciAuditRecordDTO.getId(), ciAuditConfigVO.getCdAuditUserIds());
        }
    }

    @Override
    protected Long saveConfig(DevopsCiJobVO devopsCiJobVO) {
        CiAuditConfigVO ciAuditConfig = devopsCiJobVO.getCiAuditConfig();
        CiAuditConfigDTO ciAuditConfigDTO = ConvertUtils.convertObject(ciAuditConfig, CiAuditConfigDTO.class);
        ciAuditConfigDTO.setId(null);

        ciAuditConfigService.baseCreate(ciAuditConfigDTO);

        ciAuditUserService.batchCreateByConfigIdAndUserIds(ciAuditConfigDTO.getId(), ciAuditConfig.getCdAuditUserIds());
        return ciAuditConfigDTO.getId();
    }


    @Override
    public void fillJobConfigInfo(DevopsCiJobVO devopsCiJobVO) {
        ciAuditConfigService.queryConfigWithUserDetailsById(devopsCiJobVO.getConfigId());
    }

    @Override
    public List<String> buildScript(Long organizationId, Long projectId, DevopsCiJobDTO devopsCiJobDTO) {
        List<String> result = new ArrayList<>();

        result.add("process_audit");

        return result;
    }

    @Override
    public CiJobTypeEnum getType() {
        return CiJobTypeEnum.AUDIT;
    }
}
