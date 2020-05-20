package io.choerodon.devops.app.service;

import java.util.List;

import com.github.pagehelper.PageInfo;

import org.springframework.data.domain.Pageable;
import io.choerodon.devops.api.vo.DevopsEnvFileErrorVO;
import io.choerodon.devops.infra.dto.DevopsEnvFileErrorDTO;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  11:53 2019/7/12
 * Description:
 */
public interface DevopsEnvFileErrorService {
    DevopsEnvFileErrorDTO baseCreateOrUpdate(DevopsEnvFileErrorDTO devopsEnvFileErrorDTO);

    List<DevopsEnvFileErrorDTO> baseListByEnvId(Long envId);

    PageInfo<DevopsEnvFileErrorDTO> basePageByEnvId(Long envId, Pageable pageable);

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
