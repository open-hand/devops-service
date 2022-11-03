package io.choerodon.devops.app.eventhandler.pipeline.job;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.devops.api.vo.CiJobWebHookVO;
import io.choerodon.devops.api.vo.pipeline.CiAuditConfigVO;
import io.choerodon.devops.app.service.CiAuditConfigService;
import io.choerodon.devops.app.service.CiAuditRecordService;
import io.choerodon.devops.app.service.CiAuditUserRecordService;
import io.choerodon.devops.app.service.DevopsCiStepService;
import io.choerodon.devops.infra.dto.CiAuditRecordDTO;
import io.choerodon.devops.infra.dto.DevopsCiJobDTO;
import io.choerodon.devops.infra.dto.DevopsCiJobRecordDTO;
import io.choerodon.devops.infra.dto.DevopsCiStepDTO;
import io.choerodon.devops.infra.enums.CiJobTypeEnum;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/11/3 9:06
 */
@Service
public class AuditJobHandlerImpl implements JobHandler {

    @Autowired
    private CiAuditConfigService ciAuditConfigService;
    @Autowired
    private DevopsCiStepService devopsCiStepService;
    @Autowired
    private CiAuditRecordService ciAuditRecordService;
    @Autowired
    private CiAuditUserRecordService ciAuditUserRecordService;

    @Override
    public void fillJobAdditionalInfo(DevopsCiJobDTO devopsCiJobDTO, CiJobWebHookVO job) {
        Long jobId = devopsCiJobDTO.getId();
        List<DevopsCiStepDTO> devopsCiStepDTOS = devopsCiStepService.listByJobId(jobId);
        DevopsCiStepDTO devopsCiStepDTO = devopsCiStepDTOS.get(0);
        CiAuditConfigVO ciAuditConfigVO = ciAuditConfigService.queryConfigWithUsersByStepId(devopsCiStepDTO.getId());
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
    public CiJobTypeEnum getType() {
        return CiJobTypeEnum.AUDIT;
    }
}
