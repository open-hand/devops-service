package io.choerodon.devops.app.service.impl;

import com.google.gson.internal.LinkedTreeMap;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.DevopsProjectConfigDTO;
import io.choerodon.devops.api.validator.DevopsProjectConfigValidator;
import io.choerodon.devops.app.service.DevopsProjectConfigService;
import io.choerodon.devops.app.service.ProjectConfigHarborService;
import io.choerodon.devops.domain.application.entity.DevopsProjectConfigE;
import io.choerodon.devops.domain.application.repository.DevopsProjectConfigRepository;
import io.choerodon.devops.infra.common.util.enums.ProjectConfigType;
import io.choerodon.devops.infra.config.ConfigurationProperties;
import io.choerodon.devops.infra.config.RetrofitHandler;
import io.choerodon.devops.infra.feign.HarborClient;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zongw.lee@gmail.com
 * @since 2019/03/11
 */
@Service
public class DevopsProjectConfigServiceImpl implements DevopsProjectConfigService {

    private static final String HARBOR = "harbor";

    @Autowired
    DevopsProjectConfigRepository devopsProjectConfigRepository;

    @Autowired
    DevopsProjectConfigValidator configValidator;

    @Autowired
    ProjectConfigHarborService harborService;

    @Override
    public DevopsProjectConfigDTO create(Long projectId, DevopsProjectConfigDTO devopsProjectConfigDTO) {
        if (devopsProjectConfigDTO.getType().equals(HARBOR) && devopsProjectConfigDTO.getConfig().getProject() != null) {
            checkRegistryProjectIsPrivate(devopsProjectConfigDTO);
        }
        DevopsProjectConfigE devopsProjectConfigE = ConvertHelper.convert(devopsProjectConfigDTO, DevopsProjectConfigE.class);
        devopsProjectConfigE.setProjectId(projectId);
        configValidator.checkConfigType(devopsProjectConfigDTO);
        ProjectConfigType type = ProjectConfigType.valueOf(devopsProjectConfigE.getType().toUpperCase());

        devopsProjectConfigRepository.checkName(projectId, devopsProjectConfigE.getName());
        DevopsProjectConfigE res = devopsProjectConfigRepository.create(devopsProjectConfigE);
        if (type.equals(ProjectConfigType.HARBOR)) {
            harborService.createHarbor(devopsProjectConfigE.getConfig(), devopsProjectConfigE.getProjectId());
        }
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
        Call<Object> listProject = harborClient.listProject(devopsProjectConfigDTO.getConfig().getProject());
        Response<Object> projectResponse = RetrofitHandler.execute(listProject);
        if ("false".equals(((LinkedTreeMap) ((LinkedTreeMap) ((ArrayList) projectResponse.body()).get(0)).get("metadata")).get("public").toString())) {
            devopsProjectConfigDTO.getConfig().setPrivate(true);
        } else {
            devopsProjectConfigDTO.getConfig().setPrivate(false);
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
    public Page<DevopsProjectConfigDTO> listByOptions(Long projectId, PageRequest pageRequest, String params) {
        return ConvertPageHelper.convertPage(devopsProjectConfigRepository.listByOptions(projectId, pageRequest, params), DevopsProjectConfigDTO.class);
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
        return  devopsProjectConfigRepository.checkIsUsed(configId);
    }
}
