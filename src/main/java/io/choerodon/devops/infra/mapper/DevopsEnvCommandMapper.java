package io.choerodon.devops.infra.mapper;

import java.sql.Date;
import java.util.List;

import io.choerodon.devops.api.vo.kubernetes.Command;
import io.choerodon.devops.infra.dto.DevopsEnvCommandDTO;
import io.choerodon.mybatis.common.Mapper;

import org.apache.ibatis.annotations.Param;

public interface DevopsEnvCommandMapper extends Mapper<DevopsEnvCommandDTO> {

    DevopsEnvCommandDTO queryByObject(@Param("objectType") String objectType, @Param("objectId") Long objectId);

    List<DevopsEnvCommandDTO> listInstanceCommand(@Param("objectType") String objectType, @Param("objectId") Long objectId);

    List<DevopsEnvCommandDTO> listByObject(@Param("objectType") String objectType, @Param("objectId") Long objectId, @Param("startTime") Date startTime, @Param("endTime") Date endTime);

    List<DevopsEnvCommandDTO> listAllInstanceCommandToMigrate();

    void updateSha(@Param("id") Long commandId, @Param("sha") String sha);

    void deleteByObjectTypeAndObjectId(@Param("objectType") String objectType,
                                       @Param("objectId") Long objectId);

    /**
     * 查出环境下的在特定时间之前的还在处理中的资源的command
     *
     * @param envId      环境id
     * @param beforeDate 特定时间字符串，格式为：'yyyy-MM-dd HH:mm:ss'
     * @return Commands
     */
    List<Command> listCommandsToSync(@Param("envId") Long envId,
                                     @Param("beforeDate") String beforeDate);
}
