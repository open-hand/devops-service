package io.choerodon.devops.app.service.impl;

import java.io.IOException;
import java.util.*;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.validator.DevopsProjectConfigValidator;
import io.choerodon.devops.api.vo.DevopsProjectConfigVO;
import io.choerodon.devops.api.vo.ProjectConfigVO;
import io.choerodon.devops.api.vo.ProjectDefaultConfigDTO;
import io.choerodon.devops.api.vo.ProjectVO;
import io.choerodon.devops.api.vo.iam.entity.DevopsProjectVO;
import io.choerodon.devops.app.service.DevopsProjectConfigService;
import io.choerodon.devops.app.service.ProjectConfigHarborService;
import io.choerodon.devops.domain.application.repository.DevopsProjectConfigRepository;
import io.choerodon.devops.domain.application.repository.DevopsProjectRepository;
import io.choerodon.devops.domain.application.valueobject.OrganizationVO;
import io.choerodon.devops.infra.config.ConfigurationProperties;
import io.choerodon.devops.infra.config.HarborConfigurationProperties;
import io.choerodon.devops.infra.dto.DevopsProjectConfigDTO;
import io.choerodon.devops.infra.dto.harbor.*;
import io.choerodon.devops.infra.feign.HarborClient;
import io.choerodon.devops.infra.handler.RetrofitHandler;
import io.choerodon.devops.infra.mapper.DevopsProjectConfigMapper;
import io.choerodon.devops.infra.util.PageRequestUtil;
import io.choerodon.devops.infra.util.TypeUtil;
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

    @Autowired
    private DevopsProjectConfigMapper devopsProjectConfigMapper;


    @Override
    public DevopsProjectConfigVO create(Long projectId, DevopsProjectConfigVO devopsProjectConfigVO) {
        if (devopsProjectConfigVO.getType().equals(HARBOR) && devopsProjectConfigVO.getConfig().getProject() != null) {
            checkRegistryProjectIsPrivate(devopsProjectConfigVO);
        }
        DevopsProjectConfigE devopsProjectConfigE = ConvertHelper.convert(devopsProjectConfigVO, DevopsProjectConfigE.class);
        devopsProjectConfigE.setProjectId(projectId);
        configValidator.checkConfigType(devopsProjectConfigVO);

        devopsProjectConfigRepository.baseCheckByName(projectId, devopsProjectConfigE.getName());
        DevopsProjectConfigE res = devopsProjectConfigRepository.baseCreate(devopsProjectConfigE);
        return ConvertHelper.convert(res, DevopsProjectConfigVO.class);
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

    @Override
    public DevopsProjectConfigVO updateByPrimaryKeySelective(Long projectId, DevopsProjectConfigVO devopsProjectConfigVO) {
        if (devopsProjectConfigVO.getType().equals(HARBOR) && devopsProjectConfigVO.getConfig().getProject() != null) {
            checkRegistryProjectIsPrivate(devopsProjectConfigVO);
        }
        DevopsProjectConfigE devopsProjectConfigE = ConvertHelper.convert(devopsProjectConfigVO, DevopsProjectConfigE.class);
        if (!ObjectUtils.isEmpty(devopsProjectConfigVO.getType())) {
            configValidator.checkConfigType(devopsProjectConfigVO);
        }
        return ConvertHelper.convert(devopsProjectConfigRepository.baseUpdate(devopsProjectConfigE), DevopsProjectConfigVO.class);
    }

    @Override
    public DevopsProjectConfigVO queryByPrimaryKey(Long id) {
        return ConvertHelper.convert(devopsProjectConfigRepository.baseQuery(id), DevopsProjectConfigVO.class);
    }

    @Override
    public PageInfo<DevopsProjectConfigVO> listByOptions(Long projectId, PageRequest pageRequest, String params) {
        return ConvertPageHelper.convertPageInfo(devopsProjectConfigRepository.basePageByOptions(projectId, pageRequest, params), DevopsProjectConfigVO.class);
    }

    @Override
    public void delete(Long id) {
        devopsProjectConfigRepository.baseDelete(id);
    }

    @Override
    public List<DevopsProjectConfigVO> queryByIdAndType(Long projectId, String type) {
        return ConvertHelper.convertList(devopsProjectConfigRepository.baseListByIdAndType(projectId, type), DevopsProjectConfigVO.class);
    }

    @Override
    public void checkName(Long projectId, String name) {
        devopsProjectConfigRepository.baseCheckByName(projectId, name);
    }

    @Override
    public Boolean checkIsUsed(Long configId) {
        return devopsProjectConfigRepository.baseCheckUsed(configId);
    }

    @Override
    public void setHarborProjectIsPrivate(Long projectId, boolean harborPrivate) {
        ProjectVO projectE = iamRepository.queryIamProject(projectId);
        OrganizationVO organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        ConfigurationProperties configurationProperties = new ConfigurationProperties(harborConfigurationProperties);
        configurationProperties.setType("harbor");
        Retrofit retrofit = RetrofitHandler.initRetrofit(configurationProperties);
        HarborClient harborClient = retrofit.create(HarborClient.class);
        if (harborPrivate) {
            //设置为私有后将harbor项目设置为私有,新增项目默认harbor配置,以及更新项目下所有应用的默认harbor配置
            DevopsProjectVO devopsProjectE = devopsProjectRepository.baseQueryByProjectId(projectId);
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
                    if (result.raw().code() != 201) {
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
            devopsProjectRepository.baseUpdate(ConvertHelper.convert(devopsProjectE, DevopsProjectDTO.class));


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
            projectConfigVO.setProject(organization.getCode() + "-" + projectE.getCode());
            devopsProjectConfigVO.setConfig(projectConfigVO);
            DevopsProjectConfigE devopsProjectConfigE = devopsProjectConfigRepository.baseCreate(ConvertHelper.convert(devopsProjectConfigVO, DevopsProjectConfigE.class));

            //更新项目下所有应用的harbor配置为该默认配置
            List<DevopsProjectConfigE> oldDevopsProjectConfigEs = devopsProjectConfigRepository.baseListByIdAndType(projectId, HARBOR);
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
                    Response<SystemInfo> systemInfoResponse = harborClient.getSystemInfo().execute();
                    if (systemInfoResponse.raw().code() != 200) {
                        throw new CommonException(systemInfoResponse.errorBody().string());
                    }
                    if (systemInfoResponse.body().getHarborVersion().equals("v1.4.0")) {
                        Response<List<User>> users = harborClient.listUser(String.format("user%s%s", organization.getId(), projectId)).execute();
                        if (users.raw().code() != 200) {
                            throw new CommonException(users.errorBody().string());
                        }
                        harborClient.deleteLowVersionMember(projects.body().get(0).getProjectId(), users.body().get(0).getUserId().intValue()).execute();
                    } else {
                        Response<List<ProjectMember>> projectMembers = harborClient.getProjectMembers(projects.body().get(0).getProjectId(), String.format("user%s%s", organization.getId(), projectId)).execute();
                        if (projectMembers.raw().code() != 200) {
                            throw new CommonException(projectMembers.errorBody().string());
                        }
                        harborClient.deleteMember(projects.body().get(0).getProjectId(), projectMembers.body().get(0).getId().intValue()).execute();
                    }
                    DevopsProjectConfigE devopsProjectConfigE = devopsProjectConfigRepository.baseQueryByName(projectId, "project_harbor_default");
                    DevopsProjectConfigE newDevopsProjectConfigE = devopsProjectConfigRepository.baseQueryByName(null, "harbor_default");

                    DevopsProjectVO devopsProjectE = devopsProjectRepository.baseQueryByProjectId(projectId);
                    devopsProjectE.setHarborProjectIsPrivate(false);
                    devopsProjectRepository.baseUpdate(ConvertHelper.convert(devopsProjectE, DevopsProjectDTO.class));
                    applicationRepository.updateAppHarborConfig(projectId, newDevopsProjectConfigE.getId(), devopsProjectConfigE.getId(), false);
                    devopsProjectConfigRepository.baseDelete(devopsProjectConfigE.getId());

                }
            } catch (IOException e) {
                throw new CommonException(e);
            }
        }

    }

    @Override
    public ProjectDefaultConfigDTO getProjectDefaultConfig(Long projectId) {
        ProjectDefaultConfigDTO projectDefaultConfigDTO = new ProjectDefaultConfigDTO();
        DevopsProjectVO devopsProjectE = devopsProjectRepository.baseQueryByProjectId(projectId);
        projectDefaultConfigDTO.setHarborIsPrivate(devopsProjectE.getHarborProjectIsPrivate());
        List<DevopsProjectConfigE> harborConfigs = devopsProjectConfigRepository.baseListByIdAndType(projectId, HARBOR);
        Optional<DevopsProjectConfigE> devopsConfigE = harborConfigs.stream().filter(devopsProjectConfigE -> devopsProjectConfigE.getName().equals("project_harbor_default")).findFirst();
        if (devopsConfigE.isPresent()) {
            projectDefaultConfigDTO.setHarborConfigName(devopsConfigE.get().getName());
        } else {
            projectDefaultConfigDTO.setHarborConfigName(harborConfigs.get(0).getName());
        }
        List<DevopsProjectConfigE> chartConfigs = devopsProjectConfigRepository.baseListByIdAndType(projectId, CHART);
        Optional<DevopsProjectConfigE> chartConfig = chartConfigs.stream().filter(devopsProjectConfigE -> devopsProjectConfigE.getName().equals("chart_default")).findFirst();

        if (chartConfig.isPresent()) {
            projectDefaultConfigDTO.setChartConfigName(chartConfig.get().getName());
        }

        return projectDefaultConfigDTO;
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

        PageInfo<DevopsProjectConfigDTO> devopsProjectConfigDTOPageInfo = PageHelper
                .startPage(pageRequest.getPage(),pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo( () -> devopsProjectConfigMapper.listByOptions(projectId,
                        (Map<String, Object>) mapParams.get(TypeUtil.SEARCH_PARAM),
                        (String) mapParams.get(TypeUtil.PARAM), PageRequestUtil.checkSortIsEmpty(pageRequest)));
        return devopsProjectConfigDTOPageInfo;
    }

    public void baseDelete(Long id) {
        if (devopsProjectConfigMapper.deleteByPrimaryKey(id) != 1) {
            throw new CommonException("error.devops.project.config.delete");
        }
    }

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
}
