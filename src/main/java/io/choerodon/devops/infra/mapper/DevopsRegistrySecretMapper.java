package io.choerodon.devops.infra.mapper;

import io.choerodon.devops.infra.dto.DevopsRegistrySecretDTO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Created by Sheep on 2019/3/14.
 */
public interface DevopsRegistrySecretMapper extends Mapper<DevopsRegistrySecretDTO> {



    void updateStatus(@Param(value = "id") Long id, @Param(value = "status") Boolean status);
}
