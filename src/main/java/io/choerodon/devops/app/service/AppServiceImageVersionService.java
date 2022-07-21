package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Set;

import io.choerodon.devops.api.vo.appversion.AppServiceImageVersionVO;
import io.choerodon.devops.infra.dto.AppServiceImageVersionDTO;

/**
 * 应用版本表(AppServiceImageVersion)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2022-07-13 16:47:42
 */
public interface AppServiceImageVersionService {

    void create(AppServiceImageVersionDTO appServiceImageVersionDTO);

    AppServiceImageVersionDTO queryByAppServiceVersionId(Long appServiceVersionId);

    List<AppServiceImageVersionVO> listByAppVersionIds(Set<Long> versionIds);

    void baseUpdate(AppServiceImageVersionDTO appServiceImageVersionDTO);

    void deleteByAppServiceVersionId(Long appServiceVersionId);

    /**
     * 2.2版本迁移原应服务版本数据使用，后续可删除
     *
     * @param appServiceImageVersionDTOS
     */
    void batchInsertInNewTrans(List<AppServiceImageVersionDTO> appServiceImageVersionDTOS);
}

