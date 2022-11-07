package io.choerodon.devops.app.eventhandler.pipeline.job;

import static io.choerodon.devops.infra.constant.ExceptionConstants.CiApiTestCode.DEVOPS_CI_API_TEST_INFO_CREATE;
import static io.choerodon.devops.infra.constant.PipelineCheckConstant.DEVOPS_JOB_ID_IS_NULL;
import static io.choerodon.devops.infra.constant.ResourceCheckConstant.DEVOPS_ORGANIZATION_ID_IS_NULL;
import static io.choerodon.devops.infra.constant.ResourceCheckConstant.DEVOPS_PROJECT_ID_IS_NULL;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import io.choerodon.devops.api.vo.DevopsCiJobVO;
import io.choerodon.devops.app.eventhandler.pipeline.step.AbstractDevopsCiStepHandler;
import io.choerodon.devops.app.service.DevopsCiStepService;
import io.choerodon.devops.app.service.impl.DevopsCiStepOperator;
import io.choerodon.devops.infra.dto.DevopsCiApiTestInfoDTO;
import io.choerodon.devops.infra.dto.DevopsCiJobDTO;
import io.choerodon.devops.infra.dto.DevopsCiStepDTO;
import io.choerodon.devops.infra.enums.CiJobTypeEnum;
import io.choerodon.devops.infra.mapper.DevopsCiApiTestInfoMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.MapperUtil;

@Service
public class ApiTestJobHandlerImpl extends AbstractJobHandler {

    private static final String API_TEST_COMMAND_TEMPLATE = "environment=runner choerodonUrl=%s configId=%s type=%s taskId=%s";
    private static final String SUITE_TEST_COMMAND_TEMPLATE = "environment=runner choerodonUrl=%s configId=%s type=%s suiteId=%s";

    @Autowired
    DevopsCiApiTestInfoMapper devopsCiApiTestInfoMapper;
    @Autowired
    private DevopsCiStepService devopsCiStepService;
    @Autowired
    private DevopsCiStepOperator devopsCiStepOperator;

    @Override
    public CiJobTypeEnum getType() {
        return CiJobTypeEnum.API_TEST;
    }

    @Override
    public List<String> buildScript(Long organizationId, Long projectId, DevopsCiJobDTO devopsCiJobDTO) {
        Assert.notNull(devopsCiJobDTO, "Job can't be null");
        Assert.notNull(organizationId, DEVOPS_ORGANIZATION_ID_IS_NULL);
        Assert.notNull(projectId, DEVOPS_PROJECT_ID_IS_NULL);
        final Long jobId = devopsCiJobDTO.getId();
        Assert.notNull(jobId, DEVOPS_JOB_ID_IS_NULL);

        List<DevopsCiStepDTO> devopsCiStepDTOS = devopsCiStepService.listByJobId(jobId);

        if (CollectionUtils.isEmpty(devopsCiStepDTOS)) {
            return null;
        }
        // 最后生成的所有script集合
        List<String> result = new ArrayList<>();
        devopsCiStepDTOS.stream().sorted(Comparator.comparingLong(DevopsCiStepDTO::getSequence)).forEach(devopsCiStepDTO -> {
            AbstractDevopsCiStepHandler handler = devopsCiStepOperator.getHandlerOrThrowE(devopsCiStepDTO.getType());
            result.addAll(handler.buildGitlabCiScript(devopsCiStepDTO));
        });
        return result;
    }

    @Override
    protected Long saveConfig(DevopsCiJobVO devopsCiJobVO) {
        DevopsCiApiTestInfoDTO devopsCiApiTestInfoDTO = ConvertUtils.convertObject(devopsCiJobVO.getCiApiTestConfigVO(), DevopsCiApiTestInfoDTO.class);
        MapperUtil.resultJudgedInsert(devopsCiApiTestInfoMapper, devopsCiApiTestInfoDTO, DEVOPS_CI_API_TEST_INFO_CREATE);
        return devopsCiApiTestInfoDTO.getId();
    }
}
