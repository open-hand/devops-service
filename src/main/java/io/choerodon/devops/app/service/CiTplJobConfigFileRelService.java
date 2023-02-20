package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.pipeline.ConfigFileRelVO;
import io.choerodon.devops.infra.dto.CiTplJobConfigFileRelDTO;

/**
 * CI任务模板配置文件关联表(CiTplJobConfigFileRel)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-02-16 15:50:17
 */
public interface CiTplJobConfigFileRelService {

    void baseCreate(CiTplJobConfigFileRelDTO ciTplJobConfigFileRelDTO);

    void deleteByJobId(Long jobId);

    List<CiTplJobConfigFileRelDTO> listByJobId(Long jobId);

    List<ConfigFileRelVO> listVOByJobId(Long jobId);
}

