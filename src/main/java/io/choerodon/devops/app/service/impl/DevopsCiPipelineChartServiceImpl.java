package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.devops.app.service.DevopsCiPipelineChartService;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.constant.ResourceCheckConstant;
import io.choerodon.devops.infra.dto.DevopsCiPipelineChartDTO;
import io.choerodon.devops.infra.mapper.DevopsCiPipelineChartMapper;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * ci任务生成chart记录(DevopsCiPipelineChart)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-15 17:35:12
 */
@Service
public class DevopsCiPipelineChartServiceImpl implements DevopsCiPipelineChartService {
    @Autowired
    private DevopsCiPipelineChartMapper devopsCiPipelineChartMapper;


    @Override
    public DevopsCiPipelineChartDTO queryByPipelineIdAndJobName(Long appServiceId, Long gitlabPipelineId, String jobName) {
        Assert.notNull(appServiceId, ResourceCheckConstant.ERROR_APP_SERVICE_ID_IS_NULL);
        Assert.notNull(gitlabPipelineId, PipelineCheckConstant.DEVOPS_GITLAB_PIPELINE_ID_IS_NULL);
        Assert.notNull(jobName, PipelineCheckConstant.DEVOPS_JOB_NAME_IS_NULL);

        DevopsCiPipelineChartDTO devopsCiPipelineChartDTO = new DevopsCiPipelineChartDTO();
        devopsCiPipelineChartDTO.setGitlabPipelineId(gitlabPipelineId);
        devopsCiPipelineChartDTO.setAppServiceId(appServiceId);
        devopsCiPipelineChartDTO.setJobName(jobName);
        return devopsCiPipelineChartMapper.selectOne(devopsCiPipelineChartDTO);
    }

    @Override
    @Transactional
    public void baseCreate(DevopsCiPipelineChartDTO devopsCiPipelineChartDTO) {
        MapperUtil.resultJudgedInsertSelective(devopsCiPipelineChartMapper, devopsCiPipelineChartDTO, "error.save.chart.info");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByAppServiceId(Long appServiceId) {
        Assert.notNull(appServiceId, ResourceCheckConstant.ERROR_APP_SERVICE_ID_IS_NULL);

        DevopsCiPipelineChartDTO devopsCiPipelineChartDTO = new DevopsCiPipelineChartDTO();
        devopsCiPipelineChartDTO.setAppServiceId(appServiceId);

        devopsCiPipelineChartMapper.delete(devopsCiPipelineChartDTO);
    }
}

