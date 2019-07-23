package io.choerodon.devops.infra.mapper;

import java.util.List;

import io.choerodon.devops.api.vo.DevopsEnvMessageVO;
import io.choerodon.devops.infra.dto.DevopsEnvApplicationDTO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

public interface DevopsEnvApplicationMapper extends Mapper<DevopsEnvApplicationDTO> {

    /**
     * 当记录不存在时，插入记录
     *
     * @param devopsEnvApplicationDO 环境应用关联关系
     * @return 影响的记录数目
     */
    void insertIgnore(DevopsEnvApplicationDTO devopsEnvApplicationDO);

    /**
     * 通过环境Id查询所有应用Id
     *
     * @param envId 环境Id
     * @return List 应用Id
     */
    List<Long> queryAppByEnvId(@Param("envId") Long envId);

    /**
     * 通过环境Id查询所有应用Id
     *
     * @param envId 环境Id
     * @param appId 应用Id
     * @return List 环境资源信息
     */
    List<DevopsEnvMessageVO> listResourceByEnvAndApp(@Param("envId") Long envId, @Param("appId") Long appId);
}
