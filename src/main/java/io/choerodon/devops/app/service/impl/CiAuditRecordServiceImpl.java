package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.CiAuditRecordService;
import io.choerodon.devops.infra.dto.CiAuditRecordDTO;
import io.choerodon.devops.infra.mapper.CiAuditRecordMapper;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * ci 人工卡点审核记录表(CiAuditRecord)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2022-11-03 10:16:51
 */
@Service
public class CiAuditRecordServiceImpl implements CiAuditRecordService {

    private static final String DEVOPS_AUDIT_RECORD_SAVE = "devops.audit.record.save";

    @Autowired
    private CiAuditRecordMapper ciAuditRecordMapper;

    @Override
    public CiAuditRecordDTO queryByUniqueOption(Long appServiceId, Long gitlabPipelineId, String name) {
        CiAuditRecordDTO ciAuditRecordDTO = new CiAuditRecordDTO(appServiceId,
                gitlabPipelineId,
                name);
        return ciAuditRecordMapper.selectOne(ciAuditRecordDTO);
    }

    @Override
    public CiAuditRecordDTO baseCreate(CiAuditRecordDTO ciAuditRecordDTO) {
        return MapperUtil.resultJudgedInsertSelective(ciAuditRecordMapper, ciAuditRecordDTO, DEVOPS_AUDIT_RECORD_SAVE);
    }
}

