package io.choerodon.devops.app.service.impl.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.core.utils.ConvertUtils;
import io.choerodon.devops.api.vo.template.CiTemplateJobVO;
import io.choerodon.devops.api.vo.template.CiTplApiTestInfoCfgVO;
import io.choerodon.devops.infra.dto.CiTplApiTestInfoCfgDTO;
import io.choerodon.devops.infra.mapper.CiTplApiTestInfoCfgMapper;

@Service
public class ApiTestTemplateJobConfigService extends TemplateJobConfigService {

    @Autowired
    private CiTplApiTestInfoCfgMapper ciTplApiTestInfoCfgMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long baseInsert(CiTemplateJobVO ciTemplateJobVO) {
        CiTplApiTestInfoCfgDTO ciTplApiTestInfoCfgDTO = ConvertUtils.convertObject(ciTemplateJobVO.getDevopsCiApiTestInfoVO(), CiTplApiTestInfoCfgDTO.class);
        ciTplApiTestInfoCfgDTO.setId(null);
        ciTplApiTestInfoCfgMapper.insertSelective(ciTplApiTestInfoCfgDTO);
        return ciTplApiTestInfoCfgDTO.getId();
    }

    @Override
    public void fillCdJobConfig(CiTemplateJobVO ciTemplateJobVO) {
        CiTplApiTestInfoCfgVO ciTplApiTestInfoCfgVO = fillAPITestConfig(ciTemplateJobVO.getConfigId());
        ciTemplateJobVO.setDevopsCiApiTestInfoVO(ciTplApiTestInfoCfgVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseDelete(CiTemplateJobVO ciTemplateJobVO) {
        if (ciTemplateJobVO.getConfigId() == null) {
            return;
        }
        ciTplApiTestInfoCfgMapper.deleteByPrimaryKey(ciTemplateJobVO.getConfigId());
    }

    private CiTplApiTestInfoCfgVO fillAPITestConfig(Long configId) {
        if (configId == null) {
            return new CiTplApiTestInfoCfgVO();
        }
        return ConvertUtils.convertObject(ciTplApiTestInfoCfgMapper.selectByPrimaryKey(configId), CiTplApiTestInfoCfgVO.class);
    }
}
