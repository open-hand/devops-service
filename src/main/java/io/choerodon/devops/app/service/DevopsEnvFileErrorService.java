package io.choerodon.devops.app.service;

import java.util.List;

import com.github.pagehelper.PageInfo;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.DevopsEnvFileErrorVO;
import io.choerodon.devops.infra.dto.DevopsEnvFileErrorDTO;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  11:53 2019/7/12
 * Description:
 */
public interface DevopsEnvFileErrorService {
    DevopsEnvFileErrorDTO baseCreateOrUpdate(DevopsEnvFileErrorVO devopsEnvFileErrorVO);

    List<DevopsEnvFileErrorDTO> baseListByEnvId(Long envId);

    PageInfo<DevopsEnvFileErrorDTO> basePageByEnvId(Long envId, PageRequest pageRequest);

    void baseDelete(DevopsEnvFileErrorVO devopsEnvFileErrorVO);

    DevopsEnvFileErrorDTO baseQueryByEnvIdAndFilePath(Long envId, String filePath);

    void baseCreate(DevopsEnvFileErrorVO devopsEnvFileErrorE);
}
