package io.choerodon.devops.app.service.impl;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import io.choerodon.devops.api.vo.harbor.HarborCustomRepo;
import io.choerodon.devops.infra.feign.RdupmClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import retrofit2.Response;
import retrofit2.Retrofit;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.DevopsConfigVO;
import io.choerodon.devops.app.eventhandler.payload.HarborPayload;
import io.choerodon.devops.app.service.DevopsConfigService;
import io.choerodon.devops.app.service.DevopsHarborUserService;
import io.choerodon.devops.app.service.DevopsProjectService;
import io.choerodon.devops.app.service.HarborService;
import io.choerodon.devops.infra.config.ConfigurationProperties;
import io.choerodon.devops.infra.config.HarborConfigurationProperties;
import io.choerodon.devops.infra.dto.DevopsProjectDTO;
import io.choerodon.devops.infra.dto.HarborUserDTO;
import io.choerodon.devops.infra.dto.harbor.*;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.dto.iam.Tenant;
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
    private static final String AUTHTYPE = "pull";
    private static final Gson gson = new Gson();

    @Autowired
    private HarborConfigurationProperties harborConfigurationProperties;
    @Autowired
    private DevopsConfigService devopsConfigService;
    @Autowired
    private DevopsProjectService devopsProjectService;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private DevopsHarborUserService devopsHarborUserService;
    @Autowired
    @Lazy
    private RdupmClient rdupmClient;

    @Value("${services.harbor.baseUrl}")
    private String baseUrl;
    @Value("${services.harbor.username}")
    private String username;
    @Value("${services.harbor.password}")
    private String password;


    @Override
    public void createHarborUserByClient(HarborPayload harborPayload, User user, ProjectDTO projectDTO, List<Integer> roles) {
        HarborClient harborClient = initHarborClient(harborPayload);
        Tenant organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
        createHarborUser(harborClient, user, roles, organizationDTO, projectDTO);
    }

    @Override
    public User convertHarborUser(ProjectDTO projectDTO, Boolean isPush, String name) {
        String pull = "";
        if (!isPush) {
            pull = "pull";
        }
        String userName = null;
        if (ObjectUtils.isEmpty(name)) {
            userName = String.format("%sUser%s%s", pull, projectDTO.getOrganizationId(), projectDTO.getId());
        } else {
            userName = name;
        }
        String userEmail = String.format("%s@harbor.com", userName);
        String pwd = String.format("%sPWD", userName);
        return new User(userName, userEmail, pwd, userName);
    }

    private void createHarborUser(HarborClient harborClient, User user, List<Integer> roles, Tenant organizationDTO, ProjectDTO projectDTO) {
        Response<Void> result = null;
        try {
            result = harborClient.insertUser(user).execute();
            if (result.raw().code() != 201 && result.raw().code() != 409) {
                throw new CommonException(result.errorBody().string());
            }
            //给项目绑定角色
            Response<List<ProjectDetail>> projects = harborClient.listProject(organizationDTO.getTenantNum() + "-" + projectDTO.getCode()).execute();
            if (!CollectionUtils.isEmpty(projects.body())) {
                Integer harborProjectId = devopsConfigService.getHarborProjectId(projects.body(), organizationDTO.getTenantNum() + "-" + projectDTO.getCode());

                Response<SystemInfo> systemInfoResponse = harborClient.getSystemInfo().execute();
                if (systemInfoResponse.raw().code() != 200) {
                    throw new CommonException(systemInfoResponse.errorBody().string());
                }
                if (systemInfoResponse.body().getHarborVersion().equals("v1.4.0")) {
                    Role role = new Role();
                    role.setUsername(user.getUsername());
                    role.setRoles(roles);
                    result = harborClient.setProjectMember(harborProjectId, role).execute();
                } else {
                    ProjectMember projectMember = new ProjectMember();
                    MemberUser memberUser = new MemberUser();
                    projectMember.setRoleId(roles.get(0));
                    memberUser.setUsername(user.getUsername());
                    projectMember.setMemberUser(memberUser);
                    result = harborClient.setProjectMember(harborProjectId, projectMember).execute();
                }
                if (result.raw().code() != 201 && result.raw().code() != 200 && result.raw().code() != 409) {
                    throw new CommonException(result.errorBody().string());
                }
            }

        } catch (Exception e) {
            throw new CommonException(e);
        }
    }


    private HarborClient initHarborClient(HarborPayload harborPayload) {
        //获取当前项目的harbor设置,如果有自定义的取自定义，没自定义取组织层的harbor配置
        if (harborPayload.getProjectId() != null) {
            DevopsConfigVO devopsConfigVO = devopsConfigService.dtoToVo(devopsConfigService.queryRealConfig(harborPayload.getProjectId(), ResourceLevel.PROJECT.value(), HARBOR, AUTHTYPE));
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
        return retrofit.create(HarborClient.class);
    }

    @Override
    public List<HarborCustomRepo> listAllCustomRepoByProject(Long projectId) {
        List<HarborCustomRepo> harborCustomRepoVOS = rdupmClient.listAllCustomRepoByProject(projectId).getBody();
        return harborCustomRepoVOS;
    }
}
