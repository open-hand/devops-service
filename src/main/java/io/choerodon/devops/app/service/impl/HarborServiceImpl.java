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
import io.choerodon.devops.app.eventhandler.payload.HarborPayload;
import io.choerodon.devops.app.service.DevopsConfigService;
import io.choerodon.devops.app.service.DevopsProjectService;
import io.choerodon.devops.app.service.HarborService;
import io.choerodon.devops.infra.config.ConfigurationProperties;
import io.choerodon.devops.infra.config.HarborConfigurationProperties;
import io.choerodon.devops.infra.dto.DevopsProjectDTO;
import io.choerodon.devops.infra.dto.harbor.*;
import io.choerodon.devops.infra.dto.iam.OrganizationDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.feign.HarborClient;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
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
    @Autowired
    private DevopsProjectService devopsProjectService;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;

    @Value("${services.harbor.baseUrl}")
    private String baseUrl;
    @Value("${services.harbor.username}")
    private String username;
    @Value("${services.harbor.password}")
    private String password;

    @Override
    public void createHarborForProject(HarborPayload harborPayload) {
        //获取当前项目的harbor设置,如果有自定义的取自定义，没自定义取组织层的harbor配置
        if (harborPayload.getProjectId() != null) {
            DevopsConfigVO devopsConfigVO = devopsConfigService.dtoToVo(devopsConfigService.queryRealConfig(harborPayload.getProjectId(), ResourceLevel.PROJECT.value(), HARBOR));
            harborConfigurationProperties.setUsername(devopsConfigVO.getConfig().getUserName());
            harborConfigurationProperties.setPassword(devopsConfigVO.getConfig().getPassword());
            harborConfigurationProperties.setBaseUrl(devopsConfigVO.getConfig().getUrl());
        } else {
            harborConfigurationProperties.setUsername(username);
            harborConfigurationProperties.setPassword(password);
            baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
            harborConfigurationProperties.setBaseUrl(baseUrl);
            harborConfigurationProperties.setInsecureSkipTlsVerify(true);
        }
        ConfigurationProperties configurationProperties = new ConfigurationProperties(harborConfigurationProperties);
        configurationProperties.setType(HARBOR);
        Retrofit retrofit = RetrofitHandler.initRetrofit(configurationProperties);
        HarborClient harborClient = retrofit.create(HarborClient.class);
        Boolean createUser = harborPayload.getProjectId() != null;
        createHarbor(harborClient, harborPayload.getProjectId(), harborPayload.getProjectCode(), createUser);
    }


    @Override
    public void createHarbor(HarborClient harborClient, Long projectId, String projectCode, Boolean createUser) {
        //创建harbor仓库
        try {
            Response<Void> result = null;
            LOGGER.info(harborConfigurationProperties.getParams());
            if (harborConfigurationProperties.getParams() == null || harborConfigurationProperties.getParams().equals("")) {
                result = harborClient.insertProject(new Project(projectCode, 1)).execute();
            } else {
                Map<String, String> params = new HashMap<>();
                params = gson.fromJson(harborConfigurationProperties.getParams(), params.getClass());
                result = harborClient.insertProject(params, new Project(projectCode, 1)).execute();
            }
            if (result.raw().code() != 201 && result.raw().code() != 409) {
                throw new CommonException(result.message());
            }
            if (createUser) {
                ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
                OrganizationDTO organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
                DevopsProjectDTO devopsProjectDTO = devopsProjectService.baseQueryByProjectId(projectId);
                String username = String.format("user%s%s", organizationDTO.getId(), projectId);
                String email = String.format("%s@choerodon.com", username);
                String password = String.format("%sAAA", username);
                User user = new User(username, email, password, username);
                //创建用户
                try {
                    result = harborClient.insertUser(user).execute();
                    if (result.raw().code() != 201) {
                        throw new CommonException(result.errorBody().string());
                    }
                    //给项目绑定角色
                    Response<List<ProjectDetail>> projects = harborClient.listProject(organizationDTO.getCode() + "-" + projectDTO.getCode()).execute();
                    if (!projects.body().isEmpty()) {
                        Response<SystemInfo> systemInfoResponse = harborClient.getSystemInfo().execute();
                        if (systemInfoResponse.raw().code() != 200) {
                            throw new CommonException(systemInfoResponse.errorBody().string());
                        }
                        if (systemInfoResponse.body().getHarborVersion().equals("v1.4.0")) {
                            Role role = new Role();
                            role.setUsername(user.getUsername());
                            role.setRoles(Arrays.asList(1));
                            result = harborClient.setProjectMember(projects.body().get(0).getProjectId(), role).execute();
                        } else {
                            ProjectMember projectMember = new ProjectMember();
                            MemberUser memberUser = new MemberUser();
                            memberUser.setUsername(username);
                            projectMember.setMemberUser(memberUser);
                            result = harborClient.setProjectMember(projects.body().get(0).getProjectId(), projectMember).execute();
                        }
                        if (result.raw().code() != 201 && result.raw().code() != 200 && result.raw().code() != 409) {
                            throw new CommonException(result.errorBody().string());
                        }
                    }
                } catch (IOException e) {
                    throw new CommonException(e);
                }

                //更新项目表
                if (devopsProjectDTO.getHarborProjectUserPassword() == null) {
                    devopsProjectDTO.setHarborProjectUserName(user.getUsername());
                    devopsProjectDTO.setHarborProjectUserPassword(user.getPassword());
                    devopsProjectDTO.setHarborProjectUserEmail(user.getEmail());
                }
                devopsProjectService.baseUpdate(devopsProjectDTO);
            }
        } catch (IOException e) {
            throw new CommonException(e);
        }

    }

}
