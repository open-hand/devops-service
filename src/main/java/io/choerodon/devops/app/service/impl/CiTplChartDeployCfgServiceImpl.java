package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.api.vo.pipeline.CiChartDeployConfigVO;
import io.choerodon.devops.app.service.CiTplChartDeployCfgService;
import io.choerodon.devops.infra.dto.CiTplChartDeployCfgDTO;
import io.choerodon.devops.infra.mapper.CiTplChartDeployCfgMapper;
import io.choerodon.devops.infra.util.ConvertUtils;

/**
 * 流水线模板 chart部署任务配置表(CiTplChartDeployCfg)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2022-11-04 15:04:59
 */
@Service
public class CiTplChartDeployCfgServiceImpl implements CiTplChartDeployCfgService {
    @Autowired
    private CiTplChartDeployCfgMapper ciTplChartDeployCfgMapper;

    @Override
    public CiChartDeployConfigVO queryConfigVoById(Long id) {
        return ConvertUtils.convertObject(queryConfigById(id), CiChartDeployConfigVO.class);
    }

    @Override
    public CiTplChartDeployCfgDTO queryConfigById(Long id) {
        return ciTplChartDeployCfgMapper.selectByPrimaryKey(id);
    }
}

