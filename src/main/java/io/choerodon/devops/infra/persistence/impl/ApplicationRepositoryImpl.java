package io.choerodon.devops.infra.persistence.impl;

import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.ApplicationE;
import io.choerodon.devops.domain.application.repository.ApplicationRepository;
import io.choerodon.devops.domain.application.repository.IamRepository;
import io.choerodon.devops.infra.common.util.PageRequestUtil;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.dataobject.ApplicationDO;
import io.choerodon.devops.infra.mapper.ApplicationMapper;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.kubernetes.client.JSON;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by younger on 2018/3/28.
 */
@Service
public class ApplicationRepositoryImpl implements ApplicationRepository {

    private Gson gson = new Gson();
    private JSON json = new JSON();

    @Autowired
    private IamRepository iamRepository;
    private ApplicationMapper applicationMapper;

    public ApplicationRepositoryImpl(ApplicationMapper applicationMapper) {
        this.applicationMapper = applicationMapper;
    }

    @Override
    public void checkApp(Long projectId, Long appId) {
        ApplicationDO applicationDO = applicationMapper.selectByPrimaryKey(appId);
        if (applicationDO == null || !applicationDO.getProjectId().equals(projectId)) {
            throw new CommonException("error.app.project.notMatch");
        }
    }

    @Override
    public void checkName(Long projectId, String appName) {
        ApplicationDO applicationDO = new ApplicationDO();
        applicationDO.setProjectId(projectId);
        applicationDO.setName(appName);
        if (applicationMapper.selectOne(applicationDO) != null) {
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
    public void checkCode(Long projectId, String code) {
        ApplicationDO applicationDO = new ApplicationDO();
        applicationDO.setProjectId(projectId);
        applicationDO.setCode(code);
        if (applicationMapper.selectOne(applicationDO) != null) {
            throw new CommonException("error.code.exist");
        }
    }

    @Override
    public ApplicationE create(ApplicationE applicationE) {
        ApplicationDO applicationDO = ConvertHelper.convert(applicationE, ApplicationDO.class);
        applicationMapper.insert(applicationDO);
        return ConvertHelper.convert(applicationDO, ApplicationE.class);
    }


    @Override
    public int update(ApplicationE applicationE) {
        ApplicationDO applicationDO = applicationMapper.selectByPrimaryKey(applicationE.getId());
        ApplicationDO newApplicationDO = ConvertHelper.convert(applicationE, ApplicationDO.class);
        if (applicationE.getFailed() != null && !applicationE.getFailed()) {
            applicationMapper.updateAppToSuccess(applicationDO.getId());
        }
        newApplicationDO.setObjectVersionNumber(applicationDO.getObjectVersionNumber());
        return applicationMapper.updateByPrimaryKeySelective(newApplicationDO);
    }

    @Override
    public ApplicationE query(Long applicationId) {
        ApplicationDO applicationDO = applicationMapper.selectByPrimaryKey(applicationId);
        return ConvertHelper.convert(applicationDO, ApplicationE.class);
    }

    @Override
    public Page<ApplicationE> listByOptions(Long projectId, Boolean isActive, Boolean hasVersion,
                                            String type, Boolean doPage, PageRequest pageRequest, String params) {
        Page<ApplicationDO> applicationES = new Page<>();

        Map<String, Object> mapParams = TypeUtil.castMapParams(params);
        //是否需要分页
        if (doPage != null && !doPage) {
            applicationES.setContent(applicationMapper.list(projectId, isActive, hasVersion, type,
                    (Map<String, Object>) mapParams.get(TypeUtil.SEARCH_PARAM),
                    mapParams.get(TypeUtil.PARAM).toString(), PageRequestUtil.checkSortIsEmpty(pageRequest)));
        } else {
            applicationES = PageHelper
                    .doPageAndSort(pageRequest, () -> applicationMapper.list(projectId, isActive, hasVersion, type,
                            (Map<String, Object>) mapParams.get(TypeUtil.SEARCH_PARAM),
                            (String) mapParams.get(TypeUtil.PARAM), PageRequestUtil.checkSortIsEmpty(pageRequest)));
        }
        return ConvertPageHelper.convertPage(applicationES, ApplicationE.class);
    }

    @Override
    public Page<ApplicationE> listCodeRepository(Long projectId, PageRequest pageRequest, String params,
                                                 Boolean isProjectOwner, Long userId) {
        Page<ApplicationDO> applicationES;
        Map maps = gson.fromJson(params, Map.class);
        applicationES = PageHelper.doPageAndSort(pageRequest, () -> applicationMapper.listCodeRepository(projectId,
                TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM)),
                TypeUtil.cast(maps.get(TypeUtil.PARAM)), isProjectOwner, userId));
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
        return ConvertHelper.convert(applicationMapper.selectOne(applicationDO), ApplicationE.class);
    }

    @Override
    public List<ApplicationE> listByEnvId(Long projectId, Long envId, String status) {
        return ConvertHelper.convertList(applicationMapper.listByEnvId(projectId, envId, null, status), ApplicationE.class);
    }

    @Override
    public Page<ApplicationE> pageByEnvId(Long projectId, Long envId, Long appId, PageRequest pageRequest) {
        return ConvertPageHelper.convertPage(
                PageHelper.doPageAndSort(
                        pageRequest, () -> applicationMapper.listByEnvId(projectId, envId, appId, "nodeleted")),
                ApplicationE.class
        );
    }

    @Override
    public List<ApplicationE> listByActive(Long projectId) {
        return ConvertHelper.convertList(applicationMapper.listActive(projectId), ApplicationE.class);
    }

    @Override
    public List<ApplicationE> listAll(Long projectId) {
        return ConvertHelper.convertList(applicationMapper.listAll(projectId), ApplicationE.class);
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

    @Override
    public List<ApplicationE> listByCode(String code) {
        return ConvertHelper.convertList(applicationMapper.listByCode(code), ApplicationE.class);
    }

    @Override
    public List<ApplicationE> listByGitLabProjectIds(List<Long> gitLabProjectIds) {
        return ConvertHelper
                .convertList(applicationMapper.listByGitLabProjectIds(gitLabProjectIds), ApplicationE.class);
    }

    @Override
    public void delete(Long appId) {
        applicationMapper.deleteByPrimaryKey(appId);
    }

    @Override
    public List<ApplicationE> listByProjectIdAndSkipCheck(Long projectId) {
        ApplicationDO applicationDO = new ApplicationDO();
        applicationDO.setProjectId(projectId);
        applicationDO.setIsSkipCheckPermission(true);
        return ConvertHelper.convertList(applicationMapper.select(applicationDO), ApplicationE.class);
    }

    @Override
    public List<ApplicationE> listByProjectId(Long projectId) {
        ApplicationDO applicationDO = new ApplicationDO();
        applicationDO.setProjectId(projectId);
        return ConvertHelper.convertList(applicationMapper.select(applicationDO), ApplicationE.class);
    }
}
