package io.choerodon.devops.app.service;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.DevopsCustomizeResourceDTO;
import io.choerodon.devops.api.vo.DevopsCustomizeResourceReqDTO;
import io.choerodon.devops.domain.application.entity.DevopsCustomizeResourceE;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by Sheep on 2019/6/26.
 */
public interface DevopsCustomizeResourceService {

    /**
     *
     * @param projectId
     * @param devopsCustomizeResourceReqDTO
     * @param contentFile
     */
    void createOrUpdateResource(Long projectId, DevopsCustomizeResourceReqDTO devopsCustomizeResourceReqDTO, MultipartFile contentFile);


    /**
     * @param type
     * @param devopsCustomizeResourceE
     */
    void createOrUpdateResourceByGitOps(String type, DevopsCustomizeResourceE devopsCustomizeResourceE, Long userId, Long envId);


    /**
     * @param resourceId
     */
    void deleteResource(Long resourceId);


    /**
     * @param resourceId
     */
    void deleteResourceByGitOps(Long resourceId);

    /**
     * @param resourceId
     * @return
     */
    DevopsCustomizeResourceDTO getDevopsCustomizeResourceDetail(Long resourceId);

    /**
     * @param envId
     * @param pageRequest
     * @param params
     * @return
     */
    PageInfo<DevopsCustomizeResourceDTO> pageResources(Long envId, PageRequest pageRequest, String params);
}
