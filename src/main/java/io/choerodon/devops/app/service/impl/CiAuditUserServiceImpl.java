package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import io.choerodon.devops.app.service.CiAuditUserService;
import io.choerodon.devops.infra.dto.CiAuditUserDTO;
import io.choerodon.devops.infra.mapper.CiAuditUserMapper;

/**
 * ci 人工卡点审核人员表(CiAuditUser)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2022-11-02 11:40:41
 */
@Service
public class CiAuditUserServiceImpl implements CiAuditUserService {
    @Autowired
    private CiAuditUserMapper ciAuditUserMapper;

    @Override
    public List<CiAuditUserDTO> listByAuditConfigId(Long configId) {
        CiAuditUserDTO ciAuditUserDTO = new CiAuditUserDTO();
        ciAuditUserDTO.setAuditConfigId(configId);
        return ciAuditUserMapper.select(ciAuditUserDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchCreateByConfigIdAndUserIds(Long configId, List<Long> cdAuditUserIds) {
        List<CiAuditUserDTO> auditUserList = cdAuditUserIds.stream().map(u -> {
            CiAuditUserDTO ciAuditUserDTO = new CiAuditUserDTO();
            ciAuditUserDTO.setAuditConfigId(configId);
            ciAuditUserDTO.setUserId(u);
            return ciAuditUserDTO;
        }).collect(Collectors.toList());

        ciAuditUserMapper.insertList(auditUserList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteByConfigIds(List<Long> configIds) {
        if (!ObjectUtils.isEmpty(configIds)) {
            ciAuditUserMapper.batchDeleteByConfigIds(configIds);
        }
    }
}

