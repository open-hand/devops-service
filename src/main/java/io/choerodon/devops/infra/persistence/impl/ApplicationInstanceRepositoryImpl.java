package io.choerodon.devops.infra.persistence.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.ApplicationInstanceE;
import io.choerodon.devops.domain.application.repository.ApplicationInstanceRepository;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.dataobject.ApplicationInstanceDO;
import io.choerodon.devops.infra.dataobject.ApplicationInstancesDO;
import io.choerodon.devops.infra.dataobject.DeployDO;
import io.choerodon.devops.infra.mapper.ApplicationInstanceMapper;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by Zenger on 2018/4/12.
 */
@Service
public class ApplicationInstanceRepositoryImpl implements ApplicationInstanceRepository {
    private static final Gson gson = new Gson();

    private ApplicationInstanceMapper applicationInstanceMapper;

    public ApplicationInstanceRepositoryImpl(ApplicationInstanceMapper applicationInstanceMapper) {
        this.applicationInstanceMapper = applicationInstanceMapper;
    }

    @Override
    public Page<ApplicationInstanceE> listApplicationInstance(Long projectId, PageRequest pageRequest,
                                                              Long envId, Long versionId, Long appId, String params) {
        Map maps = gson.fromJson(params, Map.class);
        Map<String, Object> searchParamMap = TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM));
        String paramMap = TypeUtil.cast(maps.get(TypeUtil.PARAM));
        Page<ApplicationInstanceDO> applicationInstanceDOPage = PageHelper.doPageAndSort(pageRequest, () ->
                applicationInstanceMapper
                        .listApplicationInstance(projectId, envId, versionId, appId, searchParamMap, paramMap));
        return ConvertPageHelper.convertPage(applicationInstanceDOPage, ApplicationInstanceE.class);
    }

    @Override
    public ApplicationInstanceE selectByCode(String code, Long envId) {
        ApplicationInstanceDO applicationInstanceDO = new ApplicationInstanceDO();
        applicationInstanceDO.setCode(code);
        applicationInstanceDO.setEnvId(envId);
        return ConvertHelper.convert(
                applicationInstanceMapper.selectOne(applicationInstanceDO),
                ApplicationInstanceE.class);
    }

    @Override
    public ApplicationInstanceE create(ApplicationInstanceE applicationInstanceE) {
        ApplicationInstanceDO applicationInstanceDO =
                ConvertHelper.convert(applicationInstanceE, ApplicationInstanceDO.class);
        if (applicationInstanceMapper.insert(applicationInstanceDO) != 1) {
            throw new CommonException("error.application.instance.create");
        }
        return ConvertHelper.convert(applicationInstanceDO, ApplicationInstanceE.class);
    }

    @Override
    public ApplicationInstanceE selectById(Long id) {
        return ConvertHelper.convert(applicationInstanceMapper.selectByPrimaryKey(id), ApplicationInstanceE.class);
    }

    @Override
    public List<ApplicationInstanceE> listByOptions(Long projectId, Long appId, Long appVersionId, Long envId) {
        return ConvertHelper.convertList(applicationInstanceMapper.listApplicationInstanceCode(
                projectId, envId, appVersionId, appId),
                ApplicationInstanceE.class);
    }

    @Override
    public List<ApplicationInstanceE> listByAppIdAndEnvId(Long projectId, Long appId, Long envId) {
        return ConvertHelper.convertList(applicationInstanceMapper.listRunningAndFailedInstance(
                projectId, envId, appId),
                ApplicationInstanceE.class);
    }

    @Override
    public int checkOptions(Long envId, Long appId, Long appInstanceId) {
        return applicationInstanceMapper.checkOptions(envId, appId, appInstanceId);
    }

    @Override
    public String queryValueByEnvIdAndAppId(Long envId, Long appId) {
        return applicationInstanceMapper.queryValueByEnvIdAndAppId(envId, appId);
    }

    @Override
    public void update(ApplicationInstanceE applicationInstanceE) {
        ApplicationInstanceDO applicationInstanceDO = ConvertHelper.convert(
                applicationInstanceE, ApplicationInstanceDO.class);
        applicationInstanceDO.setObjectVersionNumber(
                applicationInstanceMapper.selectByPrimaryKey(applicationInstanceDO.getId()).getObjectVersionNumber());
        if (applicationInstanceMapper.updateByPrimaryKeySelective(applicationInstanceDO) != 1) {
            throw new CommonException("error.instance.update");
        }
    }

    @Override
    public List<ApplicationInstanceE> selectByEnvId(Long envId) {
        ApplicationInstanceDO applicationInstanceDO = new ApplicationInstanceDO();
        applicationInstanceDO.setEnvId(envId);
        return ConvertHelper.convertList(applicationInstanceMapper
                .select(applicationInstanceDO), ApplicationInstanceE.class);
    }

    @Override
    public List<ApplicationInstancesDO> getDeployInstances(Long projectId, Long appId, List<Long> envIds) {
        if (envIds != null && envIds.isEmpty()) {
            envIds = null;
        }
        return applicationInstanceMapper.listApplicationInstances(projectId, appId, envIds);
    }

    @Override
    public List<ApplicationInstanceE> list() {
        return ConvertHelper.convertList(applicationInstanceMapper.selectAll(), ApplicationInstanceE.class);
    }

    @Override
    public String queryValueByInstanceId(Long instanceId) {
        return applicationInstanceMapper.queryByInstanceId(instanceId);
    }

    @Override
    public void deleteById(Long id) {
        applicationInstanceMapper.deleteByPrimaryKey(id);
    }

    @Override
    public List<DeployDO> listDeployTime(Long projectId, Long envId, Long[] appIds, Date startTime, Date endTime) {
        return applicationInstanceMapper
                .listDeployTime(projectId, envId, appIds, new java.sql.Date(startTime.getTime()),
                        new java.sql.Date(endTime.getTime()));
    }

    @Override
    public List<DeployDO> listDeployFrequency(Long projectId, Long[] envIds, Long appId, Date startTime, Date endTime) {
        return applicationInstanceMapper
                .listDeployFrequency(projectId, envIds, appId, new java.sql.Date(startTime.getTime()),
                        new java.sql.Date(endTime.getTime()));
    }

    @Override
    public Page<DeployDO> pageDeployFrequencyDetail(Long projectId, PageRequest pageRequest, Long[] envIds, Long appId,
                                                    Date startTime, Date endTime) {
        return PageHelper.doPageAndSort(pageRequest, () ->
                applicationInstanceMapper
                        .listDeployFrequency(projectId, envIds, appId, new java.sql.Date(startTime.getTime()),
                                new java.sql.Date(endTime.getTime())));
    }

    @Override
    public Page<DeployDO> pageDeployTimeDetail(Long projectId, PageRequest pageRequest, Long envId, Long[] appIds,
                                               Date startTime, Date endTime) {
        return PageHelper.doPageAndSort(pageRequest, () ->
                applicationInstanceMapper
                        .listDeployTime(projectId, envId, appIds, new java.sql.Date(startTime.getTime()),
                                new java.sql.Date(endTime.getTime())));
    }

    @Override
    public List<ApplicationInstanceE> listByAppId(Long appId) {
        ApplicationInstanceDO applicationInstanceDO = new ApplicationInstanceDO();
        applicationInstanceDO.setAppId(appId);
        return ConvertHelper
                .convertList(applicationInstanceMapper.select(applicationInstanceDO), ApplicationInstanceE.class);
    }

    @Override
    public void deleteAppInstanceByEnvId(Long envId) {
        ApplicationInstanceDO applicationInstanceDO = new ApplicationInstanceDO();
        applicationInstanceDO.setEnvId(envId);
        applicationInstanceMapper.delete(applicationInstanceDO);
    }

    @Override
    public void checkName(String instanceName) {
        ApplicationInstanceDO applicationInstanceDO = new ApplicationInstanceDO();
        applicationInstanceDO.setCode(instanceName);
        if (applicationInstanceMapper.selectOne(applicationInstanceDO) != null) {
            throw new CommonException("error.app.instance.name.already.exist");
        }
    }

    @Override
    public String getInstanceDeploymentDetailJsonByInstanceId(Long instanceId, String deploymentName) {
        return applicationInstanceMapper.getInstanceDeploymentDetailJsonByInstanceId(instanceId, deploymentName);
    }
}
