package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsWorkloadResourceContentService;
import io.choerodon.devops.infra.dto.DevopsWorkloadResourceContentDTO;
import io.choerodon.devops.infra.mapper.DevopsWorkloadResourceContentMapper;
import io.choerodon.devops.infra.util.MapperUtil;

@Service
public class DevopsWorkloadResourceContentServiceImpl implements DevopsWorkloadResourceContentService {
    @Autowired
    DevopsWorkloadResourceContentMapper devopsWorkloadResourceContentMapper;

    @Override
    public DevopsWorkloadResourceContentDTO baseQuery(Long workLoadId, String type) {
        DevopsWorkloadResourceContentDTO devopsWorkloadResourceContentDTO = new DevopsWorkloadResourceContentDTO();
        devopsWorkloadResourceContentDTO.setWorkloadId(workLoadId);
        devopsWorkloadResourceContentDTO.setType(type);
        return devopsWorkloadResourceContentMapper.selectOne(devopsWorkloadResourceContentDTO);
    }

    @Override
    @Transactional
    public void create(String type, Long workLoadId, String content) {
        DevopsWorkloadResourceContentDTO devopsWorkloadResourceContentDTO = new DevopsWorkloadResourceContentDTO(workLoadId, type, content);
        MapperUtil.resultJudgedInsert(devopsWorkloadResourceContentMapper, devopsWorkloadResourceContentDTO, "devops.workload.resource.create");
    }

    @Override
    public void update(String type, Long resourceId, String content) {
        if (devopsWorkloadResourceContentMapper.updateContentByResourceIdAndResourceKind(type, resourceId, content) != 1) {
            throw new CommonException("devops.workload.resource.update");
        }
    }

    @Override
    public void deleteByResourceId(String type, Long workloadId) {
        devopsWorkloadResourceContentMapper.deleteByResourceId(type, workloadId);
    }
}
