package io.choerodon.devops.app.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.ConfigVO;
import io.choerodon.devops.api.vo.DefaultConfigVO;
import io.choerodon.devops.api.vo.DevopsConfigVO;
import io.choerodon.devops.app.service.AppServiceService;
import io.choerodon.devops.app.service.DevopsConfigService;
import io.choerodon.devops.app.service.DevopsProjectService;
import io.choerodon.devops.infra.config.ConfigurationProperties;
import io.choerodon.devops.infra.config.HarborConfigurationProperties;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.DevopsConfigDTO;
import io.choerodon.devops.infra.dto.DevopsProjectDTO;
import io.choerodon.devops.infra.dto.harbor.*;
import io.choerodon.devops.infra.dto.iam.OrganizationDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.feign.HarborClient;
import io.choerodon.devops.infra.feign.operator.IamServiceClientOperator;
import io.choerodon.devops.infra.handler.RetrofitHandler;
import io.choerodon.devops.infra.mapper.DevopsConfigMapper;
import io.choerodon.devops.infra.util.PageRequestUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import org.springframework.beans.BeanUtils;
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
public class DevopsConfigServiceImpl implements DevopsConfigService {

    private static final String HARBOR = "harbor";
    private static final String CHART = "chart";
    private static final String CUSTOM = "custom";
    private static final Gson gson = new Gson();
    public static final String APP_SERVICE = "appService";

    @Autowired
    private DevopsConfigMapper devopsConfigMapper;

    @Autowired
    private IamServiceClientOperator iamServiceClientOperator;

    @Autowired
    private HarborConfigurationProperties harborConfigurationProperties;

    @Autowired
    private DevopsProjectService devopsProjectService;

    @Autowired
    private AppServiceService appServiceService;

    public void operate(Long resourceId, String resourceType, List<DevopsConfigVO> devopsConfigVOS) {

        devopsConfigVOS.forEach(devopsConfigVO -> {
            //根据每个配置的默认还是自定义执行不同逻辑
            if (devopsConfigVO.getCustom()) {

                //自定义的harbor类型,不管是新建还是更新，当传进来有harbor project时都要检验project是否是私有
                if (devopsConfigVO.getType().equals(HARBOR) && devopsConfigVO.getConfig().getProject() != null) {
                    checkRegistryProjectIsPrivate(devopsConfigVO);
                }
                //根据配置所在的资源层级，查询出数据库中是否存在，存在则更新，不存在则新建
                DevopsConfigDTO devopsConfigDTO = baseQueryByResourceAndType(resourceId, resourceType, devopsConfigVO.getType());
                DevopsConfigDTO newDevopsConfigDTO = voToDto(devopsConfigVO);
                if (devopsConfigDTO != null) {
                    newDevopsConfigDTO.setId(devopsConfigDTO.getId());
                    setResourceId(resourceId, resourceType, newDevopsConfigDTO);
                    baseUpdate(newDevopsConfigDTO);
                } else {
                    setResourceId(resourceId, resourceType, newDevopsConfigDTO);
                    baseCreate(newDevopsConfigDTO);
                }
            } else {
                //默认的harbor类型,在项目层级有设置私有的功能
                if (devopsConfigVO.getType().equals(HARBOR) && resourceType.equals(ResourceLevel.PROJECT.value())) {
                    DevopsProjectDTO devopsProjectDTO = devopsProjectService.baseQueryByProjectId(resourceId);
                    //判断当前默认仓库私有配置是否和数据库中存储一致，不一致则执行对应逻辑
                    if (!devopsProjectDTO.getHarborProjectIsPrivate().equals(devopsConfigVO.getHarborPrivate())) {
                        operateHarborProject(resourceId, devopsConfigVO.getHarborPrivate());
                    }
                }
                //根据配置所在的资源层级，查询出数据库中是否存在，存在则删除
                DevopsConfigDTO devopsConfigDTO = baseQueryByResourceAndType(resourceId, resourceType, devopsConfigVO.getType());
                if (devopsConfigDTO != null) {
                    baseDelete(devopsConfigDTO.getId());
                }
            }
        });
    }


    @Override
    public List<DevopsConfigVO> queryByResourceId(Long resourceId, String resourceType) {

        List<DevopsConfigVO> devopsConfigVOS = new ArrayList<>();

        List<DevopsConfigDTO> devopsConfigDTOS = baseListByResource(resourceId, resourceType);
        devopsConfigDTOS.forEach(devopsConfigDTO -> {
            DevopsConfigVO devopsConfigVO = dtoToVo(devopsConfigDTO);
            //如果是项目层级下的harbor类型，需返回是否私有
            if (devopsConfigVO.getProjectId() != null && devopsConfigVO.getType().equals(HARBOR)) {
                DevopsProjectDTO devopsProjectDTO = devopsProjectService.baseQueryByProjectId(devopsConfigVO.getProjectId());
                devopsConfigVO.setHarborPrivate(devopsProjectDTO.getHarborProjectIsPrivate());
            }
            devopsConfigVOS.add(devopsConfigVO);
        });
        return devopsConfigVOS;
    }


    private void operateHarborProject(Long projectId, Boolean harborPrivate) {
        ProjectDTO projectDTO = iamServiceClientOperator.queryIamProjectById(projectId);
        OrganizationDTO organizationDTO = iamServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
        ConfigurationProperties configurationProperties = new ConfigurationProperties(harborConfigurationProperties);
        configurationProperties.setType(HARBOR);
        Retrofit retrofit = RetrofitHandler.initRetrofit(configurationProperties);
        HarborClient harborClient = retrofit.create(HarborClient.class);
        if (harborPrivate) {
            //设置为私有后将harbor项目设置为私有
            DevopsProjectDTO devopsProjectDTO = devopsProjectService.baseQueryByProjectId(projectId);
            String username = devopsProjectDTO.getHarborProjectUserName() == null ? String.format("user%s%s", organizationDTO.getId(), projectId) : devopsProjectDTO.getHarborProjectUserName();
            String email = devopsProjectDTO.getHarborProjectUserEmail() == null ? String.format("%s@choerodon.com", username) : devopsProjectDTO.getHarborProjectUserEmail();
            String password = devopsProjectDTO.getHarborProjectUserPassword() == null ? String.format("%sA", username) : devopsProjectDTO.getHarborProjectUserPassword();
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
                Response<List<ProjectDetail>> projects = harborClient.listProject(organizationDTO.getCode() + "-" + projectDTO.getCode()).execute();
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
                        result = harborClient.setProjectMember(projects.body().get(0).getProjectId(), new ProjectMember()).execute();
                    }
                    if (result.raw().code() != 200) {
                        throw new CommonException(result.errorBody().string());
                    }
                }
            } catch (IOException e) {
                throw new CommonException(e);
            }

            //更新项目表
            if (devopsProjectDTO.getHarborProjectUserPassword() == null) {
                devopsProjectDTO.setHarborProjectUserName(user.getUsername());
                devopsProjectDTO.setHarborProjectIsPrivate(true);
                devopsProjectDTO.setHarborProjectUserPassword(user.getPassword());
                devopsProjectDTO.setHarborProjectUserEmail(user.getEmail());
            }
            devopsProjectDTO.setHarborProjectIsPrivate(true);
            devopsProjectService.baseUpdate(devopsProjectDTO);
        } else {
            //设置为公有后将harbor项目设置为公有,删除成员角色
            try {
                Response<List<ProjectDetail>> projects = harborClient.listProject(organizationDTO.getCode() + "-" + projectDTO.getCode()).execute();
                if (!projects.body().isEmpty()) {
                    ProjectDetail projectDetail = new ProjectDetail();
                    Metadata metadata = new Metadata();
                    metadata.setHarborPublic("true");
                    projectDetail.setMetadata(metadata);
                    Response<Void> result = harborClient.updateProject(projects.body().get(0).getProjectId(), projectDetail).execute();
                    if (result.raw().code() != 200) {
                        throw new CommonException(result.errorBody().toString());
                    }
                    Response<SystemInfo> systemInfoResponse = harborClient.getSystemInfo().execute();
                    if (systemInfoResponse.raw().code() != 200) {
                        throw new CommonException(systemInfoResponse.errorBody().string());
                    }
                    if (systemInfoResponse.body().getHarborVersion().equals("v1.4.0")) {
                        Response<List<User>> users = harborClient.listUser(String.format("user%s%s", organizationDTO.getId(), projectId)).execute();
                        if (users.raw().code() != 200) {
                            throw new CommonException(users.errorBody().string());
                        }
                        harborClient.deleteLowVersionMember(projects.body().get(0).getProjectId(), users.body().get(0).getUserId().intValue()).execute();
                    } else {
                        Response<List<ProjectMember>> projectMembers = harborClient.getProjectMembers(projects.body().get(0).getProjectId(), String.format("user%s%s", organizationDTO.getId(), projectId)).execute();
                        if (projectMembers.raw().code() != 200) {
                            throw new CommonException(projectMembers.errorBody().string());
                        }
                        harborClient.deleteMember(projects.body().get(0).getProjectId(), projectMembers.body().get(0).getId()).execute();
                    }

                    DevopsProjectDTO devopsProjectDTO = devopsProjectService.baseQueryByProjectId(projectId);
                    devopsProjectDTO.setHarborProjectIsPrivate(false);
                    devopsProjectService.baseUpdate(devopsProjectDTO);

                }
            } catch (IOException e) {
                throw new CommonException(e);
            }
        }

    }

    @Override
    public DefaultConfigVO queryDefaultConfig(Long resourceId, String resourceType) {
        DefaultConfigVO defaultConfigVO = new DefaultConfigVO();

        //查询当前资源层级数据库中是否有对应的组件设置，有则返回url,无返回空，代表使用默认
        DevopsConfigDTO harborConfig = baseQueryByResourceAndType(resourceId, resourceType, HARBOR);
        if (harborConfig != null) {
            defaultConfigVO.setHarborConfigUrl(gson.fromJson(harborConfig.getConfig(), ConfigVO.class).getUrl());
        }
        DevopsConfigDTO chartConfig = baseQueryByResourceAndType(resourceId, resourceType, CHART);
        if (chartConfig != null) {
            defaultConfigVO.setChartConfigUrl(gson.fromJson(chartConfig.getConfig(), ConfigVO.class).getUrl());
        }
        return defaultConfigVO;
    }

    @Override
    public DevopsConfigDTO queryRealConfig(Long resourceId, String resourceType, String configType) {
        //应用服务层次，先找应用配置，在找项目配置,最后找组织配置,项目和组织层次同理
        DevopsConfigDTO defaultConfig = baseQueryDefaultConfig(configType);
        if (resourceType.equals(APP_SERVICE)) {
            DevopsConfigDTO appServiceConfig = baseQueryByResourceAndType(resourceId, resourceType, configType);
            if (appServiceConfig != null) {
                return appServiceConfig;
            }
            AppServiceDTO appServiceDTO = appServiceService.baseQuery(resourceId);
            Long projectId = devopsProjectService.queryProjectIdByAppId(appServiceDTO.getAppId());
            DevopsConfigDTO projectConfig = baseQueryByResourceAndType(projectId, ResourceLevel.PROJECT.value(), configType);
            if (projectConfig != null) {
                return projectConfig;
            }
            ProjectDTO projectDTO = iamServiceClientOperator.queryIamProjectById(projectId);
            OrganizationDTO organizationDTO = iamServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
            DevopsConfigDTO organizationConfig = baseQueryByResourceAndType(organizationDTO.getId(), ResourceLevel.ORGANIZATION.value(), configType);
            if (organizationConfig != null) {
                return organizationConfig;
            }
            return defaultConfig;
        } else if (resourceType.equals(ResourceLevel.PROJECT.value())) {
            DevopsConfigDTO projectConfig = baseQueryByResourceAndType(resourceId, ResourceLevel.PROJECT.value(), configType);
            if (projectConfig != null) {
                return projectConfig;
            }
            ProjectDTO projectDTO = iamServiceClientOperator.queryIamProjectById(resourceId);
            OrganizationDTO organizationDTO = iamServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
            DevopsConfigDTO organizationConfig = baseQueryByResourceAndType(organizationDTO.getId(), ResourceLevel.ORGANIZATION.value(), configType);
            if (organizationConfig != null) {
                return organizationConfig;
            }
            return defaultConfig;
        } else {
            DevopsConfigDTO organizationConfig = baseQueryByResourceAndType(resourceId, ResourceLevel.ORGANIZATION.value(), configType);
            if (organizationConfig != null) {
                return organizationConfig;
            }
            return defaultConfig;
        }
    }


    public DevopsConfigDTO baseCreate(DevopsConfigDTO devopsConfigDTO) {
        if (devopsConfigMapper.insert(devopsConfigDTO) != 1) {
            throw new CommonException("error.devops.project.config.create");
        }
        return devopsConfigDTO;
    }

    /**
     * @param devopsConfigDTO
     * @return true为不存在同名值  false存在
     */
    public Boolean baseCheckByName(DevopsConfigDTO devopsConfigDTO) {
        return ObjectUtils.isEmpty(devopsConfigMapper.selectOne(devopsConfigDTO));
    }

    public DevopsConfigDTO baseUpdate(DevopsConfigDTO devopsConfigDTO) {
        if (devopsConfigMapper.updateByPrimaryKeySelective(devopsConfigDTO) != 1) {
            throw new CommonException("error.devops.project.config.update");
        }
        return devopsConfigMapper.selectByPrimaryKey(devopsConfigDTO);
    }

    @Override
    public DevopsConfigDTO baseQuery(Long id) {
        return devopsConfigMapper.selectByPrimaryKey(id);
    }


    public DevopsConfigDTO baseQueryByName(Long projectId, String name) {
        DevopsConfigDTO paramDO = new DevopsConfigDTO();
        paramDO.setProjectId(projectId);
        paramDO.setName(name);
        return devopsConfigMapper.selectOne(paramDO);
    }

    public DevopsConfigDTO baseCheckByName(String name) {
        return devopsConfigMapper.queryByNameWithNoProject(name);
    }

    public PageInfo<DevopsConfigDTO> basePageByOptions(Long projectId, PageRequest pageRequest, String params) {
        Map<String, Object> mapParams = TypeUtil.castMapParams(params);

        return PageHelper.startPage(pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest))
                .doSelectPageInfo(() -> devopsConfigMapper.listByOptions(projectId,
                        TypeUtil.cast(mapParams.get(TypeUtil.SEARCH_PARAM)),
                        TypeUtil.cast(mapParams.get(TypeUtil.PARAMS)), PageRequestUtil.checkSortIsEmpty(pageRequest)));
    }

    public void baseDelete(Long id) {
        if (devopsConfigMapper.deleteByPrimaryKey(id) != 1) {
            throw new CommonException("error.devops.project.config.delete");
        }
    }

    @Override
    public List<DevopsConfigDTO> baseListByIdAndType(Long projectId, String type) {
        return devopsConfigMapper.listByIdAndType(projectId, type);
    }

    public void baseCheckByName(Long projectId, String name) {
        DevopsConfigDTO devopsConfigDTO = new DevopsConfigDTO();
        devopsConfigDTO.setProjectId(projectId);
        devopsConfigDTO.setName(name);
        if (devopsConfigMapper.selectOne(devopsConfigDTO) != null) {
            throw new CommonException("error.project.config.exist");
        }
    }

    @Override
    public DevopsConfigDTO baseQueryByResourceAndType(Long resourceId, String resourceType, String type) {
        DevopsConfigDTO devopsConfigDTO = new DevopsConfigDTO();
        setResourceId(resourceId, resourceType, devopsConfigDTO);
        devopsConfigDTO.setType(type);
        return devopsConfigMapper.selectOne(devopsConfigDTO);
    }


    public DevopsConfigDTO baseQueryDefaultConfig(String type) {
        return devopsConfigMapper.queryDefaultConfig(type);
    }

    private void setResourceId(Long resourceId, String resourceType, DevopsConfigDTO devopsConfigDTO) {
        if (ResourceLevel.ORGANIZATION.value().equals(resourceType)) {
            devopsConfigDTO.setOrganizationId(resourceId);
        } else if (ResourceLevel.PROJECT.value().equals(resourceType)) {
            devopsConfigDTO.setProjectId(resourceId);
        } else {
            devopsConfigDTO.setAppServiceId(resourceId);
        }
    }

    public Boolean baseCheckUsed(Long checkIsUsed) {
        return devopsConfigMapper.checkIsUsed(checkIsUsed).isEmpty();
    }

    public List<DevopsConfigDTO> baseListByResource(Long resourceId, String resourceType) {
        DevopsConfigDTO devopsConfigDTO = new DevopsConfigDTO();
        setResourceId(resourceId, resourceType, devopsConfigDTO);
        return devopsConfigMapper.select(devopsConfigDTO);
    }

    public DevopsConfigVO dtoToVo(DevopsConfigDTO devopsConfigDTO) {
        DevopsConfigVO devopsConfigVO = new DevopsConfigVO();
        BeanUtils.copyProperties(devopsConfigDTO, devopsConfigVO);
        ConfigVO configVO = gson.fromJson(devopsConfigDTO.getConfig(), ConfigVO.class);
        devopsConfigVO.setConfig(configVO);
        return devopsConfigVO;
    }

    public DevopsConfigDTO voToDto(DevopsConfigVO devopsConfigVO) {
        DevopsConfigDTO devopsConfigDTO = new DevopsConfigDTO();
        BeanUtils.copyProperties(devopsConfigVO, devopsConfigDTO);
        String configJson = gson.toJson(devopsConfigVO.getConfig());
        devopsConfigDTO.setConfig(configJson);
        return devopsConfigDTO;
    }

    private void checkRegistryProjectIsPrivate(DevopsConfigVO devopsConfigVO) {
        ConfigurationProperties configurationProperties = new ConfigurationProperties();
        configurationProperties.setBaseUrl(devopsConfigVO.getConfig().getUrl());
        configurationProperties.setUsername(devopsConfigVO.getConfig().getUserName());
        configurationProperties.setPassword(devopsConfigVO.getConfig().getPassword());
        configurationProperties.setInsecureSkipTlsVerify(false);
        configurationProperties.setProject(devopsConfigVO.getConfig().getProject());
        configurationProperties.setType(HARBOR);
        Retrofit retrofit = RetrofitHandler.initRetrofit(configurationProperties);
        HarborClient harborClient = retrofit.create(HarborClient.class);
        Call<List<ProjectDetail>> listProject = harborClient.listProject(devopsConfigVO.getConfig().getProject());
        Response<List<ProjectDetail>> projectResponse = null;
        try {
            projectResponse = listProject.execute();
            if (projectResponse != null && projectResponse.body() != null) {
                if ("false".equals(projectResponse.body().get(0).getMetadata().getHarborPublic())) {
                    devopsConfigVO.getConfig().setPrivate(true);
                } else {
                    devopsConfigVO.getConfig().setPrivate(false);
                }
            }
        } catch (IOException e) {
            throw new CommonException(e);
        }
    }
}


