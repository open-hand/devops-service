package io.choerodon.devops.app.service.impl;

import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.devops.api.vo.sonar.Measure;
import io.choerodon.devops.api.vo.sonar.SonarComponent;
import io.choerodon.devops.app.service.AppServiceService;
import io.choerodon.devops.app.service.DevopsCiPipelineSonarService;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.constant.ResourceCheckConstant;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.DevopsCiPipelineSonarDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.dto.iam.Tenant;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.SonarClientOperator;
import io.choerodon.devops.infra.mapper.DevopsCiPipelineSonarMapper;
import io.choerodon.devops.infra.util.ExceptionUtil;
import io.choerodon.devops.infra.util.JsonHelper;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * ci任务生成sonar记录(DevopsCiPipelineSonar)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-16 17:56:59
 */
@Service
public class DevopsCiPipelineSonarServiceImpl implements DevopsCiPipelineSonarService {

    private static final String SONAR = "sonar";
    private static final String DEVOPS_SAVE_SONAR_INFO = "devops.save.sonar.info";
    private static final String DEVOPS_UPDATE_SONAR_INFO = "devops.update.sonar.info";

    @Autowired
    private AppServiceService appServiceService;
    @Autowired
    private DevopsCiPipelineSonarMapper devopsCiPipelineSonarMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private SonarClientOperator sonarClientOperator;

    @Override
    @Transactional
    public void saveSonarInfo(Long gitlabPipelineId, String jobName, String token, String scannerType) {
        ExceptionUtil.wrapExWithCiEx(() -> {
            AppServiceDTO appServiceDTO = appServiceService.baseQueryByToken(token);
            Long appServiceId = appServiceDTO.getId();
            DevopsCiPipelineSonarDTO devopsCiPipelineSonarDTO = queryByPipelineId(appServiceId, gitlabPipelineId, jobName);
            if (devopsCiPipelineSonarDTO == null) {
                baseCreate(new DevopsCiPipelineSonarDTO(appServiceId, gitlabPipelineId, jobName, scannerType));
            }
        });
    }

    @Override
    public DevopsCiPipelineSonarDTO queryByPipelineId(Long appServiceId, Long gitlabPipelineId, String jobName) {
        Assert.notNull(appServiceId, ResourceCheckConstant.DEVOPS_APP_SERVICE_ID_IS_NULL);
        Assert.notNull(gitlabPipelineId, PipelineCheckConstant.DEVOPS_GITLAB_PIPELINE_ID_IS_NULL);
        Assert.notNull(jobName, ResourceCheckConstant.DEVOPS_JOB_NAME_ID_IS_NULL);

        DevopsCiPipelineSonarDTO devopsCiPipelineSonarDTO = new DevopsCiPipelineSonarDTO(appServiceId, gitlabPipelineId, jobName);
        return devopsCiPipelineSonarMapper.selectOne(devopsCiPipelineSonarDTO);
    }

    @Override
    @Transactional
    public void baseCreate(DevopsCiPipelineSonarDTO devopsCiPipelineSonarDTO) {
        MapperUtil.resultJudgedInsertSelective(devopsCiPipelineSonarMapper, devopsCiPipelineSonarDTO, DEVOPS_SAVE_SONAR_INFO);
    }

    @Override
    @Transactional
    public void baseUpdate(DevopsCiPipelineSonarDTO devopsCiPipelineSonarDTO) {
        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsCiPipelineSonarMapper, devopsCiPipelineSonarDTO, DEVOPS_UPDATE_SONAR_INFO);
    }

    @Override
    public Boolean getSonarQualityGateScanResult(Long gitlabPipelineId, String token) {
        AppServiceDTO appServiceDTO = appServiceService.baseQueryByToken(token);
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(appServiceDTO.getProjectId());
        Tenant organization = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
        String projectKey = AppServiceServiceImpl.getSonarKey(appServiceDTO.getCode(), projectDTO.getDevopsComponentCode(), organization.getTenantNum());
        SonarComponent sonarQualityGateResultDetail = sonarClientOperator.getSonarQualityGateResultDetail(projectKey);
        Measure measure = sonarQualityGateResultDetail.getComponent().getMeasures().get(0);
        Map<String, Object> result = JsonHelper.unmarshalByJackson(measure.getValue(), new TypeReference<Map<String, Object>>() {
        });
        return "OK".equals(result.get("LEVEL"));
    }
}

