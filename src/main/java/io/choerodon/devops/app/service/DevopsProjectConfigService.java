package io.choerodon.devops.app.service;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.DevopsProjectConfigVO;
import io.choerodon.devops.api.vo.ProjectDefaultConfigDTO;
import io.choerodon.devops.infra.dto.DevopsProjectConfigDTO;

import java.util.List;

/**
 * @author zongw.lee@gmail.com
 * @since 2019/03/11
 */
public interface DevopsProjectConfigService {
    DevopsProjectConfigVO create(Long projectId, DevopsProjectConfigVO devopsProjectConfigVO);

    DevopsProjectConfigVO updateByPrimaryKeySelective(Long projectId, DevopsProjectConfigVO devopsProjectConfigVO);

    DevopsProjectConfigVO queryByPrimaryKey(Long id);

    PageInfo<DevopsProjectConfigVO> listByOptions(Long projectId, PageRequest pageRequest, String params);

    void delete(Long id);

    List<DevopsProjectConfigVO> queryByIdAndType(Long projectId, String type);

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

    DevopsProjectConfigDTO baseCreate(DevopsProjectConfigDTO devopsProjectConfigDTO);

    Boolean baseCheckByName(DevopsProjectConfigDTO devopsProjectConfigDTO);

    DevopsProjectConfigDTO baseUpdate(DevopsProjectConfigDTO devopsProjectConfigDTO);

    DevopsProjectConfigDTO baseQuery(Long id);

    DevopsProjectConfigDTO baseQueryByName(Long projectId, String name);

    DevopsProjectConfigDTO baseCheckByName(String name);

    PageInfo<DevopsProjectConfigDTO> basePageByOptions(Long projectId, PageRequest pageRequest, String params);

    void baseDelete(Long id);

    List<DevopsProjectConfigDTO> baseListByIdAndType(Long projectId, String type);

    void baseCheckByName(Long projectId, String name);

    Boolean baseCheckUsed(Long checkIsUsed);

}
