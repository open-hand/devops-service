package io.choerodon.devops.domain.application.repository;

import com.github.pagehelper.PageInfo;

import java.util.List;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.DevopsServiceVO;
import io.choerodon.devops.infra.dto.DevopsServiceDTO;

/**
 * Created by Zenger on 2018/4/13.
 */
public interface DevopsServiceRepository {

    DevopsServiceDTO baseCreate(DevopsServiceDTO devopsServiceDTO);

    DevopsServiceDTO baseQuery(Long id);

    void baseDelete(Long id);

    void baseUpdate(DevopsServiceDTO devopsServiceDTO);

    Boolean baseCheckName(Long envId, String name);


    PageInfo<DevopsServiceVO> basePageByOptions(Long projectId, Long envId, Long instanceId,
                                                PageRequest pageRequest, String searchParam, Long appId);

    List<DevopsServiceVO> baseListByEnvId(Long envId);

    DevopsServiceVO baseQueryById(Long id);

    List<Long> baseListEnvByRunningService();

    DevopsServiceDTO baseQueryByNameAndEnvId(String name, Long envId);

    Boolean baseCheckServiceByEnv(Long envId);

    List<DevopsServiceDTO> baseList();

    void baseUpdateLables(Long id);

    void baseDeleteServiceAndInstanceByEnvId(Long envId);

    void baseUpdateEndPoint(Long id);
}
