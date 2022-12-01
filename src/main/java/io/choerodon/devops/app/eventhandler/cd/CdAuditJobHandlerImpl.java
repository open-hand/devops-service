package io.choerodon.devops.app.eventhandler.cd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.cd.PipelineAuditCfgVO;
import io.choerodon.devops.api.vo.cd.PipelineJobVO;
import io.choerodon.devops.app.service.PipelineAuditCfgService;
import io.choerodon.devops.app.service.PipelineAuditRecordService;
import io.choerodon.devops.app.service.PipelineAuditUserService;
import io.choerodon.devops.infra.dto.PipelineAuditCfgDTO;
import io.choerodon.devops.infra.dto.PipelineJobDTO;
import io.choerodon.devops.infra.dto.PipelineJobRecordDTO;
import io.choerodon.devops.infra.enums.cd.CdJobTypeEnum;
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
    public void deleteConfigByPipelineId(Long pipelineId) {
        pipelineAuditCfgService.deleteConfigByPipelineId(pipelineId);
    }

    @Override
    public void initAdditionalRecordInfo(Long pipelineId, PipelineJobDTO job, PipelineJobRecordDTO pipelineJobRecordDTO) {
        pipelineAuditRecordService.initAuditRecord(pipelineId, pipelineJobRecordDTO.getId(), job.getConfigId());
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
        return CdJobTypeEnum.AUDIT;
    }
}
