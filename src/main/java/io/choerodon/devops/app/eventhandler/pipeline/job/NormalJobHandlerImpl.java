package io.choerodon.devops.app.eventhandler.pipeline.job;

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

import io.choerodon.devops.app.eventhandler.pipeline.step.AbstractDevopsCiStepHandler;
import io.choerodon.devops.app.service.DevopsCiStepService;
import io.choerodon.devops.app.service.impl.DevopsCiStepOperator;
import io.choerodon.devops.infra.dto.DevopsCiJobDTO;
import io.choerodon.devops.infra.dto.DevopsCiStepDTO;
import io.choerodon.devops.infra.enums.CiJobTypeEnum;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/11/3 16:02
 */
@Service
public class NormalJobHandlerImpl extends AbstractJobHandler {

    @Autowired
    private DevopsCiStepService devopsCiStepService;
    @Autowired
    private DevopsCiStepOperator devopsCiStepOperator;

    @Override
    public CiJobTypeEnum getType() {
        return CiJobTypeEnum.NORMAL;
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
        devopsCiStepDTOS
                .stream()
                .sorted(Comparator.comparingLong(DevopsCiStepDTO::getSequence))
                .forEach(devopsCiStepDTO -> {
                    AbstractDevopsCiStepHandler handler = devopsCiStepOperator.getHandlerOrThrowE(devopsCiStepDTO.getType());
                    result.addAll(handler.buildGitlabCiScript(devopsCiStepDTO));
                });
        return result;
    }
}
