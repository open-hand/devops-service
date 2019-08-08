package io.choerodon.devops.app.service;

import java.util.List;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.DevopsConfigVO;
import io.choerodon.devops.api.vo.ProjectDefaultConfigVO;
import io.choerodon.devops.infra.dto.DevopsConfigDTO;

/**
 * @author zongw.lee@gmail.com
 * @since 2019/03/11
 */
public interface DevopsConfigService {

    /**
     * 项目下创建配置
     *
     * @param projectId
     * @param devopsConfigVO
     * @return
     */
    DevopsConfigVO create(Long projectId, DevopsConfigVO devopsConfigVO);

    /**
     * 项目下更新配置信息
     *
     * @param projectId
     * @param devopsConfigVO
     * @return
     */
    DevopsConfigVO update(Long projectId, DevopsConfigVO devopsConfigVO);

    /**
     * 项目下根据Id查询配置
     *
     * @param id
     * @return
     */
    DevopsConfigVO queryById(Long id);

    /**
     * 项目下分页查询配置
     *
     * @param projectId
     * @param pageRequest
     * @param params
     * @return
     */
    PageInfo<DevopsConfigVO> pageByOptions(Long projectId, PageRequest pageRequest, String params);

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
    List<DevopsConfigVO> listByIdAndType(Long projectId, String type);

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

    DevopsConfigDTO baseCreate(DevopsConfigDTO devopsConfigDTO);

    Boolean baseCheckByName(DevopsConfigDTO devopsConfigDTO);

    DevopsConfigDTO baseUpdate(DevopsConfigDTO devopsConfigDTO);

    DevopsConfigDTO baseQuery(Long id);

    DevopsConfigDTO baseQueryByName(Long projectId, String name);

    DevopsConfigDTO baseCheckByName(String name);

    PageInfo<DevopsConfigDTO> basePageByOptions(Long projectId, PageRequest pageRequest, String params);

    void baseDelete(Long id);

    List<DevopsConfigDTO> baseListByIdAndType(Long projectId, String type);

    void baseCheckByName(Long projectId, String name);

    Boolean baseCheckUsed(Long checkIsUsed);

}
