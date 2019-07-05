package io.choerodon.devops.domain.application.repository;

import java.util.List;

import com.github.pagehelper.PageInfo;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.domain.application.entity.ApplicationE;

/**
 * Created by younger on 2018/3/28.
 */
public interface ApplicationRepository {
    void checkApp(Long projectId, Long appId);

    void checkName(Long projectId, String appName);

    void checkCode(ApplicationE applicationE);

    void checkCode(Long projectId, String code);

    ApplicationE queryByCode(String code, Long projectId);

    ApplicationE queryByCodeWithNullProject(String code);

    int update(ApplicationE applicationE);

    /**
     * 修复创建应用偶发失败 乐观锁问题
     */
    void updateSql(ApplicationE applicationE);

    ApplicationE create(ApplicationE applicationE);

    ApplicationE query(Long applicationId);

    PageInfo<ApplicationE> listByOptions(Long projectId, Boolean isActive, Boolean hasVersion, Boolean appMarket, String type, Boolean doPage,
                                         PageRequest pageRequest, String params);

    PageInfo<ApplicationE> listCodeRepository(Long projectId, PageRequest pageRequest, String params, Boolean isProjectId,
                                          Long userId);

    Boolean applicationExist(String uuid);

    List<ApplicationE> listByEnvId(Long projectId, Long envId, String status);

    PageInfo<ApplicationE> pageByEnvId(Long projectId, Long envId, Long appId, PageRequest pageRequest);

    List<ApplicationE> listByActive(Long projectId);

    List<ApplicationE> listAll(Long projectId);

    PageInfo<ApplicationE> listByActiveAndPubAndVersion(Long projectId, Boolean isActive, PageRequest pageRequest,
                                                    String params);

    ApplicationE queryByToken(String token);

    void checkAppCanDisable(Long applicationId);

    List<ApplicationE> listByCode(String code);

    List<ApplicationE> listByGitLabProjectIds(List<Long> gitLabProjectIds);

    void delete(Long appId);

    List<ApplicationE> listByProjectIdAndSkipCheck(Long projectId);

    List<ApplicationE> listByProjectId(Long projectId);

    void updateAppHarborConfig(Long projectId, Long newConfigId, Long oldConfigId, boolean harborPrivate);
}
