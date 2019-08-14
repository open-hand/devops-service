package io.choerodon.devops.app.service.impl;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import retrofit2.Response;
import retrofit2.Retrofit;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.DevopsConfigVO;
import io.choerodon.devops.api.vo.HarborMarketVO;
import io.choerodon.devops.app.eventhandler.payload.HarborPayload;
import io.choerodon.devops.app.service.DevopsConfigService;
import io.choerodon.devops.app.service.HarborService;
import io.choerodon.devops.infra.config.ConfigurationProperties;
import io.choerodon.devops.infra.config.HarborConfigurationProperties;
import io.choerodon.devops.infra.dto.harbor.Project;
import io.choerodon.devops.infra.dto.harbor.ProjectDetail;
import io.choerodon.devops.infra.dto.harbor.Role;
import io.choerodon.devops.infra.feign.HarborClient;
import io.choerodon.devops.infra.handler.RetrofitHandler;

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
    private DevopsConfigService devopsConfigService;
    @Value("services.harbor.baseUrl")
    private String baseUrl;
    @Value("services.harbor.username")
    private String username;
    @Value("services.harbor.password")
    private String password;

    @Override
    public void createHarbor(HarborPayload harborPayload) {
        //创建harbor仓库
        try {
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

    @Override
    public void createHarborForAppMarket(HarborMarketVO harborMarketVO) {
        try {
            //1.获取当前项目的harbor设置,如果有自定义的取自定义，没自定义取组织层的harbor配置
            harborConfigurationProperties.setUsername(username);
            harborConfigurationProperties.setPassword(password);
            harborConfigurationProperties.setBaseUrl(baseUrl);

            ConfigurationProperties configurationProperties = new ConfigurationProperties(harborConfigurationProperties);
            configurationProperties.setType(HARBOR);
            Retrofit retrofit = RetrofitHandler.initRetrofit(configurationProperties);
            HarborClient harborClient = retrofit.create(HarborClient.class);
            //2. 创建项目
            Response<Void> projectResult = harborClient.insertProject(new Project(harborMarketVO.getProjectCode(), 0)).execute();
            if (projectResult.raw().code() != 201) {
                throw new CommonException("error.create.harbor.project", projectResult.message());
            }
            //3.创建用户
            Response<Void> userResult = harborClient.insertUser(harborMarketVO.getUser()).execute();
            if (userResult.raw().code() != 201) {
                throw new CommonException("error.create.harbor.user", userResult.message());
            }
            //4.分配权限
            Response<List<ProjectDetail>> projects = harborClient.listProject(harborMarketVO.getProjectCode()).execute();
            Integer projectId;
            if (projects.body() != null && !projects.body().isEmpty()) {
                projectId = projects.body().get(0).getProjectId();
                Role role = new Role();
                role.setUsername(harborMarketVO.getUser().getUsername());
                role.setRoles(Arrays.asList(1));
                Response<Void> roleResult = harborClient.setProjectMember(projectId, role).execute();
                if (roleResult.raw().code() != 201) {
                    throw new CommonException("error.create.harbor.role", roleResult.message());
                }
            }
        } catch (IOException e) {
            throw new CommonException(e);
        }
    }

}
