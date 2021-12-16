package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.devops.app.service.AppServiceService;
import io.choerodon.devops.app.service.DevopsCiPipelineService;
import io.choerodon.devops.app.service.DevopsCiPipelineSonarService;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.constant.ResourceCheckConstant;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.CiCdPipelineDTO;
import io.choerodon.devops.infra.dto.DevopsCiPipelineSonarDTO;
import io.choerodon.devops.infra.mapper.DevopsCiPipelineSonarMapper;
import io.choerodon.devops.infra.util.ExceptionUtil;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * ci任务生成sonar记录(DevopsCiPipelineSonar)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-16 17:56:59
 */
@Service
public class DevopsCiPipelineSonarServiceImpl implements DevopsCiPipelineSonarService {

    @Autowired
    private AppServiceService appServiceService;
    @Autowired
    private DevopsCiPipelineService devopsCiPipelineService;

    @Autowired
    private DevopsCiPipelineSonarMapper devopsCiPipelineSonarMapper;


    @Override
    @Transactional
    public void saveSonarInfo(Long jobId, Long gitlabPipelineId, String jobName, String token, String scannerType) {
        ExceptionUtil.wrapExWithCiEx(() -> {
            AppServiceDTO appServiceDTO = appServiceService.baseQueryByToken(token);
            CiCdPipelineDTO ciCdPipelineDTO = devopsCiPipelineService.queryByAppSvcId(appServiceDTO.getId());
            Long devopsPipelineId = ciCdPipelineDTO.getId();
            DevopsCiPipelineSonarDTO devopsCiPipelineSonarDTO = queryByPipelineId(devopsPipelineId, gitlabPipelineId, jobName);
            if (devopsCiPipelineSonarDTO == null) {
                baseCreate(new DevopsCiPipelineSonarDTO(devopsPipelineId, gitlabPipelineId, jobName));
            }

        });
    }

    @Override
    public DevopsCiPipelineSonarDTO queryByPipelineId(Long devopsPipelineId, Long gitlabPipelineId, String jobName) {
        Assert.notNull(devopsPipelineId, PipelineCheckConstant.ERROR_PIPELINE_IS_NULL);
        Assert.notNull(gitlabPipelineId, ResourceCheckConstant.ERROR_GITLAB_PIPELINE_ID_IS_NULL);
        Assert.notNull(jobName, ResourceCheckConstant.ERROR_JOB_NAME_ID_IS_NULL);

        DevopsCiPipelineSonarDTO devopsCiPipelineSonarDTO = new DevopsCiPipelineSonarDTO(devopsPipelineId, gitlabPipelineId, jobName);
        return devopsCiPipelineSonarMapper.selectOne(devopsCiPipelineSonarDTO);
    }

    @Override
    @Transactional
    public void baseCreate(DevopsCiPipelineSonarDTO devopsCiPipelineSonarDTO) {
        MapperUtil.resultJudgedInsertSelective(devopsCiPipelineSonarMapper, devopsCiPipelineSonarDTO, "error.save.sonar.info");
    }
}

