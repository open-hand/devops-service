package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.domain.application.entity.ApplicationTemplateE;
import io.choerodon.devops.infra.feign.IamServiceClient;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by younger on 2018/3/27.
 */
public interface ApplicationTemplateRepository {

    ApplicationTemplateE create(ApplicationTemplateE applicationTemplateE);

    ApplicationTemplateE update(ApplicationTemplateE applicationTemplateE);

    void delete(Long appTemplateId);

    ApplicationTemplateE query(Long appTemplateId);

    Page<ApplicationTemplateE> listByOptions(PageRequest pageRequest, Long organizationId, String searchParam);

    ApplicationTemplateE queryByCode(Long organizationId, String code);

    List<ApplicationTemplateE> list(Long organizationId);

    void checkName(ApplicationTemplateE applicationTemplateE);

    void checkCode(ApplicationTemplateE applicationTemplateE);

    Boolean applicationTemplateExist(String uuid);

    void initMockService(IamServiceClient iamServiceClient);
}
