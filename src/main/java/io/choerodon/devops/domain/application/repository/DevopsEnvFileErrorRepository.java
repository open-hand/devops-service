package io.choerodon.devops.domain.application.repository;

import java.util.List;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvFileErrorE;

/**
 * Creator: Runge
 * Date: 2018/8/9
 * Time: 20:44create
 * Description:
 */
public interface DevopsEnvFileErrorRepository {

    DevopsEnvFileErrorE baseCreateOrUpdate(DevopsEnvFileErrorE devopsEnvFileErrorE);

    List<DevopsEnvFileErrorE> baseListByEnvId(Long envId);

    PageInfo<DevopsEnvFileErrorE> basePageByEnvId(Long envId, PageRequest pageRequest);

    void baseDelete(DevopsEnvFileErrorE devopsEnvFileErrorE);

    DevopsEnvFileErrorE baseQueryByEnvIdAndFilePath(Long envId, String filePath);

    void baseCreate(DevopsEnvFileErrorE devopsEnvFileErrorE);

}
