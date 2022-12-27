package io.choerodon.devops.app.service.impl.config;

import io.choerodon.core.utils.ConvertUtils;
import io.choerodon.devops.api.vo.template.CiTemplateJobVO;
import io.choerodon.devops.api.vo.template.CiTplAuditVO;
import io.choerodon.devops.infra.dto.CiTemplateAuditDTO;
import io.choerodon.devops.infra.mapper.CiTemplateAuditMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditTemplateJobConfigService extends TemplateJobConfigService {
    @Autowired
    private CiTemplateAuditMapper ciTemplateAuditMapper;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long baseInsert(CiTemplateJobVO ciTemplateJobVO) {
        CiTemplateAuditDTO ciTemplateAuditDTO = ConvertUtils.convertObject(ciTemplateJobVO.getCiAuditConfig(), CiTemplateAuditDTO.class);
        if (ciTemplateAuditDTO == null) {
            return null;
        }
        ciTemplateAuditDTO.setId(null);
        ciTemplateAuditMapper.insertSelective(ciTemplateAuditDTO);
        return ciTemplateAuditDTO.getId();
    }

    @Override
    public void fillCdJobConfig(CiTemplateJobVO ciTemplateJobVO) {
        CiTplAuditVO ciTplAuditVO = fillAuditConfig(ciTemplateJobVO.getConfigId());
        ciTemplateJobVO.setCiAuditConfig(ciTplAuditVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseDelete(CiTemplateJobVO ciTemplateJobVO) {
        if (ciTemplateJobVO.getConfigId() == null) {
            return;
        }
        ciTemplateAuditMapper.deleteByPrimaryKey(ciTemplateJobVO.getConfigId());
    }

    private CiTplAuditVO fillAuditConfig(Long configId) {
        if (configId == null) {
            return new CiTplAuditVO();
        }
        return ConvertUtils.convertObject(ciTemplateAuditMapper.selectByPrimaryKey(configId), CiTplAuditVO.class);
    }
}
