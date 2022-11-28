package io.choerodon.devops.app.eventhandler.cd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.devops.api.vo.cd.PipelineChartDeployCfgVO;
import io.choerodon.devops.api.vo.cd.PipelineJobVO;
import io.choerodon.devops.app.service.PipelineChartDeployCfgService;
import io.choerodon.devops.infra.dto.PipelineChartDeployCfgDTO;
import io.choerodon.devops.infra.enums.cd.CdJobTypeEnum;
import io.choerodon.devops.infra.util.ConvertUtils;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/11/24 16:50
 */
@Component
public class CdChartDeployJobHandlerImpl extends AbstractCdJobHandler {

    @Autowired
    private PipelineChartDeployCfgService pipelineChartDeployCfgService;

    @Override
    protected void checkConfigInfo(Long projectId, PipelineJobVO pipelineJobVO) {
        super.checkConfigInfo(projectId, pipelineJobVO);
    }

    @Override
    public void deleteConfigByPipelineId(Long pipelineId) {
        pipelineChartDeployCfgService.deleteConfigByPipelineId(pipelineId);
    }


    @Override
    protected Long saveConfig(Long pipelineId, PipelineJobVO devopsCiJobVO) {
        PipelineChartDeployCfgVO chartDeployCfg = devopsCiJobVO.getChartDeployCfg();
        PipelineChartDeployCfgDTO pipelineChartDeployCfgDTO = ConvertUtils.convertObject(chartDeployCfg, PipelineChartDeployCfgDTO.class);
        pipelineChartDeployCfgDTO.setId(null);
        pipelineChartDeployCfgDTO.setPipelineId(pipelineId);

        pipelineChartDeployCfgService.baseCreate(pipelineChartDeployCfgDTO);
        return pipelineChartDeployCfgDTO.getId();
    }

    @Override
    public void fillJobConfigInfo(PipelineJobVO pipelineJobVO) {
        pipelineJobVO.setChartDeployCfg(pipelineChartDeployCfgService.queryVoByConfigId(pipelineJobVO.getConfigId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void execCommand(Long jobRecordId, StringBuffer log) {


    }

    @Override
    public CdJobTypeEnum getType() {
        return CdJobTypeEnum.CHART_DEPLOY;
    }
}
