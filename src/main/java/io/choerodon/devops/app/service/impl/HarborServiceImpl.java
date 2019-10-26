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
import io.choerodon.devops.app.service.DevopsHarborUserService;
import io.choerodon.devops.app.service.DevopsProjectService;
import io.choerodon.devops.app.service.HarborService;
import io.choerodon.devops.infra.config.ConfigurationProperties;
import io.choerodon.devops.infra.config.HarborConfigurationProperties;
import io.choerodon.devops.infra.dto.DevopsProjectDTO;
import io.choerodon.devops.infra.dto.HarborUserDTO;
import io.choerodon.devops.infra.dto.harbor.*;
import io.choerodon.devops.infra.dto.iam.OrganizationDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.feign.HarborClient;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.handler.RetrofitHandler;
import io.choerodon.devops.infra.util.GenerateUUID;

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
    private static final String USER_PREFIX = "user%s%s";
    private static final String OPERATE_CREATE = "create";
    private static final String OPERATE_DELETE = "delete";

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
            DevopsConfigVO devopsConfigVO = devopsConfigService.dtoToVo(devopsConfigService.queryRealConfig(harborPayload.getProjectId(), ResourceLevel.PROJECT.value(), HARBOR,AUTHTYPE));
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
        createHarbor(harborClient, harborPayload.getProjectId(), harborPayload.getProjectCode(), createUser,true);
    }


    @Override
    public void createHarbor(HarborClient harborClient, Long projectId, String projectCode, Boolean createUser,Boolean harborPrivate) {
        //创建harbor仓库
        try {
            Response<Void> result = null;
            LOGGER.info(harborConfigurationProperties.getParams());
            if (harborConfigurationProperties.getParams() == null || harborConfigurationProperties.getParams().equals("")) {
                result = harborClient.insertProject(new Project(projectCode, harborPrivate ? 0 : 1)).execute();
            } else {
                Map<String, String> params = new HashMap<>();
                params = gson.fromJson(harborConfigurationProperties.getParams(), params.getClass());
                result = harborClient.insertProject(params, new Project(projectCode, harborPrivate ? 0 : 1)).execute();
            }
            if (result.raw().code() != 201 && result.raw().code() != 409) {
                throw new CommonException(result.message());
            }

            ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
            OrganizationDTO organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
            DevopsProjectDTO devopsProjectDTO = devopsProjectService.baseQueryByProjectId(projectId);
            if (createUser) {
                String username = String.format("User%s%s", organizationDTO.getId(), projectId);
                String useremail = String.format("%s@harbor.com", username);
                String password = String.format("%s%s", username, GenerateUUID.generateUUID().substring(0, 3));

                String pullUsername = String.format("pullUser%s%s", organizationDTO.getId(), projectId);
                String pullUseremail = String.format("%s@harbor.com", pullUsername);
                String pullUserpassword = String.format("%s%s", pullUsername, GenerateUUID.generateUUID().substring(0, 3));

                User user = new User(username, useremail, password, username);
                User pullUser = new User(pullUsername, pullUseremail, pullUserpassword, pullUsername);
                //创建用户
                createUser(harborClient, user, Arrays.asList(1), organizationDTO, projectDTO);
                createUser(harborClient, pullUser, Arrays.asList(3), organizationDTO, projectDTO);
                HarborUserDTO harborUserDTO = new HarborUserDTO(user.getUsername(), user.getPassword(), user.getEmail(), true);
                HarborUserDTO pullHarborUserDTO = new HarborUserDTO(pullUser.getUsername(), pullUser.getPassword(), pullUser.getEmail(), false);

                devopsProjectDTO.setHarborProjectIsPrivate(true);
                if (devopsHarborUserService.create(harborUserDTO) != 1) {
                    throw new CommonException("error.harbor.user.insert");
                } else {
                    devopsProjectDTO.setHarborUserId(harborUserDTO.getId());
                    devopsProjectService.baseUpdate(devopsProjectDTO);
                }
                if (devopsHarborUserService.create(pullHarborUserDTO) != 1) {
                    throw new CommonException("error.harbor.pull.user.insert");
                } else {
                    devopsProjectDTO.setHarborPullUserId(pullHarborUserDTO.getId());
                    devopsProjectService.baseUpdate(devopsProjectDTO);
                }

            }else {
                //设置barbor用户角色
                operateMember(harborClient,null,null,organizationDTO,projectDTO,OPERATE_DELETE);
            }
        } catch (IOException e) {
            throw new CommonException(e);
        }

    }

    private void createUser(HarborClient harborClient, User user, List<Integer> roles, OrganizationDTO organizationDTO, ProjectDTO projectDTO) {
        Response<Void> result = null;
        try {
            result = harborClient.insertUser(user).execute();
            if (result.raw().code() != 201 && result.raw().code() != 409) {
                throw new CommonException(result.errorBody().string());
            }
            //给项目绑定角色
            operateMember(harborClient, user, roles, organizationDTO, projectDTO, OPERATE_CREATE);
        } catch (IOException e) {
            throw new CommonException(e);
        }

    }
    private void operateMember(HarborClient harborClient, User user, List<Integer> roles, OrganizationDTO organizationDTO, ProjectDTO projectDTO, String operateRole) {
        Response<Void> result = null;
        //删除成员角色
        try {
            Response<List<ProjectDetail>> projects = harborClient.listProject(organizationDTO.getCode() + "-" + projectDTO.getCode()).execute();
            if (!projects.body().isEmpty()) {
                Response<SystemInfo> systemInfoResponse = harborClient.getSystemInfo().execute();
                if (systemInfoResponse.raw().code() != 200) {
                    throw new CommonException(systemInfoResponse.errorBody().string());
                }
                if (systemInfoResponse.body().getHarborVersion().equals("v1.4.0")) {
                    if (OPERATE_CREATE.equals(operateRole)) {
                        Role role = new Role();
                        role.setUsername(user.getUsername());
                        role.setRoles(roles);
                        result = harborClient.setProjectMember(projects.body().get(0).getProjectId(), role).execute();
                    } else if (OPERATE_DELETE.equals(operateRole)) {
                        Response<List<User>> users = harborClient.listUser(String.format(USER_PREFIX, organizationDTO.getId(), projectDTO.getId())).execute();
                        if (users.raw().code() != 200) {
                            throw new CommonException(users.errorBody().string());
                        }
                        harborClient.deleteLowVersionMember(projects.body().get(0).getProjectId(), users.body().get(0).getUserId().intValue());
                    }

                } else {
                    if (OPERATE_CREATE.equals(operateRole)) {
                        //绑定角色
                        ProjectMember projectMember = new ProjectMember();
                        MemberUser memberUser = new MemberUser();
                        projectMember.setRoleId(roles.get(0));
                        memberUser.setUsername(user.getUsername());
                        projectMember.setMemberUser(memberUser);
                        result = harborClient.setProjectMember(projects.body().get(0).getProjectId(), projectMember).execute();
                        if (result.raw().code() != 201 && result.raw().code() != 200 && result.raw().code() != 409) {
                            throw new CommonException(result.errorBody().string());
                        }
                    } else if (OPERATE_DELETE.equals(operateRole)) {
                        Response<List<ProjectMember>> projectMembers = harborClient.getProjectMembers(projects.body().get(0).getProjectId(), String.format(USER_PREFIX, organizationDTO.getId(), projectDTO.getId())).execute();
                        if (projectMembers.raw().code() != 200) {
                            throw new CommonException(projectMembers.errorBody().string());
                        }
                        projectMembers.body().stream().forEach(projectMember -> {
                            try {
                                harborClient.deleteMember(projects.body().get(0).getProjectId(), projectMember.getId()).execute();
                            } catch (IOException e) {
                                throw new CommonException("error.delete.harbor.member");
                            }
                        });
                    }

                }

            }
        } catch (IOException e) {
            throw new CommonException(e);
        }
    }

}
