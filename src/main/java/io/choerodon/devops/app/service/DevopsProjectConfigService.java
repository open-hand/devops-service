package io.choerodon.devops.app.service;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.DevopsProjectConfigDTO;
import io.choerodon.devops.api.vo.ProjectDefaultConfigDTO;

import java.util.List;

/**
 * @author zongw.lee@gmail.com
 * @since 2019/03/11
 */
public interface DevopsProjectConfigService {
    DevopsProjectConfigDTO create(Long projectId, DevopsProjectConfigDTO devopsProjectConfigDTO);

    DevopsProjectConfigDTO updateByPrimaryKeySelective(Long projectId, DevopsProjectConfigDTO devopsProjectConfigDTO);

    DevopsProjectConfigDTO queryByPrimaryKey(Long id);

    PageInfo<DevopsProjectConfigDTO> listByOptions(Long projectId, PageRequest pageRequest, String params);

    void delete(Long id);

    List<DevopsProjectConfigDTO> queryByIdAndType(Long projectId, String type);

    /**
     * 创建配置校验名称是否存在
     *
     * @param projectId 项目id
     * @param name      配置name
     */
    void checkName(Long projectId, String name);

    Boolean checkIsUsed(Long configId);


    /**
     * 设置项目对应harbor仓库为私有或者公有
     *
     * @param projectId 项目id
     * @param harborPrivate  是否私有
     */
    void setHarborProjectIsPrivate(Long projectId, boolean harborPrivate);


    /**
     * 设置项目对应harbor仓库为私有或者公有
     *
     * @param projectId 项目id
     * @return String[]
     */
    ProjectDefaultConfigDTO getProjectDefaultConfig(Long projectId);
}
