package io.choerodon.devops.app.service;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.ConfigFileVO;
import io.choerodon.devops.infra.dto.ConfigFileDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * 配置文件表(ConfigFile)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-02-15 09:25:06
 */
public interface ConfigFileService {

    ConfigFileDTO create(String sourceType,
                         Long sourceId,
                         ConfigFileVO configFileVO);

    void update(String sourceType,
                Long sourceId,
                Long id,
                ConfigFileVO configFileVO);

    ConfigFileDTO baseQueryById(Long id);

    void delete(String sourceType,
                Long sourceId,
                Long id);

    Page<ConfigFileVO> paging(String sourceType,
                              Long sourceId,
                              PageRequest pageable,
                              String param);

    ConfigFileVO queryByIdWithDetail(String sourceType, Long sourceId, Long id);
}

