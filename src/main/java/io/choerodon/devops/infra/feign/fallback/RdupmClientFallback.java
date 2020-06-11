package io.choerodon.devops.infra.feign.fallback;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.harbor.HarborCustomRepoVO;
import io.choerodon.devops.infra.feign.RdupmClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * User: Mr.Wang
 * Date: 2020/6/11
 */
@Component
public class RdupmClientFallback implements RdupmClient {
    @Override
    public ResponseEntity<List<HarborCustomRepoVO>> listAllCustomRepoByProject(Long projectId) {
        throw new CommonException("error.query.repo.list");
    }

    @Override
    public ResponseEntity<HarborCustomRepoVO> listRelatedCustomRepoByService(Long projectId, Long appServiceId) {
        throw new CommonException("error.query.repo.list.by.appServiceId");
    }

    @Override
    public ResponseEntity saveRelationByService(Long projectId, Long appServiceId, Long customRepoId) {
        throw new CommonException("error.save.repo.rel");
    }

    @Override
    public ResponseEntity deleteRelationByService(Long projectId, Long appServiceId, Long customRepoId) {
        throw new CommonException("error.delete.repo");
    }
}
