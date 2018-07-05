package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.domain.application.entity.ApplicationE;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by younger on 2018/3/28.
 */
public interface ApplicationRepository {
    void checkApp(Long projectId, Long appId);

    void checkName(ApplicationE applicationE);

    void checkCode(ApplicationE applicationE);

    ApplicationE queryByCode(String code, Long projectId);

    int update(ApplicationE applicationE);

    ApplicationE create(ApplicationE applicationE);

    ApplicationE query(Long applicationId);

    Page<ApplicationE> listByOptions(Long projectId,
                                     Boolean isActive,
                                     Boolean hasVersion,
                                     PageRequest pageRequest,
                                     String params);

    Page<ApplicationE> listCodeRepository(Long projectId,
                                     PageRequest pageRequest,
                                     String params);

    Boolean applicationExist(String uuid);

    List<ApplicationE> listByEnvId(Long projectId, Long envId, String status);

    Page<ApplicationE> pageByEnvId(Long projectId, Long envId, PageRequest pageRequest);

    List<ApplicationE> listByActive(Long projectId);

    List<ApplicationE> listAll(Long projectId);

    Page<ApplicationE> listByActiveAndPubAndVersion(Long projectId, Boolean isActive,
                                                    PageRequest pageRequest, String params);

    ApplicationE queryByToken(String token);

    void checkAppCanDisable(Long applicationId);

    List<ApplicationE> listByCode(String code);

    String checkSortIsEmpty(PageRequest pageRequest);
}
