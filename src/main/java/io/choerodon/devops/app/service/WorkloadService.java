package io.choerodon.devops.app.service;

import org.springframework.web.multipart.MultipartFile;

import io.choerodon.devops.api.vo.WorkloadBaseCreateOrUpdateVO;
import io.choerodon.devops.infra.enums.ResourceType;

public interface WorkloadService {
    void createOrUpdate(Long projectId, WorkloadBaseCreateOrUpdateVO workloadBaseCreateOrUpdateVO, MultipartFile multipartFile, ResourceType type);

    Long getWorkloadId(Long envId,String workloadName,String type);

    void delete(Long projectId, Long id, ResourceType resourceType);
}
