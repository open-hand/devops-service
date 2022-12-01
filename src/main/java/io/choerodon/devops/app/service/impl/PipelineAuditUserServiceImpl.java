package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.devops.app.service.PipelineAuditUserService;
import io.choerodon.devops.infra.dto.PipelineAuditUserDTO;
import io.choerodon.devops.infra.mapper.PipelineAuditUserMapper;

/**
 * 人工卡点审核人员表(PipelineAuditUser)应用服务
 *
 * @author
 * @since 2022-11-24 15:56:49
 */
@Service
public class PipelineAuditUserServiceImpl implements PipelineAuditUserService {

    private static final String DEVOPS_AUDIT_CONFIG_ID_IS_NULL = "devops.audit.config.id.is.null";

    @Autowired
    private PipelineAuditUserMapper pipelineAuditUserMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchCreateByConfigIdAndUserIds(Long configId, List<Long> auditUserIds) {
        List<PipelineAuditUserDTO> auditUserList = auditUserIds.stream().map(u -> {
            PipelineAuditUserDTO pipelineAuditUserDTO = new PipelineAuditUserDTO();
            pipelineAuditUserDTO.setAuditConfigId(configId);
            pipelineAuditUserDTO.setUserId(u);
            return pipelineAuditUserDTO;
        }).collect(Collectors.toList());

        pipelineAuditUserMapper.insertList(auditUserList);
    }

    @Override
    public List<PipelineAuditUserDTO> listByAuditConfigId(Long auditConfigId) {
        Assert.notNull(auditConfigId, DEVOPS_AUDIT_CONFIG_ID_IS_NULL);
        PipelineAuditUserDTO pipelineAuditUserDTO = new PipelineAuditUserDTO();
        pipelineAuditUserDTO.setAuditConfigId(auditConfigId);
        return pipelineAuditUserMapper.select(pipelineAuditUserDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteByConfigIds(List<Long> configIds) {
        pipelineAuditUserMapper.batchDeleteByConfigIds(configIds);
    }
}

