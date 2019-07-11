package io.choerodon.devops.infra.persistence.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.iam.entity.ApplicationTemplateE;
import io.choerodon.devops.domain.application.repository.ApplicationTemplateRepository;
import io.choerodon.devops.infra.util.PageRequestUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.devops.infra.dto.ApplicationTemplateDO;
import io.choerodon.devops.infra.dto.ApplicationVersionDO;
import io.choerodon.devops.infra.dto.iam.OrganizationDO;
import io.choerodon.devops.infra.feign.IamServiceClient;
import io.choerodon.devops.infra.mapper.ApplicationTemplateMapper;
import io.kubernetes.client.JSON;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Created by younger on 2018/3/27.
 */
@Service
public class ApplicationTemplateRepositoryImpl implements ApplicationTemplateRepository {

    private static ObjectMapper mapper = new ObjectMapper();
    private static JSON json = new JSON();
    private String[] models = new String[]{"microservice", "microserviceui", "javalib"};
    @Value("${services.gitlab.url}")
    private String gitlabUrl;

    private IamServiceClient iamServiceClient;
    private ApplicationTemplateMapper applicationTemplateMapper;

    public ApplicationTemplateRepositoryImpl(ApplicationTemplateMapper applicationTemplateMapper, IamServiceClient iamServiceClient) {
        this.applicationTemplateMapper = applicationTemplateMapper;
        this.iamServiceClient = iamServiceClient;
    }

    @Override
    public ApplicationTemplateE create(ApplicationTemplateE applicationTemplateE) {
        ApplicationTemplateDO applicationTemplateDO = ConvertHelper.convert(applicationTemplateE,
                ApplicationTemplateDO.class);
        ResponseEntity<OrganizationDO> organizationDO =
                iamServiceClient.queryOrganizationById(applicationTemplateDO.getOrganizationId());
        applicationTemplateDO.setRepoUrl(
                    organizationDO.getBody().getCode() + "_template" + "/"
                            + applicationTemplateE.getCode() + ".git");

        if (applicationTemplateMapper.insert(applicationTemplateDO) != 1) {
            throw new CommonException("error.insert.appTemplate");
        }
        return ConvertHelper.convert(applicationTemplateDO, ApplicationTemplateE.class);
    }

    @Override
    public ApplicationTemplateE update(ApplicationTemplateE applicationTemplateE) {
        ApplicationTemplateDO newApplicationTemplateDO = ConvertHelper.convert(applicationTemplateE,
                ApplicationTemplateDO.class);
        if (applicationTemplateE.getObjectVersionNumber() == null) {
            ApplicationTemplateDO applicationTemplateDO = applicationTemplateMapper.selectByPrimaryKey(
                    applicationTemplateE.getId());
            newApplicationTemplateDO.setObjectVersionNumber(applicationTemplateDO.getObjectVersionNumber());
        }
        if (applicationTemplateMapper.updateByPrimaryKeySelective(newApplicationTemplateDO) != 1) {
            throw new CommonException("error.update.appTemplate");
        }
        return ConvertHelper.convert(newApplicationTemplateDO, ApplicationTemplateE.class);
    }

    @Override
    public void delete(Long appTemplateId) {
        applicationTemplateMapper.deleteByPrimaryKey(appTemplateId);
    }

    @Override
    public ApplicationTemplateE query(Long appTemplateId) {
        return ConvertHelper.convert(applicationTemplateMapper.selectByPrimaryKey(appTemplateId),
                ApplicationTemplateE.class);
    }

    @Override
    public PageInfo<ApplicationTemplateE> listByOptions(PageRequest pageRequest, Long organizationId, String params) {
        PageInfo<ApplicationVersionDO> applicationTemplateDOS;
        if (!StringUtils.isEmpty(params)) {
            Map<String, Object> maps = json.deserialize(params, Map.class);
            if (maps.get(TypeUtil.SEARCH_PARAM).equals("")) {
                applicationTemplateDOS = PageHelper.startPage(
                        pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() -> applicationTemplateMapper.list(
                        organizationId,
                        null,
                        TypeUtil.cast(maps.get(TypeUtil.PARAM))));
            } else {
                applicationTemplateDOS = PageHelper.startPage(
                        pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() -> applicationTemplateMapper.list(
                        organizationId,
                        TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM)),
                        TypeUtil.cast(maps.get(TypeUtil.PARAM))));
            }
        } else {
            applicationTemplateDOS = PageHelper.startPage(
                    pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() -> applicationTemplateMapper.list(organizationId, null, null));
        }
        return ConvertPageHelper.convertPageInfo(applicationTemplateDOS, ApplicationTemplateE.class);
    }

    @Override
    public ApplicationTemplateE queryByCode(Long organizationId, String code) {
        ApplicationTemplateDO applicationTemplate = applicationTemplateMapper.queryByCode(organizationId, code);
        return ConvertHelper.convert(applicationTemplate, ApplicationTemplateE.class);
    }

    @Override
    public List<ApplicationTemplateE> list(Long organizationId) {
        return ConvertHelper.convertList(applicationTemplateMapper.list(
                organizationId, null, null), ApplicationTemplateE.class);
    }

    @Override
    public void checkName(ApplicationTemplateE applicationTemplateE) {
        ApplicationTemplateDO applicationTemplateDO = new ApplicationTemplateDO();
        applicationTemplateDO.setOrganizationId(applicationTemplateE.getOrganization().getId());
        applicationTemplateDO.setName(applicationTemplateE.getName());
        if (Arrays.asList(models).contains(applicationTemplateE.getName().toLowerCase())) {
            throw new CommonException("error.name.exist");
        }
        if (applicationTemplateMapper.selectOne(applicationTemplateDO) != null) {
            throw new CommonException("error.name.exist");
        }
    }

    @Override
    public void checkCode(ApplicationTemplateE applicationTemplateE) {
        ApplicationTemplateDO applicationTemplateDO = new ApplicationTemplateDO();
        applicationTemplateDO.setOrganizationId(applicationTemplateE.getOrganization().getId());
        applicationTemplateDO.setCode(applicationTemplateE.getCode());
        if (Arrays.asList(models).contains(applicationTemplateE.getCode().toLowerCase())) {
            throw new CommonException("error.code.exist");
        }
        if (!applicationTemplateMapper.select(applicationTemplateDO).isEmpty()) {
            throw new CommonException("error.code.exist");
        }
    }

    @Override
    public Boolean applicationTemplateExist(String uuid) {
        ApplicationTemplateDO applicationTemplateDO = new ApplicationTemplateDO();
        applicationTemplateDO.setUuid(uuid);
        return !applicationTemplateMapper.select(applicationTemplateDO).isEmpty();
    }

    @Override
    public void initMockService(IamServiceClient iamServiceClient) {
        this.iamServiceClient = iamServiceClient;
    }
}
