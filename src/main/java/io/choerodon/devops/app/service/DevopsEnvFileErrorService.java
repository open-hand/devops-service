package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.DevopsEnvFileErrorVO;
import io.choerodon.devops.infra.dto.DevopsEnvFileErrorDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  11:53 2019/7/12
 * Description:
 */
public interface DevopsEnvFileErrorService {
    DevopsEnvFileErrorDTO baseCreateOrUpdate(DevopsEnvFileErrorDTO devopsEnvFileErrorDTO);

    List<DevopsEnvFileErrorDTO> baseListByEnvId(Long envId);

    Page<DevopsEnvFileErrorDTO> basePageByEnvId(Long envId, PageRequest pageable);

    void baseDelete(DevopsEnvFileErrorDTO devopsEnvFileErrorDTO);

    DevopsEnvFileErrorDTO baseQueryByEnvIdAndFilePath(Long envId, String filePath);

    void baseCreate(DevopsEnvFileErrorVO devopsEnvFileErrorE);

    /**
     * 根据环境id删除相应的纪录
     * 删除环境时使用
     *
     * @param envId 环境id
     */
    void deleteByEnvId(Long envId);
}
