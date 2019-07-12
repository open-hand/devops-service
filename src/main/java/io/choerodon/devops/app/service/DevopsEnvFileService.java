package io.choerodon.devops.app.service;

import java.util.List;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.DevopsEnvFileErrorVO;
import io.choerodon.devops.api.vo.DevopsEnvFileVO;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvFileE;
import io.choerodon.devops.infra.dto.DevopsEnvFileDTO;

/**
 * Creator: Runge
 * Date: 2018/8/10
 * Time: 11:03
 * Description:
 */
public interface DevopsEnvFileService {
    List<DevopsEnvFileErrorVO> listByEnvId(Long envId);

    PageInfo<DevopsEnvFileErrorVO> pageByEnvId(Long envId, PageRequest pageRequest);

    DevopsEnvFileVO baseCreate(DevopsEnvFileVO devopsEnvFileE);

    List<DevopsEnvFileDTO> baseListByEnvId(Long envId);

    DevopsEnvFileDTO baseQueryByEnvAndPathAndCommit(Long envId, String path, String commit);

    DevopsEnvFileDTO baseQueryByEnvAndPathAndCommits(Long envId, String path, List<String> commits);

    DevopsEnvFileDTO baseQueryByEnvAndPath(Long envId, String path);

    void baseUpdate(DevopsEnvFileDTO devopsEnvFileDTO);

    void baseDelete(DevopsEnvFileDTO devopsEnvFileDTO);

    List<DevopsEnvFileDTO> baseListByEnvIdAndPath(Long envId, String path);

}
