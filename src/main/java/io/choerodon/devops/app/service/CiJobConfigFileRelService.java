package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.pipeline.ConfigFileRelVO;
import io.choerodon.devops.infra.dto.CiJobConfigFileRelDTO;

/**
 * CI配置文件关联表(CiJobConfigFileRel)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-02-16 15:50:03
 */
public interface CiJobConfigFileRelService {

    void baseCreate(CiJobConfigFileRelDTO ciJobConfigFileRelDTO);

    void deleteByJobIds(List<Long> jobIds);

    List<CiJobConfigFileRelDTO> listByJobId(Long id);

    List<ConfigFileRelVO> listVOByJobId(Long id);
}

