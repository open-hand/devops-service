package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.CiTemplateAuditUserService;
import io.choerodon.devops.infra.dto.CiTemplateAuditUserDTO;
import io.choerodon.devops.infra.mapper.CiTemplateAuditUserMapper;

/**
 * ci 人工卡点审核人员表(CiTemplateAuditUser)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2022-11-02 14:39:57
 */
@Service
public class CiTemplateAuditUserServiceImpl implements CiTemplateAuditUserService {
    @Autowired
    private CiTemplateAuditUserMapper ciTemplateAuditUserMapper;

    @Override
    public List<CiTemplateAuditUserDTO> listByConfigId(Long configId) {
        CiTemplateAuditUserDTO ciTemplateAuditUserDTO = new CiTemplateAuditUserDTO();
        ciTemplateAuditUserDTO.setAuditConfigId(configId);
        return ciTemplateAuditUserMapper.select(ciTemplateAuditUserDTO);
    }
}

