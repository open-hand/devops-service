package io.choerodon.devops.domain.application.repository;

import com.github.pagehelper.PageInfo;

import java.util.List;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.infra.dto.DevopsSecretDTO;

/**
 * Created by n!Ck
 * Date: 18-12-4
 * Time: 上午10:04
 * Description:
 */
public interface DevopsSecretRepository {

    DevopsSecretDTO baseCreate(DevopsSecretDTO devopsSecretDTO);

    void baseUpdate(DevopsSecretDTO devopsSecretDTO);

    void baseDelete(Long secretId);

    void baseCheckName(String name, Long envId);

    DevopsSecretDTO baseQuery(Long secretId);

    DevopsSecretDTO baseQueryByEnvIdAndName(Long envId, String name);

    PageInfo<DevopsSecretDTO> basePageByOption(Long envId, PageRequest pageRequest, String params, Long appId);

    List<DevopsSecretDTO> baseListByEnv(Long envId);
}
