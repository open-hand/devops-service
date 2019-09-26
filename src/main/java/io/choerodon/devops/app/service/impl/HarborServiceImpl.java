package io.choerodon.devops.app.service.impl;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.HarborService;
import io.choerodon.devops.domain.application.entity.DevopsProjectE;
import io.choerodon.devops.domain.application.entity.ProjectE;
import io.choerodon.devops.domain.application.event.HarborPayload;
import io.choerodon.devops.domain.application.repository.DevopsProjectRepository;
import io.choerodon.devops.domain.application.repository.IamRepository;
import io.choerodon.devops.domain.application.valueobject.Organization;
import io.choerodon.devops.infra.config.ConfigurationProperties;
import io.choerodon.devops.infra.config.HarborConfigurationProperties;
import io.choerodon.devops.infra.config.RetrofitHandler;
import io.choerodon.devops.infra.dataobject.DevopsProjectDO;
import io.choerodon.devops.infra.dataobject.harbor.*;
import io.choerodon.devops.infra.feign.HarborClient;
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
    private static final Gson gson = new Gson();

    @Autowired
    private HarborConfigurationProperties harborConfigurationProperties;
    @Autowired
    private DevopsProjectRepository devopsProjectRepository;
    @Autowired
    private IamRepository iamRepository;


    @Override
    public void createHarbor(HarborPayload harborPayload) {
        //创建harbor仓库
        try {
            ProjectE projectE = iamRepository.queryIamProject(harborPayload.getProjectId());
            Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
            ConfigurationProperties configurationProperties = new ConfigurationProperties(harborConfigurationProperties);
            configurationProperties.setType("harbor");
            Retrofit retrofit = RetrofitHandler.initRetrofit(configurationProperties);
            HarborClient harborClient = retrofit.create(HarborClient.class);
            Response<Void> result = null;
            DevopsProjectE devopsProjectE = devopsProjectRepository.queryDevopsProject(harborPayload.getProjectId());
            String username = String.format("user%s%s", organization.getId(), harborPayload.getProjectId());
            String email = String.format("%s@harbor.com", username);
            String password = String.format("%spassword", username);
            User user = new User(username, email, password, username);
            LOGGER.info(harborConfigurationProperties.getParams());
            //创建项目
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
            //创建用户
            result = harborClient.insertUser(user).execute();
            if (result.raw().code() != 201) {
                throw new CommonException(result.errorBody().string());
            }
            //给项目绑定用户角色
            Response<List<ProjectDetail>> projects = harborClient.listProject(organization.getCode() + "-" + projectE.getCode()).execute();
            if (projects.body() != null && !projects.body().isEmpty()) {
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
            if (devopsProjectE.getHarborProjectUserPassword() == null) {
                devopsProjectE.setHarborProjectUserName(user.getUsername());
                devopsProjectE.setHarborProjectIsPrivate(true);
                devopsProjectE.setHarborProjectUserPassword(user.getPassword());
                devopsProjectE.setHarborProjectUserEmail(user.getEmail());
            }
            devopsProjectRepository.updateProjectAttr(ConvertHelper.convert(devopsProjectE, DevopsProjectDO.class));
        } catch (IOException e) {
            throw new CommonException(e);
        }

    }
}
