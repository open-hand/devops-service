package io.choerodon.devops.infra.feign.fallback;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.infra.dto.governance.NacosListenConfigDTO;
import io.choerodon.devops.infra.feign.GovernanceServiceClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * Created by younger on 2018/3/29.
 */
@Component
public class GovernanceServiceClientFallback implements GovernanceServiceClient {
    public static final String ERROR_QUERY_LISTEN_CONFIG = "error.query.listen.config";

    @Override
    public ResponseEntity<List<NacosListenConfigDTO>> batchQueryListenConfig(Long organizationId,
                                                                             Long projectId,
                                                                             Set<Long> configIds) {
        throw new CommonException(ERROR_QUERY_LISTEN_CONFIG);
    }
}