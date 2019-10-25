package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Set;

import com.github.pagehelper.PageInfo;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.DefaultConfigVO;
import io.choerodon.devops.api.vo.DevopsConfigRepVO;
import io.choerodon.devops.api.vo.DevopsConfigVO;
import io.choerodon.devops.infra.dto.DevopsConfigDTO;

/**
 * @author zongw.lee@gmail.com
 * @since 2019/03/11
 */
public interface DevopsConfigService {

    /**
     * 项目下处理
     *
     * @param resourceId
     * @param resourceType
     * @param devopsConfigVO
     * @return
     */
    void operate(Long resourceId, String resourceType, List<DevopsConfigVO> devopsConfigVO);


    /**
     * 项目下根据类型查询配置
     *
     * @param resourceId
     * @param resourceType
     * @return
     */
    List<DevopsConfigVO> queryByResourceId(Long resourceId, String resourceType);


    /**
     * 查询各资源层级默认组件设置
     *
     * @param resourceId   资源id
     * @param resourceType 资源类型
     * @return DefaultConfigVO
     */
    DefaultConfigVO queryDefaultConfig(Long resourceId, String resourceType);


    DevopsConfigDTO queryRealConfig(Long resourceId, String resourceType, String configType,String operateType);

    DevopsConfigVO queryRealConfigVO(Long resourceId, String resourceType, String configType);

    DevopsConfigDTO baseCreate(DevopsConfigDTO devopsConfigDTO);

    DevopsConfigDTO baseUpdate(DevopsConfigDTO devopsConfigDTO);

    void updateResourceId(Long configId);

    DevopsConfigDTO baseQuery(Long id);

    DevopsConfigDTO baseQueryByName(Long projectId, String name);

    DevopsConfigDTO baseCheckByName(String name);

    PageInfo<DevopsConfigDTO> basePageByOptions(Long projectId, PageRequest pageRequest, String params);

    void baseDelete(Long id);

    DevopsConfigDTO baseQueryByResourceAndType(Long resourceId, String resourceType, String configType);

    DevopsConfigVO dtoToVo(DevopsConfigDTO devopsConfigDTO);

    DevopsConfigDTO voToDto(DevopsConfigVO devopsConfigVO);

    /***
     * 查询 仓库配置
     */
    DevopsConfigRepVO queryConfig(Long resourceId, String resourceType);

    /**
     * 操作 仓库配置
     */
    void operateConfig(Long organizationId, String resourceType, DevopsConfigRepVO devopsConfigRepVO);

    void deleteByConfigIds(Set<Long> configIds);
}

