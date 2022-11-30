package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import io.choerodon.devops.app.service.CiAuditUserRecordService;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.CiAuditUserRecordDTO;
import io.choerodon.devops.infra.enums.AuditStatusEnum;
import io.choerodon.devops.infra.mapper.CiAuditUserRecordMapper;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * ci 人工卡点用户审核记录表(CiAuditUserRecord)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2022-11-03 10:32:19
 */
@Service
public class CiAuditUserRecordServiceImpl implements CiAuditUserRecordService {

    private static final String DEVOPS_AUDIT_USER_RECORD_UPDATE = "devops.audit.user.record.update";
    private static final String DEVOPS_AUDIT_RECORD_ID_IS_NULL = "devops.audit.record.id.is.null";

    @Autowired
    private CiAuditUserRecordMapper ciAuditUserRecordMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initAuditRecord(Long ciPipelineId, Long auditRecordId, List<Long> cdAuditUserIds) {
        if (!CollectionUtils.isEmpty(cdAuditUserIds)) {
            List<CiAuditUserRecordDTO> auditUserRecordDTOS = cdAuditUserIds
                    .stream()
                    .map(v -> new CiAuditUserRecordDTO(ciPipelineId, auditRecordId, v, AuditStatusEnum.NOT_AUDIT.value())).collect(Collectors.toList());
            ciAuditUserRecordMapper.insertList(auditUserRecordDTOS);
        }

    }

    @Override
    public List<CiAuditUserRecordDTO> listByAuditRecordId(Long auditRecordId) {
        Assert.notNull(auditRecordId, DEVOPS_AUDIT_RECORD_ID_IS_NULL);

        CiAuditUserRecordDTO ciAuditUserRecordDTO = new CiAuditUserRecordDTO();
        ciAuditUserRecordDTO.setAuditRecordId(auditRecordId);

        return ciAuditUserRecordMapper.select(ciAuditUserRecordDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseUpdate(CiAuditUserRecordDTO ciAuditUserRecordDTO) {
        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(ciAuditUserRecordMapper, ciAuditUserRecordDTO, DEVOPS_AUDIT_USER_RECORD_UPDATE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByCiPipelineId(Long pipelineId) {
        Assert.notNull(pipelineId, PipelineCheckConstant.DEVOPS_PIPELINE_ID_IS_NULL);

        CiAuditUserRecordDTO ciAuditUserRecordDTO = new CiAuditUserRecordDTO();
        ciAuditUserRecordDTO.setCiPipelineId(pipelineId);
        ciAuditUserRecordMapper.delete(ciAuditUserRecordDTO);
    }
}

