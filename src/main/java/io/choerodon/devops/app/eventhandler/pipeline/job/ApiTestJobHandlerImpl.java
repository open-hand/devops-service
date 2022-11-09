package io.choerodon.devops.app.eventhandler.pipeline.job;

import static io.choerodon.devops.infra.constant.ExceptionConstants.CiApiTestCode.DEVOPS_CI_API_TEST_INFO_SAVE;
import static io.choerodon.devops.infra.constant.ExceptionConstants.CiApiTestCode.DEVOPS_CI_API_TEST_INFO_TYPE_UNKNOWN;
import static io.choerodon.devops.infra.constant.PipelineCheckConstant.DEVOPS_JOB_ID_IS_NULL;
import static io.choerodon.devops.infra.constant.ResourceCheckConstant.DEVOPS_ORGANIZATION_ID_IS_NULL;
import static io.choerodon.devops.infra.constant.ResourceCheckConstant.DEVOPS_PROJECT_ID_IS_NULL;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsCiJobVO;
import io.choerodon.devops.app.service.DevopsCiApiTestInfoService;
import io.choerodon.devops.infra.constant.ExceptionConstants;
import io.choerodon.devops.infra.dto.DevopsCiApiTestInfoDTO;
import io.choerodon.devops.infra.dto.DevopsCiJobDTO;
import io.choerodon.devops.infra.enums.CiJobTypeEnum;
import io.choerodon.devops.infra.enums.test.ApiTestTaskType;
import io.choerodon.devops.infra.mapper.DevopsCiApiTestInfoMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.JsonHelper;
import io.choerodon.devops.infra.util.KeyDecryptHelper;
import io.choerodon.devops.infra.util.MapperUtil;

@Service
public class ApiTestJobHandlerImpl extends AbstractJobHandler {

    private static final String API_TEST_COMMAND_TEMPLATE = "execute_api_test %s %s %s %s";
    private static final String SUITE_TEST_COMMAND_TEMPLATE = "execute_api_test %s %s %s";

    public static final Integer MAX_DELAY_MINUTE = 7 * 24 * 60;

    @Value("${services.gateway.url}")
    private String apiGateway;
    @Autowired
    DevopsCiApiTestInfoMapper devopsCiApiTestInfoMapper;
    @Autowired
    private
    DevopsCiApiTestInfoService devopsCiApiTestInfoService;

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
                result.add(String.format(API_TEST_COMMAND_TEMPLATE, "api", apiGateway, devopsCiApiTestInfoDTO.getApiTestTaskId(), devopsCiApiTestInfoDTO.getApiTestConfigId()));
                break;
            case SUITE:
                result.add(String.format(SUITE_TEST_COMMAND_TEMPLATE, "suite", apiGateway, devopsCiApiTestInfoDTO.getApiTestSuiteId()));
                break;
            default:
                throw new CommonException(DEVOPS_CI_API_TEST_INFO_TYPE_UNKNOWN, devopsCiApiTestInfoDTO.getTaskType());
        }

        return result;
    }

    @Override
    protected void checkConfigInfo(Long projectId, DevopsCiJobVO devopsCiJobVO) {
        if (devopsCiJobVO.getStartIn() != null && (devopsCiJobVO.getStartIn() < 1 || devopsCiJobVO.getStartIn() > MAX_DELAY_MINUTE)) {
            throw new CommonException(ExceptionConstants.CiJobCode.DEVOPS_CI_JOB_DELAY_TIME_INVALID);
        }
    }

    @Override
    protected Long saveConfig(DevopsCiJobVO devopsCiJobVO) {
        DevopsCiApiTestInfoDTO devopsCiApiTestInfoDTO = ConvertUtils.convertObject(devopsCiJobVO.getDevopsCiApiTestInfoVO(), DevopsCiApiTestInfoDTO.class);
        List<Long> notifyUserIds = KeyDecryptHelper.decryptJson(devopsCiApiTestInfoDTO.getNotifyUserIds(), new TypeReference<List<Long>>() {
        });
        devopsCiApiTestInfoDTO.setNotifyUserIds(JsonHelper.marshalByJackson(notifyUserIds));
        devopsCiApiTestInfoDTO.setId(null);
        MapperUtil.resultJudgedInsert(devopsCiApiTestInfoMapper, devopsCiApiTestInfoDTO, DEVOPS_CI_API_TEST_INFO_SAVE);
        return devopsCiApiTestInfoDTO.getId();
    }

    @Override
    public void fillJobConfigInfo(DevopsCiJobVO devopsCiJobVO) {
        devopsCiJobVO.setDevopsCiApiTestInfoVO(devopsCiApiTestInfoService.selectByPrimaryKey(devopsCiJobVO.getConfigId()));
    }
}
