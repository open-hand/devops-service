package io.choerodon.devops.app.service;

import java.util.List;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.DevopsProjectConfigVO;
import io.choerodon.devops.api.vo.ProjectDefaultConfigVO;
import io.choerodon.devops.infra.dto.DevopsProjectConfigDTO;

/**
 * @author zongw.lee@gmail.com
 * @since 2019/03/11
 */
public interface DevopsProjectConfigService {

    /**
     * 项目下创建配置
     *
     * @param projectId
     * @param devopsProjectConfigVO
     * @return
     */
    DevopsProjectConfigVO create(Long projectId, DevopsProjectConfigVO devopsProjectConfigVO);

    /**
     * 项目下更新配置信息
     *
     * @param projectId
     * @param devopsProjectConfigVO
     * @return
     */
    DevopsProjectConfigVO update(Long projectId, DevopsProjectConfigVO devopsProjectConfigVO);

    /**
     * 项目下根据Id查询配置
     *
     * @param id
     * @return
     */
    DevopsProjectConfigVO queryById(Long id);

    /**
     * 项目下分页查询配置
     *
     * @param projectId
     * @param pageRequest
     * @param params
     * @return
     */
    PageInfo<DevopsProjectConfigVO> pageByOptions(Long projectId, PageRequest pageRequest, String params);

    /**
     * 项目下删除配置
     *
     * @param id
     */
    void delete(Long id);

    /**
     * 项目下根据类型查询配置
     *
     * @param projectId
     * @param type
     * @return
     */
    List<DevopsProjectConfigVO> listByIdAndType(Long projectId, String type);

    /**
     * 创建配置校验名称是否存在
     *
     * @param projectId 项目id
     * @param name      配置name
     */
    void checkName(Long projectId, String name);

    /**
     * 根据配置id查询，该配置是否被使用
     *
     * @param configId
     * @return
     */
    Boolean checkIsUsed(Long configId);


    /**
     * 设置项目对应harbor仓库为私有或者公有
     *
     * @param projectId     项目id
     * @param harborPrivate 是否私有
     */
    void operateHarborProject(Long projectId, Boolean harborPrivate);


    /**
     * 设置项目对应harbor仓库为私有或者公有
     *
     * @param projectId 项目id
     * @return String[]
     */
    ProjectDefaultConfigVO queryProjectDefaultConfig(Long projectId);

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
