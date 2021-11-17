package io.choerodon.devops.infra.feign.operator;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.infra.dto.governance.NacosListenConfigDTO;
import io.choerodon.devops.infra.feign.GovernanceServiceClient;
import io.choerodon.devops.infra.feign.fallback.GovernanceServiceClientFallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Component
public class GovernanceServiceClientOperator {
    @Autowired
    private GovernanceServiceClient governanceServiceClient;

    /**
     * 查询监听配置
     *
     * @param tenantId
     * @param projectId
     * @param configIds
     */
    public List<NacosListenConfigDTO> batchQueryListenConfig(Long tenantId, Long projectId, Set<Long> configIds) {
        ResponseEntity<List<NacosListenConfigDTO>> nacosListenConfigResponse =
                governanceServiceClient.batchQueryListenConfig(tenantId, projectId, configIds);
        if (Objects.isNull(nacosListenConfigResponse)
                || !nacosListenConfigResponse.getStatusCode().is2xxSuccessful()
                || CollectionUtils.isEmpty(nacosListenConfigResponse.getBody())) {
            throw new CommonException(GovernanceServiceClientFallback.ERROR_QUERY_LISTEN_CONFIG);
        }
        return nacosListenConfigResponse.getBody();
    }
}
