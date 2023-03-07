package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Set;

import io.choerodon.devops.infra.dto.DevopsCiStepDTO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/11/29 14:21
 */
public interface DevopsCiStepService {
    void baseCreate(DevopsCiStepDTO devopsCiStepDTO);

    List<DevopsCiStepDTO> listByJobIds(List<Long> jobIds);

    void deleteByJobIds(List<Long> jobIds);

    void batchDeleteByIds(Set<Long> ids);

    List<DevopsCiStepDTO> listByJobId(Long jobId);

    Long queryAppServiceIdByStepId(Long id);
}
