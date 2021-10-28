package io.choerodon.devops.infra.mapper;

import io.choerodon.devops.api.vo.deploy.ConfigSettingVO;
import io.choerodon.devops.infra.dto.DeployConfigDTO;
import io.choerodon.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 主机部署文件配置表Mapper
 *
 * @author jian.zhang02@hand-china.com 2021-08-19 15:43:01
 */
public interface DeployConfigMapper extends BaseMapper<DeployConfigDTO> {

    /**
     * 查询主机部署jar应用监听配置信息
     *
     * @param hostId
     * @return
     */
    List<DeployConfigDTO> queryConfigsByHostId(@Param("hostId") Long hostId);

    /**
     * 查询配置文件信息
     *
     * @param projectId
     * @param recordId
     * @param instanceId
     * @return
     */
    DeployConfigDTO queryDeployConfig(@Param("projectId") Long projectId,
                                      @Param("recordId") Long recordId,
                                      @Param("instanceId") Long instanceId);
}
