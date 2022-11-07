package io.choerodon.devops.app.eventhandler.pipeline.job;

import static io.choerodon.devops.infra.constant.ExceptionConstants.CiApiTestCode.DEVOPS_CI_API_TEST_INFO_SAVE;
import static io.choerodon.devops.infra.constant.ExceptionConstants.CiApiTestCode.DEVOPS_CI_API_TEST_INFO_TYPE_UNKNOWN;
import static io.choerodon.devops.infra.constant.PipelineCheckConstant.DEVOPS_JOB_ID_IS_NULL;
import static io.choerodon.devops.infra.constant.ResourceCheckConstant.DEVOPS_ORGANIZATION_ID_IS_NULL;
import static io.choerodon.devops.infra.constant.ResourceCheckConstant.DEVOPS_PROJECT_ID_IS_NULL;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsCiJobVO;
import io.choerodon.devops.infra.dto.DevopsCiApiTestInfoDTO;
import io.choerodon.devops.infra.dto.DevopsCiJobDTO;
import io.choerodon.devops.infra.enums.CiJobTypeEnum;
import io.choerodon.devops.infra.enums.test.ApiTestTaskType;
import io.choerodon.devops.infra.mapper.DevopsCiApiTestInfoMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.MapperUtil;

@Service
public class ApiTestJobHandlerImpl extends AbstractJobHandler {

    private static final String API_TEST_COMMAND_TEMPLATE = "execute_api_test %s %s %s %s";
    private static final String SUITE_TEST_COMMAND_TEMPLATE = "execute_api_test %s %s %s";

    @Value("${services.test.url}")
    private String testManagerServiceUrl;
    @Autowired
    DevopsCiApiTestInfoMapper devopsCiApiTestInfoMapper;

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
        List<String> result = new ArrayList<>();
        DevopsCiApiTestInfoDTO devopsCiApiTestInfoDTO = devopsCiApiTestInfoMapper.selectByPrimaryKey(devopsCiJobDTO.getConfigId());
        switch (ApiTestTaskType.valueOf(devopsCiApiTestInfoDTO.getTaskType().toUpperCase())) {
            case TASK:
                result.add(String.format(API_TEST_COMMAND_TEMPLATE, "api", testManagerServiceUrl, devopsCiApiTestInfoDTO.getApiTestTaskId(), devopsCiApiTestInfoDTO.getApiTestConfigId()));
                break;
            case SUITE:
                result.add(String.format(SUITE_TEST_COMMAND_TEMPLATE, "suite", testManagerServiceUrl, devopsCiApiTestInfoDTO.getApiTestSuiteId()));
                break;
            default:
                throw new CommonException(DEVOPS_CI_API_TEST_INFO_TYPE_UNKNOWN, devopsCiApiTestInfoDTO.getTaskType());
        }

        return result;
    }

    @Override
    protected Long saveConfig(DevopsCiJobVO devopsCiJobVO) {
        DevopsCiApiTestInfoDTO devopsCiApiTestInfoDTO = ConvertUtils.convertObject(devopsCiJobVO.getDevopsCiApiTestInfoVO(), DevopsCiApiTestInfoDTO.class);
        devopsCiApiTestInfoDTO.setId(null);
        MapperUtil.resultJudgedInsert(devopsCiApiTestInfoMapper, devopsCiApiTestInfoDTO, DEVOPS_CI_API_TEST_INFO_SAVE);
        return devopsCiApiTestInfoDTO.getId();
    }
}
