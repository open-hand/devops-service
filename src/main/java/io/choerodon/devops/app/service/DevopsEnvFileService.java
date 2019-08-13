package io.choerodon.devops.app.service;

import java.util.List;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.DevopsEnvFileErrorVO;
import io.choerodon.devops.infra.dto.DevopsEnvFileDTO;

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
     * @param pageRequest
     * @return
     */
    PageInfo<DevopsEnvFileErrorVO> pageByEnvId(Long envId, PageRequest pageRequest);

    DevopsEnvFileDTO baseCreate(DevopsEnvFileDTO devopsEnvFileDTO);

    List<DevopsEnvFileDTO> baseListByEnvId(Long envId);

    DevopsEnvFileDTO baseQueryByEnvAndPathAndCommit(Long envId, String path, String commit);

    DevopsEnvFileDTO baseQueryByEnvAndPath(Long envId, String path);

    void baseUpdate(DevopsEnvFileDTO devopsEnvFileDTO);

    void baseDelete(DevopsEnvFileDTO devopsEnvFileDTO);

    List<DevopsEnvFileDTO> baseListByEnvIdAndPath(Long envId, String path);

}
