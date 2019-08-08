package io.choerodon.devops.app.service.impl;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.validator.DevopsProjectConfigValidator;
import io.choerodon.devops.api.vo.DevopsConfigVO;
import io.choerodon.devops.api.vo.ProjectConfigVO;
import io.choerodon.devops.api.vo.ProjectDefaultConfigVO;
import io.choerodon.devops.app.service.AppServiceService;
import io.choerodon.devops.app.service.DevopsConfigService;
import io.choerodon.devops.app.service.DevopsProjectService;
import io.choerodon.devops.infra.config.ConfigurationProperties;
import io.choerodon.devops.infra.config.HarborConfigurationProperties;
import io.choerodon.devops.infra.dto.DevopsConfigDTO;
import io.choerodon.devops.infra.dto.DevopsProjectDTO;
import io.choerodon.devops.infra.dto.harbor.*;
import io.choerodon.devops.infra.dto.iam.OrganizationDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.feign.HarborClient;
import io.choerodon.devops.infra.feign.operator.IamServiceClientOperator;
import io.choerodon.devops.infra.handler.RetrofitHandler;
import io.choerodon.devops.infra.mapper.DevopsConfigMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
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
    private static final Gson gson = new Gson();


    @Autowired
    private DevopsConfigMapper devopsConfigMapper;

    @Autowired
    private DevopsProjectConfigValidator configValidator;

    @Autowired
    private IamServiceClientOperator iamServiceClientOperator;

    @Autowired
    private HarborConfigurationProperties harborConfigurationProperties;

    @Autowired
    private DevopsProjectService devopsProjectService;

    @Autowired
    private AppServiceService applicationService;

    @Override
    public DevopsConfigVO create(Long projectId, DevopsConfigVO devopsConfigVO) {

        configValidator.checkConfigType(devopsConfigVO);
        baseCheckByName(projectId, devopsConfigVO.getName());

        if (devopsConfigVO.getType().equals(HARBOR) && devopsConfigVO.getConfig().getProject() != null) {
            checkRegistryProjectIsPrivate(devopsConfigVO);
        }
        devopsConfigVO.setProjectId(projectId);

        DevopsConfigDTO devopsConfigDTO = baseCreate(voToDto(devopsConfigVO));
        return dtoToVo(devopsConfigDTO);
    }


    @Override
    public DevopsConfigVO update(Long projectId, DevopsConfigVO devopsConfigVO) {
        if (devopsConfigVO.getType().equals(HARBOR) && devopsConfigVO.getConfig().getProject() != null) {
            checkRegistryProjectIsPrivate(devopsConfigVO);
        }
        if (!ObjectUtils.isEmpty(devopsConfigVO.getType())) {
            configValidator.checkConfigType(devopsConfigVO);
        }
        return dtoToVo(baseUpdate(voToDto(devopsConfigVO)));
    }

    @Override
    public DevopsConfigVO queryById(Long id) {
        return dtoToVo(baseQuery(id));
    }

    @Override
    public PageInfo<DevopsConfigVO> pageByOptions(Long projectId, PageRequest pageRequest, String params) {
        return ConvertUtils.convertPage(basePageByOptions(projectId, pageRequest, params), this::dtoToVo);
    }

    @Override
    public void delete(Long id) {
        baseDelete(id);
    }

    @Override
    public List<DevopsConfigVO> listByIdAndType(Long projectId, String type) {
        return ConvertUtils.convertList(baseListByIdAndType(projectId, type), this::dtoToVo);
    }

    @Override
    public void checkName(Long projectId, String name) {
        baseCheckByName(projectId, name);
    }

    @Override
    public Boolean checkIsUsed(Long configId) {
        return baseCheckUsed(configId);
    }

    @Override
    public void operateHarborProject(Long projectId, Boolean harborPrivate) {
        ProjectDTO projectDTO = iamServiceClientOperator.queryIamProjectById(projectId);
        OrganizationDTO organizationDTO = iamServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
        ConfigurationProperties configurationProperties = new ConfigurationProperties(harborConfigurationProperties);
        configurationProperties.setType(HARBOR);
        Retrofit retrofit = RetrofitHandler.initRetrofit(configurationProperties);
        HarborClient harborClient = retrofit.create(HarborClient.class);
        if (harborPrivate) {
            //设置为私有后将harbor项目设置为私有,新增项目默认harbor配置,以及更新项目下所有应用的默认harbor配置
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


            //新增项目harbor配置
            DevopsConfigVO devopsConfigVO = new DevopsConfigVO();
            devopsConfigVO.setName("project_harbor_default");
            devopsConfigVO.setType(HARBOR);
            devopsConfigVO.setProjectId(projectId);
            ProjectConfigVO projectConfigVO = new ProjectConfigVO();
            projectConfigVO.setPrivate(true);
            projectConfigVO.setUrl(harborConfigurationProperties.getBaseUrl());
            projectConfigVO.setPassword(user.getPassword());
            projectConfigVO.setUserName(user.getUsername());
            projectConfigVO.setEmail(user.getEmail());
            projectConfigVO.setProject(organizationDTO.getCode() + "-" + projectDTO.getCode());
            devopsConfigVO.setConfig(projectConfigVO);
            DevopsConfigDTO devopsConfigDTO = baseCreate(voToDto(devopsConfigVO));

            //更新项目下所有应用的harbor配置为该默认配置
            List<DevopsConfigDTO> oldDevopsConfigDTOS = baseListByIdAndType(projectId, HARBOR);
            applicationService.baseUpdateHarborConfig(projectId, devopsConfigDTO.getId(), oldDevopsConfigDTOS.get(0).getId(), true);
        } else {
            //设置为公有后将harbor项目设置为公有,删除成员角色,并删除项目默认harbor配置,以及更新项目下所有应用的默认harbor配置
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
                    DevopsConfigDTO devopsConfigDTO = baseQueryByName(projectId, "project_harbor_default");
                    DevopsConfigDTO newDevopsConfigDTO = baseQueryByName(null, "harbor_default");

                    DevopsProjectDTO devopsProjectDTO = devopsProjectService.baseQueryByProjectId(projectId);
                    devopsProjectDTO.setHarborProjectIsPrivate(false);
                    devopsProjectService.baseUpdate(devopsProjectDTO);
                    applicationService.baseUpdateHarborConfig(projectId, newDevopsConfigDTO.getId(), devopsConfigDTO.getId(), false);
                    baseDelete(devopsConfigDTO.getId());

                }
            } catch (IOException e) {
                throw new CommonException(e);
            }
        }

    }

    @Override
    public ProjectDefaultConfigVO queryProjectDefaultConfig(Long projectId) {
        ProjectDefaultConfigVO projectDefaultConfigVO = new ProjectDefaultConfigVO();
        DevopsProjectDTO devopsProjectDTO = devopsProjectService.baseQueryByProjectId(projectId);
        projectDefaultConfigVO.setHarborIsPrivate(devopsProjectDTO.getHarborProjectIsPrivate());
        List<DevopsConfigDTO> harborConfigs = baseListByIdAndType(projectId, HARBOR);
        Optional<DevopsConfigDTO> devopsProjectConfigDTO = harborConfigs.stream().filter(result -> result.getName().equals("project_harbor_default")).findFirst();
        if (devopsProjectConfigDTO.isPresent()) {
            projectDefaultConfigVO.setHarborConfigName(devopsProjectConfigDTO.get().getName());
        } else {
            projectDefaultConfigVO.setHarborConfigName(harborConfigs.get(0).getName());
        }
        List<DevopsConfigDTO> chartConfigs = baseListByIdAndType(projectId, CHART);
        Optional<DevopsConfigDTO> chartConfig = chartConfigs.stream().filter(result -> result.getName().equals("chart_default")).findFirst();

        if (chartConfig.isPresent()) {
            projectDefaultConfigVO.setChartConfigName(chartConfig.get().getName());
        }

        return projectDefaultConfigVO;
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
                        (Map<String, Object>) mapParams.get(TypeUtil.SEARCH_PARAM),
                        (String) mapParams.get(TypeUtil.PARAMS), PageRequestUtil.checkSortIsEmpty(pageRequest)));
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
        DevopsConfigDTO projectConfigDO = new DevopsConfigDTO();
        projectConfigDO.setProjectId(projectId);
        projectConfigDO.setName(name);
        if (devopsConfigMapper.selectOne(projectConfigDO) != null) {
            throw new CommonException("error.project.config.exist");
        }
    }

    public Boolean baseCheckUsed(Long checkIsUsed) {
        return devopsConfigMapper.checkIsUsed(checkIsUsed).isEmpty();
    }


    private DevopsConfigVO dtoToVo(DevopsConfigDTO devopsConfigDTO) {
        DevopsConfigVO devopsConfigVO = new DevopsConfigVO();
        BeanUtils.copyProperties(devopsConfigDTO, devopsConfigVO);
        ProjectConfigVO projectConfigVO = gson.fromJson(devopsConfigDTO.getConfig(), ProjectConfigVO.class);
        devopsConfigVO.setConfig(projectConfigVO);
        return devopsConfigVO;
    }

    private DevopsConfigDTO voToDto(DevopsConfigVO devopsConfigVO) {
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


