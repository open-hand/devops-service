package io.choerodon.devops.app.eventhandler.cd;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.cd.PipelineAuditCfgVO;
import io.choerodon.devops.api.vo.cd.PipelineJobRecordVO;
import io.choerodon.devops.api.vo.cd.PipelineJobVO;
import io.choerodon.devops.api.vo.pipeline.Audit;
import io.choerodon.devops.app.service.PipelineAuditCfgService;
import io.choerodon.devops.app.service.PipelineAuditRecordService;
import io.choerodon.devops.app.service.PipelineAuditUserRecordService;
import io.choerodon.devops.app.service.PipelineAuditUserService;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.enums.AuditStatusEnum;
import io.choerodon.devops.infra.enums.cd.CdJobTypeEnum;
import io.choerodon.devops.infra.enums.cd.PipelineStatusEnum;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.util.ConvertUtils;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/11/3 9:06
 */
@Service
public class CdAuditJobHandlerImpl extends AbstractCdJobHandler {

    private static final String DEVOPS_AUDIT_CONFIG_EMPTY = "devops.audit.config.empty";
    private static final String DEVOPS_AUDIT_USER_EMPTY = "devops.audit.user.empty";


    @Autowired
    private PipelineAuditCfgService pipelineAuditCfgService;
    @Autowired
    private PipelineAuditUserService pipelineAuditUserService;
    @Autowired
    private PipelineAuditRecordService pipelineAuditRecordService;
    @Autowired
    private PipelineAuditUserRecordService pipelineAuditUserRecordService;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;


    @Override
    protected void checkConfigInfo(Long projectId, PipelineJobVO pipelineJobVO) {
        PipelineAuditCfgVO pipelineAuditCfgVO = pipelineJobVO.getAuditConfig();
        if (pipelineAuditCfgVO == null) {
            throw new CommonException(DEVOPS_AUDIT_CONFIG_EMPTY);
        }
        if (CollectionUtils.isEmpty(pipelineAuditCfgVO.getAuditUserIds())) {
            throw new CommonException(DEVOPS_AUDIT_USER_EMPTY);
        }
    }

    @Override
    public void initAdditionalRecordInfo(Long pipelineId, PipelineJobDTO job, PipelineJobRecordDTO pipelineJobRecordDTO) {
        pipelineAuditRecordService.initAuditRecord(pipelineId, pipelineJobRecordDTO.getPipelineRecordId(), pipelineJobRecordDTO.getId(), job.getConfigId());
    }

    @Override
    public void fillAdditionalRecordInfo(PipelineJobRecordVO pipelineJobRecordVO) {
        PipelineAuditRecordDTO pipelineAuditRecordDTO = pipelineAuditRecordService.queryByJobRecordId(pipelineJobRecordVO.getId());
        if (pipelineAuditRecordDTO != null) {
            List<PipelineAuditUserRecordDTO> auditUserRecordDTOList = pipelineAuditUserRecordService.listByAuditRecordId(pipelineAuditRecordDTO.getId());
            if (!CollectionUtils.isEmpty(auditUserRecordDTOList)) {

                List<Long> uids = auditUserRecordDTOList.stream().map(PipelineAuditUserRecordDTO::getUserId).collect(Collectors.toList());
                List<IamUserDTO> allIamUserDTOS = baseServiceClientOperator.listUsersByIds(uids);
                List<Long> reviewedUids = auditUserRecordDTOList.stream()
                        .filter(v -> AuditStatusEnum.PASSED.value().equals(v.getStatus()) || AuditStatusEnum.REFUSED.value().equals(v.getStatus()))
                        .map(PipelineAuditUserRecordDTO::getUserId).collect(Collectors.toList());
                List<IamUserDTO> reviewedUsers = allIamUserDTOS.stream().filter(u -> reviewedUids.contains(u.getId())).collect(Collectors.toList());

                Audit audit = new Audit(allIamUserDTOS, reviewedUsers, pipelineJobRecordVO.getStatus(), pipelineAuditRecordDTO.getCountersigned());

                if (PipelineStatusEnum.NOT_AUDIT.value().equals(pipelineJobRecordVO.getStatus())) {
                    Long userId = DetailsHelper.getUserDetails().getUserId();
                    audit.setCanAuditFlag(auditUserRecordDTOList.stream().anyMatch(r -> r.getUserId().equals(userId) && AuditStatusEnum.NOT_AUDIT.value().equals(r.getStatus())));
                }

                pipelineJobRecordVO.setAudit(audit);
            }
        }
    }

    @Override
    protected Long saveConfig(Long pipelineId, PipelineJobVO devopsCiJobVO) {
        PipelineAuditCfgVO pipelineAuditCfg = devopsCiJobVO.getAuditConfig();
        PipelineAuditCfgDTO pipelineAuditCfgDTO = ConvertUtils.convertObject(pipelineAuditCfg, PipelineAuditCfgDTO.class);
        pipelineAuditCfgDTO.setId(null);
        pipelineAuditCfgDTO.setPipelineId(pipelineId);


        pipelineAuditCfgService.baseCreate(pipelineAuditCfgDTO);

        pipelineAuditUserService.batchCreateByConfigIdAndUserIds(pipelineAuditCfgDTO.getId(), pipelineAuditCfg.getAuditUserIds());
        return pipelineAuditCfgDTO.getId();
    }


    @Override
    public void fillJobConfigInfo(PipelineJobVO pipelineJobVO) {
        pipelineJobVO.setAuditConfig(pipelineAuditCfgService.queryConfigWithUserDetailsById(pipelineJobVO.getConfigId()));
    }


    @Override
    public CdJobTypeEnum getType() {
        return CdJobTypeEnum.CD_AUDIT;
    }
}
