package io.choerodon.devops.infra.persistence.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.kubernetes.client.JSON;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.ApplicationE;
import io.choerodon.devops.domain.application.entity.ProjectE;
import io.choerodon.devops.domain.application.repository.ApplicationRepository;
import io.choerodon.devops.domain.application.repository.IamRepository;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.dataobject.ApplicationDO;
import io.choerodon.devops.infra.mapper.ApplicationMapper;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by younger on 2018/3/28.
 */
@Service
public class ApplicationRepositoryImpl implements ApplicationRepository {

    private JSON json = new JSON();

    @Autowired
    private IamRepository iamRepository;
    private ApplicationMapper applicationMapper;

    public ApplicationRepositoryImpl(ApplicationMapper applicationMapper) {
        this.applicationMapper = applicationMapper;
    }

    @Override
    public void checkName(ApplicationE applicationE) {
        ApplicationDO applicationDO = new ApplicationDO();
        applicationDO.setProjectId(applicationE.getProjectE().getId());
        applicationDO.setName(applicationE.getName());
        if (!applicationMapper.select(applicationDO).isEmpty()) {
            throw new CommonException("error.name.exist");
        }
    }

    @Override
    public void checkCode(ApplicationE applicationE) {
        ApplicationDO applicationDO = new ApplicationDO();
        applicationDO.setProjectId(applicationE.getProjectE().getId());
        applicationDO.setCode(applicationE.getCode());
        if (!applicationMapper.select(applicationDO).isEmpty()) {
            throw new CommonException("error.code.exist");
        }
    }

    @Override
    public int create(ApplicationE applicationE) {
        return applicationMapper.insert(ConvertHelper.convert(applicationE, ApplicationDO.class));
    }


    @Override
    public int update(ApplicationE applicationE) {
        ApplicationDO applicationDO = applicationMapper.selectByPrimaryKey(applicationE.getId());
        ApplicationDO newApplicationDO = ConvertHelper.convert(applicationE, ApplicationDO.class);
        newApplicationDO.setObjectVersionNumber(applicationDO.getObjectVersionNumber());
        return applicationMapper.updateByPrimaryKeySelective(newApplicationDO);
    }

    @Override
    public ApplicationE query(Long applicationId) {
        ApplicationDO applicationDO = applicationMapper.selectByPrimaryKey(applicationId);
        return ConvertHelper.convert(applicationDO, ApplicationE.class);
    }

    @Override
    public Page<ApplicationE> listByOptions(Long projectId, Boolean isActive, PageRequest pageRequest, String params) {
        Page<ApplicationDO> applicationES;
        if (!StringUtils.isEmpty(params)) {
            Map<String, Object> maps = json.deserialize(params, Map.class);
            if (maps.get(TypeUtil.SEARCH_PARAM).equals("")) {
                applicationES = PageHelper.doPageAndSort(
                        pageRequest, () -> applicationMapper.list(
                                projectId, isActive, null,
                                TypeUtil.cast(maps.get(TypeUtil.PARAM))));
            } else {
                applicationES = PageHelper.doPageAndSort(
                        pageRequest, () -> applicationMapper.list(
                                projectId, isActive, TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM)),
                                TypeUtil.cast(maps.get(TypeUtil.PARAM))));
            }
        } else {
            applicationES = PageHelper.doPageAndSort(
                    pageRequest, () -> applicationMapper.list(projectId, isActive, null, null));
        }
        return ConvertPageHelper.convertPage(applicationES, ApplicationE.class);
    }

    @Override
    public Boolean applicationExist(String uuid) {
        ApplicationDO applicationDO = new ApplicationDO();
        applicationDO.setUuid(uuid);
        return !applicationMapper.select(applicationDO).isEmpty();
    }

    @Override
    public ApplicationE queryByCode(String code, Long projectId) {
        ApplicationDO applicationDO = new ApplicationDO();
        applicationDO.setProjectId(projectId);
        applicationDO.setCode(code);
        List<ApplicationDO> applicationDOS = applicationMapper.select(applicationDO);
        if (!applicationDOS.isEmpty()) {
            return ConvertHelper.convert(applicationDOS.get(0), ApplicationE.class);
        } else {
            throw new CommonException("error.application.get");
        }
    }

    @Override
    public List<ApplicationE> listByEnvId(Long projectId, Long envId, String status) {
        return ConvertHelper.convertList(applicationMapper.listByEnvId(projectId, envId, status), ApplicationE.class);
    }

    @Override
    public Page<ApplicationE> pageByEnvId(Long projectId, Long envId, PageRequest pageRequest) {
        return ConvertPageHelper.convertPage(
                PageHelper.doPageAndSort(
                        pageRequest, () -> applicationMapper.listByEnvId(projectId, envId, "nodeleted")),
                ApplicationE.class
        );
    }

    @Override
    public List<ApplicationE> listByActive(Long projectId) {
        return ConvertHelper.convertList(applicationMapper.listActive(projectId), ApplicationE.class);
    }

    @Override
    public List<ApplicationE> listAll(Long projectId) {
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        Long organizationId = projectE.getOrganization().getId();
        List<ProjectE> projectEList = iamRepository.listIamProjectByOrgId(organizationId);
        List<Long> projectIds = projectEList.parallelStream().map(ProjectE::getId)
                .collect(Collectors.toCollection(ArrayList::new));

        return ConvertHelper.convertList(applicationMapper.listAll(projectId, projectIds), ApplicationE.class);
    }

    @Override
    public Page<ApplicationE> listByActiveAndPubAndVersion(Long projectId, Boolean isActive,
                                                           PageRequest pageRequest, String params) {
        Map<String, Object> searchParam = null;
        String param = null;
        if (!StringUtils.isEmpty(params)) {
            Map<String, Object> searchParamMap = json.deserialize(params, Map.class);
            searchParam = TypeUtil.cast(searchParamMap.get(TypeUtil.SEARCH_PARAM));
            param = TypeUtil.cast(searchParamMap.get(TypeUtil.PARAM));
        }
        Map<String, Object> finalSearchParam = searchParam;
        String finalParam = param;
        return ConvertPageHelper.convertPage(PageHelper.doPageAndSort(pageRequest, () -> applicationMapper
                .listByActiveAndPubAndVersion(projectId, isActive, finalSearchParam, finalParam)), ApplicationE.class);
    }

    @Override
    public ApplicationE queryByToken(String token) {
        ApplicationDO applicationDO = applicationMapper.queryByToken(token);
        return ConvertHelper.convert(applicationDO, ApplicationE.class);
    }

    @Override
    public void checkAppCanDisable(Long applicationId) {
        if (applicationMapper.checkAppCanDisable(applicationId) == 0) {
            throw new CommonException("error.app.publishedOrDeployed");
        }
    }
}
