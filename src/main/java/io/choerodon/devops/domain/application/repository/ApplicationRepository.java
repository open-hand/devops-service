package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.domain.application.entity.ApplicationE;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by younger on 2018/3/28.
 */
public interface ApplicationRepository {
    void checkName(ApplicationE applicationE);

    void checkCode(ApplicationE applicationE);

    ApplicationE queryByCode(String code, Long projectId);

    int update(ApplicationE applicationE);

    int create(ApplicationE applicationE);

    ApplicationE query(Long applicationId);

    Page<ApplicationE> listByOptions(Long projectId,
                                     PageRequest pageRequest,
                                     String params);

    Boolean applicationExist(String uuid);

    List<ApplicationE> listByEnvId(Long projectId, Long envId, String status);

    Page<ApplicationE> pageByEnvId(Long projectId, Long envId, PageRequest pageRequest);

    List<ApplicationE> listByActive(Long projectId);

    List<ApplicationE> listByActiveAndPubAndVersion(Long projectId, Boolean isActive);

    ApplicationE queryByToken(String token);
}
