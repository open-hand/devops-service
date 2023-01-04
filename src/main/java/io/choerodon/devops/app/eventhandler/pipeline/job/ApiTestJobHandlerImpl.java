package io.choerodon.devops.app.eventhandler.pipeline.job;

import static io.choerodon.devops.infra.constant.ExceptionConstants.CiApiTestCode.DEVOPS_CI_API_TEST_INFO_TYPE_UNKNOWN;
import static io.choerodon.devops.infra.constant.PipelineCheckConstant.DEVOPS_JOB_ID_IS_NULL;
import static io.choerodon.devops.infra.constant.ResourceCheckConstant.DEVOPS_ORGANIZATION_ID_IS_NULL;
import static io.choerodon.devops.infra.constant.ResourceCheckConstant.DEVOPS_PROJECT_ID_IS_NULL;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsCiJobVO;
import io.choerodon.devops.api.vo.pipeline.DevopsCiApiTestInfoVO;
import io.choerodon.devops.app.service.DevopsCiApiTestInfoService;
import io.choerodon.devops.app.service.DevopsCiTplApiTestInfoCfgService;
import io.choerodon.devops.infra.constant.ExceptionConstants;
import io.choerodon.devops.infra.dto.CiTplApiTestInfoCfgDTO;
import io.choerodon.devops.infra.dto.DevopsCiApiTestInfoDTO;
import io.choerodon.devops.infra.dto.DevopsCiJobDTO;
import io.choerodon.devops.infra.enums.CiJobTypeEnum;
import io.choerodon.devops.infra.enums.test.ApiTestTaskType;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.KeyDecryptHelper;

@Service
public class ApiTestJobHandlerImpl extends AbstractJobHandler {

    /**
     * apiTestInfoConfigId 是devops_ci_job关联的DevopsCiApiTestInfo的id (devops_ci_api_test_info)
     * configId 是任务关联的测试任务配置id (api_test_task_config)
     */
    private static final String API_TEST_COMMAND_TEMPLATE = "token=${Token} environment=runner block=%s threshold=%s type=%s choerodonUrl=\"%s\" taskId=%s  apiTestInfoConfigId=%s configId=%s execute_api_test";
    private static final String SUITE_TEST_COMMAND_TEMPLATE = "token=${Token} environment=runner  block=%s threshold=%s type=%s choerodonUrl=\"%s\" suiteId=%s apiTestInfoConfigId=%s execute_api_test";

    public static final Integer MAX_DELAY_MINUTE = 7 * 24 * 60;

    @Value("${services.gateway.url}")
    private String apiGateway;
    @Autowired
    private
    DevopsCiApiTestInfoService devopsCiApiTestInfoService;
    @Autowired
    private DevopsCiTplApiTestInfoCfgService devopsCiTplApiTestInfoCfgService;

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
        DevopsCiApiTestInfoDTO devopsCiApiTestInfoDTO = devopsCiApiTestInfoService.selectByPrimaryKey(devopsCiJobDTO.getConfigId());
        switch (ApiTestTaskType.valueOf(devopsCiApiTestInfoDTO.getTaskType().toUpperCase())) {
            case TASK:
                result.add(String.format(API_TEST_COMMAND_TEMPLATE, Optional.ofNullable(devopsCiApiTestInfoDTO.getBlockAfterJob()).orElse(Boolean.FALSE), Optional.ofNullable(devopsCiApiTestInfoDTO.getPerformThreshold()).orElse(0d), "api", apiGateway, devopsCiApiTestInfoDTO.getApiTestTaskId(), devopsCiJobDTO.getConfigId(), devopsCiApiTestInfoDTO.getApiTestConfigId() == null ? "" : devopsCiApiTestInfoDTO.getApiTestConfigId()));
                break;
            case SUITE:
                result.add(String.format(SUITE_TEST_COMMAND_TEMPLATE, Optional.ofNullable(devopsCiApiTestInfoDTO.getBlockAfterJob()).orElse(Boolean.FALSE), Optional.ofNullable(devopsCiApiTestInfoDTO.getPerformThreshold()).orElse(0d), "suite", apiGateway, devopsCiApiTestInfoDTO.getApiTestSuiteId(), devopsCiJobDTO.getConfigId()));
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
        DevopsCiApiTestInfoVO devopsCiApiTestInfoVO = devopsCiJobVO.getDevopsCiApiTestInfoVO();
        if (devopsCiApiTestInfoVO == null) {
            throw new CommonException(ExceptionConstants.CiApiTestCode.DEVOPS_CI_API_TEST_INFO_NULL, devopsCiJobVO.getName());
        }
        if (devopsCiApiTestInfoVO.getApiTestTaskId() == null && devopsCiApiTestInfoVO.getApiTestSuiteId() == null) {
            throw new CommonException(ExceptionConstants.CiApiTestCode.DEVOPS_CI_API_TEST_INFO_BOTH_TAK_ID_SUITE_ID_NULL, devopsCiJobVO.getName());
        }
    }

    @Override
    protected Long saveConfig(Long ciPipelineId, DevopsCiJobVO devopsCiJobVO) {
        DevopsCiApiTestInfoDTO devopsCiApiTestInfoDTO = ConvertUtils.convertObject(devopsCiJobVO.getDevopsCiApiTestInfoVO(), DevopsCiApiTestInfoDTO.class);
        Long[] notifyUserIds = ObjectUtils.isEmpty(devopsCiApiTestInfoDTO.getNotifyUserIds()) ? new Long[]{} : KeyDecryptHelper.decryptIdArray(devopsCiApiTestInfoDTO.getNotifyUserIds().split(","));
        devopsCiApiTestInfoDTO.setNotifyUserIds(notifyUserIds == null ? "" : Arrays.stream(notifyUserIds).filter(Objects::nonNull).map(Object::toString).collect(Collectors.joining(",")));
        devopsCiApiTestInfoDTO.setId(null);
        devopsCiApiTestInfoDTO.setCiPipelineId(ciPipelineId);
        devopsCiApiTestInfoService.insert(devopsCiApiTestInfoDTO);
        return devopsCiApiTestInfoDTO.getId();
    }

    @Override
    public void deleteCdInfo(DevopsCiJobVO devopsCiJobVO) {
        devopsCiJobVO.setDevopsCiApiTestInfoVO(null);
        devopsCiJobVO.setCompleted(false);
    }

    @Override
    public void fillJobConfigInfo(DevopsCiJobVO devopsCiJobVO) {
        DevopsCiApiTestInfoVO devopsCiApiTestInfoVO = ConvertUtils.convertObject(devopsCiApiTestInfoService.selectByPrimaryKey(devopsCiJobVO.getConfigId()), DevopsCiApiTestInfoVO.class);
        String notifyUserIds = devopsCiApiTestInfoVO.getNotifyUserIds();
        if (!ObjectUtils.isEmpty(notifyUserIds)) {
            String[] encryptArray = KeyDecryptHelper.encryptArray(notifyUserIds.split(","));
            notifyUserIds = encryptArray == null ? "" : String.join(",", encryptArray);
        }
        devopsCiApiTestInfoVO.setNotifyUserIds(notifyUserIds);
        devopsCiJobVO.setDevopsCiApiTestInfoVO(devopsCiApiTestInfoVO);
    }


    @Override
    public void fillJobTemplateConfigInfo(DevopsCiJobVO devopsCiJobVO) {
        CiTplApiTestInfoCfgDTO ciTplApiTestInfoCfgDTO = devopsCiTplApiTestInfoCfgService.selectByPrimaryKey(devopsCiJobVO.getConfigId());
        if (ciTplApiTestInfoCfgDTO == null) {
            ciTplApiTestInfoCfgDTO = new CiTplApiTestInfoCfgDTO();
        }
        devopsCiJobVO.setDevopsCiApiTestInfoVO(ConvertUtils.convertObject(ciTplApiTestInfoCfgDTO, DevopsCiApiTestInfoVO.class));
    }
}
