package io.choerodon.devops.infra.feign.fallback;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.feign.BaseServiceClient;
import org.springframework.http.ResponseEntity;


/**
 * Create by Li Jinyan on 2019/8/13
 */

public class BaseServiceClientFallback implements BaseServiceClient {
    @Override
    public ResponseEntity<ProjectDTO> queryProjectByApp(Long organizationId, Long applicationId){
        throw new CommonException("error.project.get");
    }
}
