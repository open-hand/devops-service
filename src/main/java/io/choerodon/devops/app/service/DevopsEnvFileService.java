package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.DevopsEnvFileErrorVO;
import io.choerodon.devops.infra.dto.DevopsEnvFileDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Creator: Runge
 * Date: 2018/8/10
 * Time: 11:03
 * Description:
 */
public interface DevopsEnvFileService {

    /**
     * 项目下查询环境文件错误列表
     *
     * @param envId
     * @return
     */
    List<DevopsEnvFileErrorVO> listByEnvId(Long envId);

    /**
     * 项目下分页查询环境文件错误列表
     *
     * @param envId
     * @param pageable
     * @return
     */
    Page<DevopsEnvFileErrorVO> pageByEnvId(Long envId, PageRequest pageable);

    DevopsEnvFileDTO baseCreate(DevopsEnvFileDTO devopsEnvFileDTO);

    List<DevopsEnvFileDTO> baseListByEnvId(Long envId);

    DevopsEnvFileDTO baseQueryByEnvAndPathAndCommit(Long envId, String path, String commit);

    DevopsEnvFileDTO baseQueryByEnvAndPath(Long envId, String path);

    void baseUpdate(DevopsEnvFileDTO devopsEnvFileDTO);

    void baseDelete(DevopsEnvFileDTO devopsEnvFileDTO);

    List<DevopsEnvFileDTO> baseListByEnvIdAndPath(Long envId, String path);

    /**
     * 根据环境id删除相应的纪录
     * 删除环境时使用
     *
     * @param envId 环境id
     */
    void deleteByEnvId(Long envId);
}
