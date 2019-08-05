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
import io.choerodon.devops.api.vo.DevopsProjectConfigVO;
import io.choerodon.devops.api.vo.ProjectConfigVO;
import io.choerodon.devops.api.vo.ProjectDefaultConfigVO;
import io.choerodon.devops.app.service.AppSevriceService;
import io.choerodon.devops.app.service.DevopsProjectConfigService;
import io.choerodon.devops.app.service.DevopsProjectService;
import io.choerodon.devops.infra.config.ConfigurationProperties;
import io.choerodon.devops.infra.config.HarborConfigurationProperties;
import io.choerodon.devops.infra.dto.DevopsProjectConfigDTO;
import io.choerodon.devops.infra.dto.DevopsProjectDTO;
import io.choerodon.devops.infra.dto.harbor.*;
import io.choerodon.devops.infra.dto.iam.OrganizationDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.feign.HarborClient;
import io.choerodon.devops.infra.feign.operator.IamServiceClientOperator;
import io.choerodon.devops.infra.handler.RetrofitHandler;
import io.choerodon.devops.infra.mapper.DevopsProjectConfigMapper;
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
public class DevopsProjectConfigServiceImpl implements DevopsProjectConfigService {

    private static final String HARBOR = "harbor";
    private static final String CHART = "chart";
    private static final Gson gson = new Gson();


    @Autowired
    private DevopsProjectConfigMapper devopsProjectConfigMapper;

    @Autowired
    private DevopsProjectConfigValidator configValidator;

    @Autowired
    private IamServiceClientOperator iamServiceClientOperator;

    @Autowired
    private HarborConfigurationProperties harborConfigurationProperties;

    @Autowired
    private DevopsProjectService devopsProjectService;

    @Autowired
    private AppSevriceService applicationService;

    @Override
    public DevopsProjectConfigVO create(Long projectId, DevopsProjectConfigVO devopsProjectConfigVO) {

        configValidator.checkConfigType(devopsProjectConfigVO);
        baseCheckByName(projectId, devopsProjectConfigVO.getName());

        if (devopsProjectConfigVO.getType().equals(HARBOR) && devopsProjectConfigVO.getConfig().getProject() != null) {
            checkRegistryProjectIsPrivate(devopsProjectConfigVO);
        }
        devopsProjectConfigVO.setProjectId(projectId);

        DevopsProjectConfigDTO devopsProjectConfigDTO = baseCreate(voToDto(devopsProjectConfigVO));
        return dtoToVo(devopsProjectConfigDTO);
    }


    @Override
    public DevopsProjectConfigVO update(Long projectId, DevopsProjectConfigVO devopsProjectConfigVO) {
        if (devopsProjectConfigVO.getType().equals(HARBOR) && devopsProjectConfigVO.getConfig().getProject() != null) {
            checkRegistryProjectIsPrivate(devopsProjectConfigVO);
        }
        if (!ObjectUtils.isEmpty(devopsProjectConfigVO.getType())) {
            configValidator.checkConfigType(devopsProjectConfigVO);
        }
        return dtoToVo(baseUpdate(voToDto(devopsProjectConfigVO)));
    }

    @Override
    public DevopsProjectConfigVO queryById(Long id) {
        return dtoToVo(baseQuery(id));
    }

    @Override
    public PageInfo<DevopsProjectConfigVO> pageByOptions(Long projectId, PageRequest pageRequest, String params) {
        return ConvertUtils.convertPage(basePageByOptions(projectId, pageRequest, params), this::dtoToVo);
    }

    @Override
    public void delete(Long id) {
        baseDelete(id);
    }

    @Override
    public List<DevopsProjectConfigVO> listByIdAndType(Long projectId, String type) {
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
            DevopsProjectConfigVO devopsProjectConfigVO = new DevopsProjectConfigVO();
            devopsProjectConfigVO.setName("project_harbor_default");
            devopsProjectConfigVO.setType(HARBOR);
            devopsProjectConfigVO.setProjectId(projectId);
            ProjectConfigVO projectConfigVO = new ProjectConfigVO();
            projectConfigVO.setPrivate(true);
            projectConfigVO.setUrl(harborConfigurationProperties.getBaseUrl());
            projectConfigVO.setPassword(user.getPassword());
            projectConfigVO.setUserName(user.getUsername());
            projectConfigVO.setEmail(user.getEmail());
            projectConfigVO.setProject(organizationDTO.getCode() + "-" + projectDTO.getCode());
            devopsProjectConfigVO.setConfig(projectConfigVO);
            DevopsProjectConfigDTO devopsProjectConfigDTO = baseCreate(voToDto(devopsProjectConfigVO));

            //更新项目下所有应用的harbor配置为该默认配置
            List<DevopsProjectConfigDTO> oldDevopsProjectConfigDTOS = baseListByIdAndType(projectId, HARBOR);
            applicationService.baseUpdateHarborConfig(projectId, devopsProjectConfigDTO.getId(), oldDevopsProjectConfigDTOS.get(0).getId(), true);
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
                    DevopsProjectConfigDTO devopsProjectConfigDTO = baseQueryByName(projectId, "project_harbor_default");
                    DevopsProjectConfigDTO newDevopsProjectConfigDTO = baseQueryByName(null, "harbor_default");

                    DevopsProjectDTO devopsProjectDTO = devopsProjectService.baseQueryByProjectId(projectId);
                    devopsProjectDTO.setHarborProjectIsPrivate(false);
                    devopsProjectService.baseUpdate(devopsProjectDTO);
                    applicationService.baseUpdateHarborConfig(projectId, newDevopsProjectConfigDTO.getId(), devopsProjectConfigDTO.getId(), false);
                    baseDelete(devopsProjectConfigDTO.getId());

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
        List<DevopsProjectConfigDTO> harborConfigs = baseListByIdAndType(projectId, HARBOR);
        Optional<DevopsProjectConfigDTO> devopsProjectConfigDTO = harborConfigs.stream().filter(result -> result.getName().equals("project_harbor_default")).findFirst();
        if (devopsProjectConfigDTO.isPresent()) {
            projectDefaultConfigVO.setHarborConfigName(devopsProjectConfigDTO.get().getName());
        } else {
            projectDefaultConfigVO.setHarborConfigName(harborConfigs.get(0).getName());
        }
        List<DevopsProjectConfigDTO> chartConfigs = baseListByIdAndType(projectId, CHART);
        Optional<DevopsProjectConfigDTO> chartConfig = chartConfigs.stream().filter(result -> result.getName().equals("chart_default")).findFirst();

        if (chartConfig.isPresent()) {
            projectDefaultConfigVO.setChartConfigName(chartConfig.get().getName());
        }

        return projectDefaultConfigVO;
    }


    public DevopsProjectConfigDTO baseCreate(DevopsProjectConfigDTO devopsProjectConfigDTO) {
        if (devopsProjectConfigMapper.insert(devopsProjectConfigDTO) != 1) {
            throw new CommonException("error.devops.project.config.create");
        }
        return devopsProjectConfigDTO;
    }

    /**
     * @param devopsProjectConfigDTO
     * @return true为不存在同名值  false存在
     */
    public Boolean baseCheckByName(DevopsProjectConfigDTO devopsProjectConfigDTO) {
        return ObjectUtils.isEmpty(devopsProjectConfigMapper.selectOne(devopsProjectConfigDTO));
    }

    public DevopsProjectConfigDTO baseUpdate(DevopsProjectConfigDTO devopsProjectConfigDTO) {
        if (devopsProjectConfigMapper.updateByPrimaryKeySelective(devopsProjectConfigDTO) != 1) {
            throw new CommonException("error.devops.project.config.update");
        }
        return devopsProjectConfigMapper.selectByPrimaryKey(devopsProjectConfigDTO);
    }

    @Override
    public DevopsProjectConfigDTO baseQuery(Long id) {
        return devopsProjectConfigMapper.selectByPrimaryKey(id);
    }


    public DevopsProjectConfigDTO baseQueryByName(Long projectId, String name) {
        DevopsProjectConfigDTO paramDO = new DevopsProjectConfigDTO();
        paramDO.setProjectId(projectId);
        paramDO.setName(name);
        return devopsProjectConfigMapper.selectOne(paramDO);
    }

    public DevopsProjectConfigDTO baseCheckByName(String name) {
        return devopsProjectConfigMapper.queryByNameWithNoProject(name);
    }

    public PageInfo<DevopsProjectConfigDTO> basePageByOptions(Long projectId, PageRequest pageRequest, String params) {
        Map<String, Object> mapParams = TypeUtil.castMapParams(params);

        return PageHelper.startPage(pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest))
                .doSelectPageInfo(() -> devopsProjectConfigMapper.listByOptions(projectId,
                        (Map<String, Object>) mapParams.get(TypeUtil.SEARCH_PARAM),
                        (String) mapParams.get(TypeUtil.PARAM), PageRequestUtil.checkSortIsEmpty(pageRequest)));
    }

    public void baseDelete(Long id) {
        if (devopsProjectConfigMapper.deleteByPrimaryKey(id) != 1) {
            throw new CommonException("error.devops.project.config.delete");
        }
    }

    @Override
    public List<DevopsProjectConfigDTO> baseListByIdAndType(Long projectId, String type) {
        return devopsProjectConfigMapper.listByIdAndType(projectId, type);
    }

    public void baseCheckByName(Long projectId, String name) {
        DevopsProjectConfigDTO projectConfigDO = new DevopsProjectConfigDTO();
        projectConfigDO.setProjectId(projectId);
        projectConfigDO.setName(name);
        if (devopsProjectConfigMapper.selectOne(projectConfigDO) != null) {
            throw new CommonException("error.project.config.exist");
        }
    }

    public Boolean baseCheckUsed(Long checkIsUsed) {
        return devopsProjectConfigMapper.checkIsUsed(checkIsUsed).isEmpty();
    }


    private DevopsProjectConfigVO dtoToVo(DevopsProjectConfigDTO devopsProjectConfigDTO) {
        DevopsProjectConfigVO devopsProjectConfigVO = new DevopsProjectConfigVO();
        BeanUtils.copyProperties(devopsProjectConfigDTO, devopsProjectConfigVO);
        ProjectConfigVO projectConfigVO = gson.fromJson(devopsProjectConfigDTO.getConfig(), ProjectConfigVO.class);
        devopsProjectConfigVO.setConfig(projectConfigVO);
        return devopsProjectConfigVO;
    }

    private DevopsProjectConfigDTO voToDto(DevopsProjectConfigVO devopsProjectConfigVO) {
        DevopsProjectConfigDTO devopsProjectConfigDTO = new DevopsProjectConfigDTO();
        BeanUtils.copyProperties(devopsProjectConfigVO, devopsProjectConfigDTO);
        String configJson = gson.toJson(devopsProjectConfigVO.getConfig());
        devopsProjectConfigDTO.setConfig(configJson);
        return devopsProjectConfigDTO;
    }

    private void checkRegistryProjectIsPrivate(DevopsProjectConfigVO devopsProjectConfigVO) {
        ConfigurationProperties configurationProperties = new ConfigurationProperties();
        configurationProperties.setBaseUrl(devopsProjectConfigVO.getConfig().getUrl());
        configurationProperties.setUsername(devopsProjectConfigVO.getConfig().getUserName());
        configurationProperties.setPassword(devopsProjectConfigVO.getConfig().getPassword());
        configurationProperties.setInsecureSkipTlsVerify(false);
        configurationProperties.setProject(devopsProjectConfigVO.getConfig().getProject());
        configurationProperties.setType(HARBOR);
        Retrofit retrofit = RetrofitHandler.initRetrofit(configurationProperties);
        HarborClient harborClient = retrofit.create(HarborClient.class);
        Call<List<ProjectDetail>> listProject = harborClient.listProject(devopsProjectConfigVO.getConfig().getProject());
        Response<List<ProjectDetail>> projectResponse = null;
        try {
            projectResponse = listProject.execute();
            if (projectResponse != null && projectResponse.body() != null) {
                if ("false".equals(projectResponse.body().get(0).getMetadata().getHarborPublic())) {
                    devopsProjectConfigVO.getConfig().setPrivate(true);
                } else {
                    devopsProjectConfigVO.getConfig().setPrivate(false);
                }
            }
        } catch (IOException e) {
            throw new CommonException(e);
        }
    }
}


