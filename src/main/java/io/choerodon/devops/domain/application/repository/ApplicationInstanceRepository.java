package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.domain.application.entity.ApplicationInstanceE;
import io.choerodon.devops.infra.dataobject.ApplicationInstancesDO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by Zenger on 2018/4/12.
 */
public interface ApplicationInstanceRepository {

    Page<ApplicationInstanceE> listApplicationInstance(Long projectId, PageRequest pageRequest,
                                                       Long envId, Long versionId, Long appId, String params);

    ApplicationInstanceE selectByCode(String code, Long envId);

    ApplicationInstanceE create(ApplicationInstanceE applicationInstanceE);

    ApplicationInstanceE selectById(Long id);

    List<ApplicationInstanceE> listByOptions(Long projectId, Long appId, Long appVersionId, Long envId);

    int checkOptions(Long envId, Long appId, Long appVersionId, Long appInstanceId);

    String queryValueByEnvIdAndAppId(Long envId, Long appId);

    void update(ApplicationInstanceE applicationInstanceE);

    int selectByEnvId(Long envId);

    List<ApplicationInstancesDO> getDeployInstances(Long projectId, Long appId);
}
