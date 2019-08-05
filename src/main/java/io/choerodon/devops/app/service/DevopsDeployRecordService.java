package io.choerodon.devops.app.service;


import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.DevopsDeployRecordVO;
import io.choerodon.devops.infra.dto.DevopsDeployRecordDTO;

/**
 * Created by Sheep on 2019/7/29.
 */
public interface DevopsDeployRecordService {

    PageInfo<DevopsDeployRecordVO> pageByProjectId(Long projectId, String params, PageRequest pageRequest);


    PageInfo<DevopsDeployRecordDTO> basePageByProjectId(Long projectId, String params, PageRequest pageRequest);

    void baseCreate(DevopsDeployRecordDTO devopsDeployRecordDTO);

}
