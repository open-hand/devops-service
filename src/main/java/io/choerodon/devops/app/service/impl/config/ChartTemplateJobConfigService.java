package io.choerodon.devops.app.service.impl.config;

import io.choerodon.core.utils.ConvertUtils;
import io.choerodon.devops.api.vo.template.CiTemplateJobVO;
import io.choerodon.devops.api.vo.template.CiTplChartDeployCfgVO;
import io.choerodon.devops.infra.dto.CiTplChartDeployCfgDTO;
import io.choerodon.devops.infra.mapper.CiTplChartDeployCfgMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChartTemplateJobConfigService extends TemplateJobConfigService {

    @Autowired
    private CiTplChartDeployCfgMapper ciTplChartDeployCfgMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long baseInsert(CiTemplateJobVO ciTemplateJobVO) {
        CiTplChartDeployCfgDTO ciTplChartDeployCfgDTO = ConvertUtils.
                convertObject(ciTemplateJobVO.getCiChartDeployConfig(), CiTplChartDeployCfgDTO.class);
        if (ciTplChartDeployCfgDTO == null) {
            return null;
        }
        ciTplChartDeployCfgDTO.setId(null);
        ciTplChartDeployCfgMapper.insertSelective(ciTplChartDeployCfgDTO);
        return ciTplChartDeployCfgDTO.getId();
    }

    @Override
    public void fillCdJobConfig(CiTemplateJobVO ciTemplateJobVO) {
        CiTplChartDeployCfgVO ciTplChartDeployCfgVO = fillChartDeployConfig(ciTemplateJobVO.getConfigId());
        ciTemplateJobVO.setCiChartDeployConfig(ciTplChartDeployCfgVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseDelete(CiTemplateJobVO ciTemplateJobVO) {
        if (ciTemplateJobVO.getConfigId() == null) {
            return;
        }
        ciTplChartDeployCfgMapper.deleteByPrimaryKey(ciTemplateJobVO.getConfigId());
    }

    private CiTplChartDeployCfgVO fillChartDeployConfig(Long configId) {
        if (configId == null) {
            return new CiTplChartDeployCfgVO();
        }
        return ConvertUtils.convertObject(ciTplChartDeployCfgMapper
                .selectByPrimaryKey(configId), CiTplChartDeployCfgVO.class);
    }
}
