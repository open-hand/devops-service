package io.choerodon.devops.app.service.impl.config;

import io.choerodon.core.utils.ConvertUtils;
import io.choerodon.devops.api.vo.template.CiTemplateJobVO;
import io.choerodon.devops.api.vo.template.CiTplDeployDeployCfgVO;
import io.choerodon.devops.infra.dto.CiTplDeployDeployCfgDTO;
import io.choerodon.devops.infra.mapper.CiTplDeployDeployCfgMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeploymentTemplateJobConfigService extends TemplateJobConfigService {

    @Autowired
    private CiTplDeployDeployCfgMapper ciTplDeployDeployCfgMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long baseInsert(CiTemplateJobVO ciTemplateJobVO) {
        CiTplDeployDeployCfgDTO ciTplDeployDeployCfgDTO = ConvertUtils.convertObject(ciTemplateJobVO.getCiDeployDeployCfg(), CiTplDeployDeployCfgDTO.class);
        if (ciTplDeployDeployCfgDTO == null) {
            return null;
        }
        ciTplDeployDeployCfgDTO.setId(null);
        ciTplDeployDeployCfgMapper.insertSelective(ciTplDeployDeployCfgDTO);
        return ciTplDeployDeployCfgDTO.getId();
    }

    @Override
    public void fillCdJobConfig(CiTemplateJobVO ciTemplateJobVO) {
        CiTplDeployDeployCfgVO ciTplDeployDeployCfgVO = fillDeploymentDeployConfig(ciTemplateJobVO.getConfigId());
        ciTemplateJobVO.setCiDeployDeployCfg(ciTplDeployDeployCfgVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseDelete(CiTemplateJobVO ciTemplateJobVO) {
        if (ciTemplateJobVO.getConfigId() == null) {
            return;
        }
        ciTplDeployDeployCfgMapper.deleteByPrimaryKey(ciTemplateJobVO.getConfigId());
    }

    private CiTplDeployDeployCfgVO fillDeploymentDeployConfig(Long configId) {
        if (configId == null) {
            return new CiTplDeployDeployCfgVO();
        }
        return ConvertUtils.convertObject(ciTplDeployDeployCfgMapper.selectByPrimaryKey(configId), CiTplDeployDeployCfgVO.class);
    }

}
