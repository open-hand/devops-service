package io.choerodon.devops.domain.application.repository;

import java.util.List;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.domain.application.entity.DevopsEnvFileErrorE;

/**
 * Creator: Runge
 * Date: 2018/8/9
 * Time: 20:44create
 * Description:
 */
public interface DevopsEnvFileErrorRepository {

    DevopsEnvFileErrorE createOrUpdate(DevopsEnvFileErrorE devopsEnvFileErrorE);

    List<DevopsEnvFileErrorE> listByEnvId(Long envId);

    PageInfo<DevopsEnvFileErrorE> pageByEnvId(Long envId, PageRequest pageRequest);

    void delete(DevopsEnvFileErrorE devopsEnvFileErrorE);

    DevopsEnvFileErrorE queryByEnvIdAndFilePath(Long envId, String filePath);

    void  create(DevopsEnvFileErrorE devopsEnvFileErrorE);

}
