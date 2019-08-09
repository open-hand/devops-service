package io.choerodon.devops.app.service.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.DevopsConfigVO;
import io.choerodon.devops.app.eventhandler.payload.HarborPayload;
import io.choerodon.devops.app.service.DevopsConfigService;
import io.choerodon.devops.app.service.HarborService;
import io.choerodon.devops.infra.config.ConfigurationProperties;
import io.choerodon.devops.infra.config.HarborConfigurationProperties;
import io.choerodon.devops.infra.dto.harbor.Project;
import io.choerodon.devops.infra.dto.iam.OrganizationDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.feign.HarborClient;
import io.choerodon.devops.infra.feign.operator.IamServiceClientOperator;
import io.choerodon.devops.infra.handler.RetrofitHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created with IntelliJ IDEA.
 * User: Runge
 * Date: 2018/4/8
 * Time: 10:37
 * Description:
 */
@Component
public class HarborServiceImpl implements HarborService {
    private static final Logger LOGGER = LoggerFactory.getLogger(HarborServiceImpl.class);
    private static final String HARBOR = "harbor";
    private static final Gson gson = new Gson();

    @Autowired
    private HarborConfigurationProperties harborConfigurationProperties;
    @Autowired
    private IamServiceClientOperator iamServiceClientOperator;
    @Autowired
    private DevopsConfigService devopsConfigService;

    @Override
    public void createHarbor(HarborPayload harborPayload) {
        //创建harbor仓库
        try {
            ProjectDTO projectDTO = iamServiceClientOperator.queryIamProjectById(harborPayload.getProjectId());
            OrganizationDTO organizationDTO = iamServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());

            //获取当前项目的harbor设置,如果有自定义的取自定义，没自定义取组织层的harbor配置
            DevopsConfigVO devopsConfigVO = devopsConfigService.dtoToVo(devopsConfigService.queryRealConfig(harborPayload.getProjectId(), ResourceLevel.PROJECT.value(), HARBOR));
            harborConfigurationProperties.setUsername(devopsConfigVO.getConfig().getUserName());
            harborConfigurationProperties.setPassword(devopsConfigVO.getConfig().getPassword());
            harborConfigurationProperties.setBaseUrl(devopsConfigVO.getConfig().getUrl());


            ConfigurationProperties configurationProperties = new ConfigurationProperties(harborConfigurationProperties);
            configurationProperties.setType(HARBOR);
            Retrofit retrofit = RetrofitHandler.initRetrofit(configurationProperties);
            HarborClient harborClient = retrofit.create(HarborClient.class);
            Response<Void> result = null;
            LOGGER.info(harborConfigurationProperties.getParams());
            if (harborConfigurationProperties.getParams() == null || harborConfigurationProperties.getParams().equals("")) {
                result = harborClient.insertProject(new Project(harborPayload.getProjectCode(), 1)).execute();
            } else {
                Map<String, String> params = new HashMap<>();
                params = gson.fromJson(harborConfigurationProperties.getParams(), params.getClass());
                result = harborClient.insertProject(params, new Project(harborPayload.getProjectCode(), 1)).execute();
            }
            if (result.raw().code() != 201) {
                throw new CommonException(result.message());
            }
        } catch (IOException e) {
            throw new CommonException(e);
        }

    }
}
