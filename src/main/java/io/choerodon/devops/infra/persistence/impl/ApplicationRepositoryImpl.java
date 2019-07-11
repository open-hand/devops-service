package io.choerodon.devops.infra.persistence.impl;

import java.util.List;
import java.util.Map;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import io.kubernetes.client.JSON;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.iam.entity.ApplicationE;
import io.choerodon.devops.domain.application.repository.ApplicationRepository;
import io.choerodon.devops.infra.util.PageRequestUtil;
import io.choerodon.devops.infra.util.TypeUtil;
<<<<<<< HEAD

import io.choerodon.devops.infra.dataobject.ApplicationDTO;

=======
import io.choerodon.devops.infra.dataobject.ApplicationDTO;
>>>>>>> [IMP] 修改AppControler重构
import io.choerodon.devops.infra.mapper.ApplicationMapper;

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
        ApplicationDTO applicationDO = applicationMapper.selectByPrimaryKey(appId);
        if (applicationDO == null || !applicationDO.getProjectId().equals(projectId)) {
            throw new CommonException("error.app.project.notMatch");
        }
    }

    @Override
    public void checkName(Long projectId, String appName) {
        ApplicationDTO applicationDO = new ApplicationDTO();
        applicationDO.setProjectId(projectId);
        applicationDO.setName(appName);
        if (applicationMapper.selectOne(applicationDO) != null) {
            throw new CommonException("error.name.exist");
        }
    }

    @Override
    public void checkCode(ApplicationE applicationE) {
        ApplicationDTO applicationDO = new ApplicationDTO();
        applicationDO.setProjectId(applicationE.getProjectE().getId());
        applicationDO.setCode(applicationE.getCode());
        if (!applicationMapper.select(applicationDO).isEmpty()) {
            throw new CommonException("error.code.exist");
        }
    }

    @Override
    public void checkCode(Long projectId, String code) {
        ApplicationDTO applicationDO = new ApplicationDTO();
        applicationDO.setProjectId(projectId);
        applicationDO.setCode(code);
        if (applicationMapper.selectOne(applicationDO) != null) {
            throw new CommonException("error.code.exist");
        }
    }

    @Override
    public ApplicationE create(ApplicationE applicationE) {
        ApplicationDTO applicationDO = ConvertHelper.convert(applicationE, ApplicationDTO.class);
        if (applicationMapper.insert(applicationDO) != 1) {
            throw new CommonException("error.application.create.insert");
        }
        return ConvertHelper.convert(applicationDO, ApplicationE.class);
    }


    @Override
    public int update(ApplicationE applicationE) {
        ApplicationDTO applicationDO = applicationMapper.selectByPrimaryKey(applicationE.getId());
        ApplicationDTO newApplicationDO = ConvertHelper.convert(applicationE, ApplicationDTO.class);
        if (applicationE.getFailed() != null && !applicationE.getFailed()) {
            applicationMapper.updateAppToSuccess(applicationDO.getId());
        }
        newApplicationDO.setObjectVersionNumber(applicationDO.getObjectVersionNumber());
        return applicationMapper.updateByPrimaryKeySelective(newApplicationDO);
    }

    @Override
    public void updateSql(ApplicationE applicationE) {
        ApplicationDTO applicationDO = ConvertHelper.convert(applicationE, ApplicationDTO.class);
        applicationMapper.updateSql(applicationDO.getId(), applicationDO.getToken(),
                applicationDO.getGitlabProjectId(), applicationDO.getHookId(), applicationDO.getSynchro());
    }

    @Override
    public ApplicationE query(Long applicationId) {
        ApplicationDTO applicationDO = applicationMapper.selectByPrimaryKey(applicationId);
        return ConvertHelper.convert(applicationDO, ApplicationE.class);
    }

    @Override
    public PageInfo<ApplicationE> listByOptions(Long projectId, Boolean isActive, Boolean hasVersion, Boolean appMarket,
                                                String type, Boolean doPage, PageRequest pageRequest, String params) {
        PageInfo<ApplicationDTO> applicationES = new PageInfo<>();

        Map<String, Object> mapParams = TypeUtil.castMapParams(params);
        //是否需要分页
        if (doPage != null && !doPage) {
            applicationES.setList(applicationMapper.list(projectId, isActive, hasVersion, appMarket, type,
                    (Map<String, Object>) mapParams.get(TypeUtil.SEARCH_PARAM),
                    mapParams.get(TypeUtil.PARAM).toString(), PageRequestUtil.checkSortIsEmpty(pageRequest)));
        } else {
            applicationES = PageHelper
                    .startPage(pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() -> applicationMapper.list(projectId, isActive, hasVersion, appMarket, type,
                            (Map<String, Object>) mapParams.get(TypeUtil.SEARCH_PARAM),
                            (String) mapParams.get(TypeUtil.PARAM), PageRequestUtil.checkSortIsEmpty(pageRequest)));
        }
        return ConvertPageHelper.convertPageInfo(applicationES, ApplicationE.class);
    }

    @Override
    public PageInfo<ApplicationE> listCodeRepository(Long projectId, PageRequest pageRequest, String params,
                                                     Boolean isProjectOwner, Long userId) {
        PageInfo<ApplicationDTO> applicationES;
        Map maps = gson.fromJson(params, Map.class);
        applicationES = PageHelper.startPage(pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() -> applicationMapper.listCodeRepository(projectId,
                TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM)),
                TypeUtil.cast(maps.get(TypeUtil.PARAM)), isProjectOwner, userId));
        return ConvertPageHelper.convertPageInfo(applicationES, ApplicationE.class);
    }

    @Override
    public Boolean applicationExist(String uuid) {
        ApplicationDTO applicationDO = new ApplicationDTO();
        applicationDO.setUuid(uuid);
        return !applicationMapper.select(applicationDO).isEmpty();
    }

    @Override
    public ApplicationE queryByCode(String code, Long projectId) {
        ApplicationDTO applicationDO = new ApplicationDTO();
        applicationDO.setProjectId(projectId);
        applicationDO.setCode(code);
        return ConvertHelper.convert(applicationMapper.selectOne(applicationDO), ApplicationE.class);
    }

    @Override
    public ApplicationE queryByCodeWithNullProject(String code) {
        return ConvertHelper.convert(applicationMapper.queryByCodeWithNoProject(code), ApplicationE.class);
    }

    @Override
    public List<ApplicationE> listByEnvId(Long projectId, Long envId, String status) {
        return ConvertHelper.convertList(applicationMapper.listByEnvId(projectId, envId, null, status), ApplicationE.class);
    }

    @Override
    public PageInfo<ApplicationE> pageByEnvId(Long projectId, Long envId, Long appId, PageRequest pageRequest) {
        return ConvertPageHelper.convertPageInfo(
                PageHelper.startPage(pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() -> applicationMapper.listByEnvId(projectId, envId, appId, "nodeleted")),
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
    public PageInfo<ApplicationE> listByActiveAndPubAndVersion(Long projectId, Boolean isActive,
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
        return ConvertPageHelper.convertPageInfo(PageHelper.startPage(pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() -> applicationMapper
                .listByActiveAndPubAndVersion(projectId, isActive, finalSearchParam, finalParam)), ApplicationE.class);
    }

    @Override
    public ApplicationE queryByToken(String token) {
        ApplicationDTO applicationDO = applicationMapper.queryByToken(token);
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
        ApplicationDTO applicationDO = new ApplicationDTO();
        applicationDO.setProjectId(projectId);
        applicationDO.setIsSkipCheckPermission(true);
        return ConvertHelper.convertList(applicationMapper.select(applicationDO), ApplicationE.class);
    }

    @Override
    public List<ApplicationE> listByProjectId(Long projectId) {
        ApplicationDTO applicationDO = new ApplicationDTO();
        applicationDO.setProjectId(projectId);
        return ConvertHelper.convertList(applicationMapper.select(applicationDO), ApplicationE.class);
    }

    @Override
    public void updateAppHarborConfig(Long projectId, Long newConfigId, Long oldConfigId, boolean harborPrivate) {
        applicationMapper.updateAppHarborConfig(projectId, newConfigId, oldConfigId, harborPrivate);
    }
}
