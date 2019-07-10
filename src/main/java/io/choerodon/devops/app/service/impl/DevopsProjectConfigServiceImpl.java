package io.choerodon.devops.app.service.impl;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.DevopsProjectConfigDTO;
import io.choerodon.devops.api.dto.ProjectConfigDTO;
import io.choerodon.devops.api.dto.ProjectDefaultConfigDTO;
import io.choerodon.devops.api.validator.DevopsProjectConfigValidator;
import io.choerodon.devops.app.service.DevopsProjectConfigService;
import io.choerodon.devops.app.service.ProjectConfigHarborService;
import io.choerodon.devops.domain.application.entity.DevopsProjectConfigE;
import io.choerodon.devops.domain.application.entity.DevopsProjectE;
import io.choerodon.devops.domain.application.entity.ProjectE;
import io.choerodon.devops.domain.application.repository.ApplicationRepository;
import io.choerodon.devops.domain.application.repository.DevopsProjectConfigRepository;
import io.choerodon.devops.domain.application.repository.DevopsProjectRepository;
import io.choerodon.devops.domain.application.repository.IamRepository;
import io.choerodon.devops.domain.application.valueobject.Organization;
import io.choerodon.devops.infra.config.ConfigurationProperties;
import io.choerodon.devops.infra.config.HarborConfigurationProperties;
import io.choerodon.devops.infra.config.RetrofitHandler;
import io.choerodon.devops.infra.dataobject.DevopsProjectDO;
import io.choerodon.devops.infra.dataobject.harbor.*;
import io.choerodon.devops.infra.feign.HarborClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * @author zongw.lee@gmail.com
 * @since 2019/03/11
 */
@Service
public class DevopsProjectConfigServiceImpl implements DevopsProjectConfigService {

    private static final String HARBOR = "harbor";
    private static final String CHART = "chart";


    @Autowired
    DevopsProjectConfigRepository devopsProjectConfigRepository;

    @Autowired
    DevopsProjectConfigValidator configValidator;

    @Autowired
    ProjectConfigHarborService harborService;

    @Autowired
    IamRepository iamRepository;

    @Autowired
    private HarborConfigurationProperties harborConfigurationProperties;

    @Autowired
    private DevopsProjectRepository devopsProjectRepository;

    @Autowired
    private ApplicationRepository applicationRepository;


    @Override
    public DevopsProjectConfigDTO create(Long projectId, DevopsProjectConfigDTO devopsProjectConfigDTO) {
        if (devopsProjectConfigDTO.getType().equals(HARBOR) && devopsProjectConfigDTO.getConfig().getProject() != null) {
            checkRegistryProjectIsPrivate(devopsProjectConfigDTO);
        }
        DevopsProjectConfigE devopsProjectConfigE = ConvertHelper.convert(devopsProjectConfigDTO, DevopsProjectConfigE.class);
        devopsProjectConfigE.setProjectId(projectId);
        configValidator.checkConfigType(devopsProjectConfigDTO);

        devopsProjectConfigRepository.checkName(projectId, devopsProjectConfigE.getName());
        DevopsProjectConfigE res = devopsProjectConfigRepository.create(devopsProjectConfigE);
        return ConvertHelper.convert(res, DevopsProjectConfigDTO.class);
    }

    private void checkRegistryProjectIsPrivate(DevopsProjectConfigDTO devopsProjectConfigDTO) {
        ConfigurationProperties configurationProperties = new ConfigurationProperties();
        configurationProperties.setBaseUrl(devopsProjectConfigDTO.getConfig().getUrl());
        configurationProperties.setUsername(devopsProjectConfigDTO.getConfig().getUserName());
        configurationProperties.setPassword(devopsProjectConfigDTO.getConfig().getPassword());
        configurationProperties.setInsecureSkipTlsVerify(false);
        configurationProperties.setProject(devopsProjectConfigDTO.getConfig().getProject());
        configurationProperties.setType(HARBOR);
        Retrofit retrofit = RetrofitHandler.initRetrofit(configurationProperties);
        HarborClient harborClient = retrofit.create(HarborClient.class);
        Call<List<ProjectDetail>> listProject = harborClient.listProject(devopsProjectConfigDTO.getConfig().getProject());
        Response<List<ProjectDetail>> projectResponse = null;
        try {
            projectResponse = listProject.execute();
            if (projectResponse != null && projectResponse.body() != null) {
                if ("false".equals(projectResponse.body().get(0).getMetadata().getHarborPublic())) {
                    devopsProjectConfigDTO.getConfig().setPrivate(true);
                } else {
                    devopsProjectConfigDTO.getConfig().setPrivate(false);
                }
            }
        } catch (IOException e) {
            throw new CommonException(e);
        }
    }

    @Override
    public DevopsProjectConfigDTO updateByPrimaryKeySelective(Long projectId, DevopsProjectConfigDTO devopsProjectConfigDTO) {
        if (devopsProjectConfigDTO.getType().equals(HARBOR) && devopsProjectConfigDTO.getConfig().getProject() != null) {
            checkRegistryProjectIsPrivate(devopsProjectConfigDTO);
        }
        DevopsProjectConfigE devopsProjectConfigE = ConvertHelper.convert(devopsProjectConfigDTO, DevopsProjectConfigE.class);
        if (!ObjectUtils.isEmpty(devopsProjectConfigDTO.getType())) {
            configValidator.checkConfigType(devopsProjectConfigDTO);
        }
        return ConvertHelper.convert(devopsProjectConfigRepository.updateByPrimaryKeySelective(devopsProjectConfigE), DevopsProjectConfigDTO.class);
    }

    @Override
    public DevopsProjectConfigDTO queryByPrimaryKey(Long id) {
        return ConvertHelper.convert(devopsProjectConfigRepository.queryByPrimaryKey(id), DevopsProjectConfigDTO.class);
    }

    @Override
    public PageInfo<DevopsProjectConfigDTO> listByOptions(Long projectId, PageRequest pageRequest, String params) {
        return ConvertPageHelper.convertPageInfo(devopsProjectConfigRepository.listByOptions(projectId, pageRequest, params), DevopsProjectConfigDTO.class);
    }

    @Override
    public void delete(Long id) {
        devopsProjectConfigRepository.delete(id);
    }

    @Override
    public List<DevopsProjectConfigDTO> queryByIdAndType(Long projectId, String type) {
        return ConvertHelper.convertList(devopsProjectConfigRepository.queryByIdAndType(projectId, type), DevopsProjectConfigDTO.class);
    }

    @Override
    public void checkName(Long projectId, String name) {
        devopsProjectConfigRepository.checkName(projectId, name);
    }

    @Override
    public Boolean checkIsUsed(Long configId) {
        return devopsProjectConfigRepository.checkIsUsed(configId);
    }

    @Override
    public void setHarborProjectIsPrivate(Long projectId, boolean harborPrivate) {
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        ConfigurationProperties configurationProperties = new ConfigurationProperties(harborConfigurationProperties);
        configurationProperties.setType("harbor");
        Retrofit retrofit = RetrofitHandler.initRetrofit(configurationProperties);
        HarborClient harborClient = retrofit.create(HarborClient.class);
        if (harborPrivate) {
            //设置为私有后将harbor项目设置为私有,新增项目默认harbor配置,以及更新项目下所有应用的默认harbor配置
            DevopsProjectE devopsProjectE = devopsProjectRepository.queryDevopsProject(projectId);
            String username = devopsProjectE.getHarborProjectUserName() == null ? String.format("user%s%s", organization.getId(), projectId) : devopsProjectE.getHarborProjectUserName();
            String email = devopsProjectE.getHarborProjectUserEmail() == null ? String.format("%s@harbor.com", username) : devopsProjectE.getHarborProjectUserEmail();
            String password = devopsProjectE.getHarborProjectUserPassword() == null ? String.format("%sA", username) : devopsProjectE.getHarborProjectUserPassword();
            User user = new User(username, email, password, username);
            //创建用户
            Response<Void> result = null;
            try {
                Response<List<User>> users = harborClient.listUser(username).execute();
                if (users.raw().code() != 200) {
                    throw new CommonException(users.errorBody().string());
                }
                if (users.body().isEmpty()) {
                    result = harborClient.insertUser(user).execute();
                    if (result.raw().code() != 201) {
                        throw new CommonException(result.errorBody().string());
                    }
                }
                //给项目绑定角色
                Response<List<ProjectDetail>> projects = harborClient.listProject(organization.getCode() + "-" + projectE.getCode()).execute();
                if (!projects.body().isEmpty()) {
                    ProjectDetail projectDetail = new ProjectDetail();
                    Metadata metadata = new Metadata();
                    metadata.setHarborPublic("false");
                    projectDetail.setMetadata(metadata);
                    result = harborClient.updateProject(projects.body().get(0).getProjectId(), projectDetail).execute();
                    if (result.raw().code() != 200) {
                        throw new CommonException(result.errorBody().string());
                    }
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
                    if (result.raw().code() != 200) {
                        throw new CommonException(result.errorBody().string());
                    }
                }
            } catch (IOException e) {
                throw new CommonException(e);
            }

            //更新项目表
            if (devopsProjectE.getHarborProjectUserPassword() == null) {
                devopsProjectE.setHarborProjectUserName(user.getUsername());
                devopsProjectE.setHarborProjectIsPrivate(true);
                devopsProjectE.setHarborProjectUserPassword(user.getPassword());
                devopsProjectE.setHarborProjectUserEmail(user.getEmail());
            }
            devopsProjectE.setHarborProjectIsPrivate(true);
            devopsProjectRepository.updateProjectAttr(ConvertHelper.convert(devopsProjectE, DevopsProjectDO.class));

            
            //新增项目harbor配置
            DevopsProjectConfigDTO devopsProjectConfigDTO = new DevopsProjectConfigDTO();
            devopsProjectConfigDTO.setName("project_harbor_default");
            devopsProjectConfigDTO.setType(HARBOR);
            devopsProjectConfigDTO.setProjectId(projectId);
            ProjectConfigDTO projectConfigDTO = new ProjectConfigDTO();
            projectConfigDTO.setPrivate(true);
            projectConfigDTO.setUrl(harborConfigurationProperties.getBaseUrl());
            projectConfigDTO.setPassword(user.getPassword());
            projectConfigDTO.setUserName(user.getUsername());
            projectConfigDTO.setEmail(user.getEmail());
            projectConfigDTO.setProject(organization.getCode() + "-" + projectE.getCode());
            devopsProjectConfigDTO.setConfig(projectConfigDTO);
            DevopsProjectConfigE devopsProjectConfigE = devopsProjectConfigRepository.create(ConvertHelper.convert(devopsProjectConfigDTO, DevopsProjectConfigE.class));

            //更新项目下所有应用的harbor配置为该默认配置
            List<DevopsProjectConfigE> oldDevopsProjectConfigEs = devopsProjectConfigRepository.queryByIdAndType(projectId, HARBOR);
            applicationRepository.updateAppHarborConfig(projectId, devopsProjectConfigE.getId(), oldDevopsProjectConfigEs.get(0).getId(), true);
        } else {
            //设置为公有后将harbor项目设置为公有,删除成员角色,并删除项目默认harbor配置,以及更新项目下所有应用的默认harbor配置
            try {
                Response<List<ProjectDetail>> projects = harborClient.listProject(organization.getCode() + "-" + projectE.getCode()).execute();
                if (!projects.body().isEmpty()) {
                    ProjectDetail projectDetail = new ProjectDetail();
                    Metadata metadata = new Metadata();
                    metadata.setHarborPublic("true");
                    projectDetail.setMetadata(metadata);
                    Response<Void> result = harborClient.updateProject(projects.body().get(0).getProjectId(), projectDetail).execute();
                    if (result.raw().code() != 200) {
                        throw new CommonException(result.errorBody().toString());
                    }
                    Response<List<User>> users = harborClient.listUser(String.format("user%s%s", organization.getId(), projectId)).execute();
                    if (users.raw().code() != 200) {
                        throw new CommonException(users.errorBody().string());
                    }
                    Response<SystemInfo> systemInfoResponse = harborClient.getSystemInfo().execute();
                    if (systemInfoResponse.raw().code() != 200) {
                        throw new CommonException(systemInfoResponse.errorBody().string());
                    }
                    if (systemInfoResponse.body().getHarborVersion().equals("v1.4.0")) {
                        harborClient.deleteLowVersionMember(projects.body().get(0).getProjectId(), users.body().get(0).getUserId().intValue()).execute();
                    } else {
                        harborClient.deleteMember(projects.body().get(0).getProjectId(), users.body().get(0).getUserId().intValue()).execute();
                    }
                    DevopsProjectConfigE devopsProjectConfigE = devopsProjectConfigRepository.queryByName(projectId, "project_harbor_default");
                    DevopsProjectConfigE newDevopsProjectConfigE = devopsProjectConfigRepository.queryByName(null, "harbor_default");

                    DevopsProjectE devopsProjectE = devopsProjectRepository.queryDevopsProject(projectId);
                    devopsProjectE.setHarborProjectIsPrivate(false);
                    devopsProjectRepository.updateProjectAttr(ConvertHelper.convert(devopsProjectE, DevopsProjectDO.class));
                    applicationRepository.updateAppHarborConfig(projectId, newDevopsProjectConfigE.getId(), devopsProjectConfigE.getId(), false);
                    devopsProjectConfigRepository.delete(devopsProjectConfigE.getId());

                }
            } catch (IOException e) {
                throw new CommonException(e);
            }
        }

    }

    @Override
    public ProjectDefaultConfigDTO getProjectDefaultConfig(Long projectId) {
        ProjectDefaultConfigDTO projectDefaultConfigDTO = new ProjectDefaultConfigDTO();
        DevopsProjectE devopsProjectE = devopsProjectRepository.queryDevopsProject(projectId);
        projectDefaultConfigDTO.setHarborIsPrivate(devopsProjectE.getHarborProjectIsPrivate());
        List<DevopsProjectConfigE> harborConfigs = devopsProjectConfigRepository.queryByIdAndType(projectId, HARBOR);
        Optional<DevopsProjectConfigE> devopsConfigE = harborConfigs.stream().filter(devopsProjectConfigE -> devopsProjectConfigE.getName().equals("project_harbor_default")).findFirst();
        if (devopsConfigE.isPresent()) {
            projectDefaultConfigDTO.setHarborConfigName(devopsConfigE.get().getName());
        } else {
            projectDefaultConfigDTO.setHarborConfigName(harborConfigs.get(0).getName());
        }
        List<DevopsProjectConfigE> chartConfigs = devopsProjectConfigRepository.queryByIdAndType(projectId, CHART);
        Optional<DevopsProjectConfigE> chartConfig = chartConfigs.stream().filter(devopsProjectConfigE -> devopsProjectConfigE.getName().equals("chart_default")).findFirst();

        if (chartConfig.isPresent()) {
            projectDefaultConfigDTO.setChartConfigName(chartConfig.get().getName());
        }

        return projectDefaultConfigDTO;
    }
}
