package io.choerodon.devops.app.eventhandler.pipeline.job;

import static io.choerodon.devops.infra.constant.ExceptionConstants.CiHostDeployCode.DEVOPS_HOST_DEPLOY_INFO_CREATE;
import static io.choerodon.devops.infra.constant.ExceptionConstants.CiJobCode.DEVOPS_JOB_CONFIG_ID_IS_NULL;
import static io.choerodon.devops.infra.constant.PipelineCheckConstant.DEVOPS_JOB_ID_IS_NULL;
import static io.choerodon.devops.infra.constant.ResourceCheckConstant.DEVOPS_ORGANIZATION_ID_IS_NULL;
import static io.choerodon.devops.infra.constant.ResourceCheckConstant.DEVOPS_PROJECT_ID_IS_NULL;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import io.choerodon.devops.api.vo.DevopsCiJobVO;
import io.choerodon.devops.api.vo.pipeline.DevopsCiHostDeployInfoVO;
import io.choerodon.devops.app.service.DevopsHostAppService;
import io.choerodon.devops.infra.dto.DevopsCiHostDeployInfoDTO;
import io.choerodon.devops.infra.dto.DevopsCiJobDTO;
import io.choerodon.devops.infra.enums.CiCommandTypeEnum;
import io.choerodon.devops.infra.enums.CiJobTypeEnum;
import io.choerodon.devops.infra.mapper.DevopsCiApiTestInfoMapper;
import io.choerodon.devops.infra.mapper.DevopsCiHostDeployInfoMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.MapperUtil;

@Service
public class HostDeployJobHandlerImpl extends AbstractJobHandler {
    @Autowired
    DevopsCiApiTestInfoMapper devopsCiApiTestInfoMapper;

    @Autowired
    private DevopsHostAppService devopsHostAppService;
    @Autowired
    private DevopsCiHostDeployInfoMapper devopsCiHostDeployInfoMapper;

    @Override
    public CiJobTypeEnum getType() {
        return CiJobTypeEnum.HOST_DEPLOY;
    }

    @Override
    protected void checkConfigInfo(Long projectId, DevopsCiJobVO devopsCiJobVO) {
        DevopsCiHostDeployInfoVO devopsCiHostDeployInfoVO = devopsCiJobVO.getDevopsCiHostDeployInfoVO();
        devopsHostAppService.checkNameAndCodeUniqueAndThrow(projectId,
                devopsCiHostDeployInfoVO.getAppId(),
                devopsCiHostDeployInfoVO.getAppName(),
                devopsCiHostDeployInfoVO.getAppCode());
    }

    @Override
    public List<String> buildScript(Long organizationId, Long projectId, DevopsCiJobDTO devopsCiJobDTO) {
        Assert.notNull(devopsCiJobDTO, "Job can't be null");
        Assert.notNull(organizationId, DEVOPS_ORGANIZATION_ID_IS_NULL);
        Assert.notNull(projectId, DEVOPS_PROJECT_ID_IS_NULL);
        final Long jobId = devopsCiJobDTO.getId();
        Assert.notNull(jobId, DEVOPS_JOB_ID_IS_NULL);
        Assert.notNull(devopsCiJobDTO.getConfigId(), DEVOPS_JOB_CONFIG_ID_IS_NULL);
        List<String> result = new ArrayList<>();
        result.add(String.format("host_deploy %s %s", devopsCiJobDTO.getConfigId(), CiCommandTypeEnum.HOST_DEPLOY.value()));
        return result;
    }

    @Override
    protected Long saveConfig(DevopsCiJobVO devopsCiJobVO) {
        DevopsCiHostDeployInfoDTO devopsCiHostDeployInfoDTO = ConvertUtils.convertObject(devopsCiJobVO.getDevopsCiHostDeployInfoVO(), DevopsCiHostDeployInfoDTO.class);
        devopsCiHostDeployInfoDTO.setId(null);
        MapperUtil.resultJudgedInsert(devopsCiHostDeployInfoMapper, devopsCiHostDeployInfoDTO, DEVOPS_HOST_DEPLOY_INFO_CREATE);
        return devopsCiHostDeployInfoDTO.getId();
    }
}
