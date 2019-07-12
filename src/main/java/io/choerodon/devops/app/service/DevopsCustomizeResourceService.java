package io.choerodon.devops.app.service;

import java.util.List;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.DevopsCustomizeResourceVO;
import io.choerodon.devops.api.vo.DevopsCustomizeResourceReqDTO;
import io.choerodon.devops.api.vo.iam.entity.DevopsCustomizeResourceE;
import io.choerodon.devops.infra.dto.DevopsCustomizeResourceDTO;

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
    DevopsCustomizeResourceVO getDevopsCustomizeResourceDetail(Long resourceId);

    /**
     * @param envId
     * @param pageRequest
     * @param params
     * @return
     */
    PageInfo<DevopsCustomizeResourceVO> pageResources(Long envId, PageRequest pageRequest, String params);

    DevopsCustomizeResourceDTO baseCreate(DevopsCustomizeResourceDTO devopsCustomizeResourceDTO);

    DevopsCustomizeResourceDTO baseQuery(Long resourceId);

    void baseUpdate(DevopsCustomizeResourceDTO devopsCustomizeResourceDTO);

    void baseDelete(Long resourceId);

    List<DevopsCustomizeResourceDTO> listByEnvAndFilePath(Long envId, String filePath);

    DevopsCustomizeResourceDTO queryByEnvIdAndKindAndName(Long envId, String kind, String name);

    DevopsCustomizeResourceDTO queryDetail(Long resourceId);

    PageInfo<DevopsCustomizeResourceDTO> pageDevopsCustomizeResourceE(Long envId, PageRequest pageRequest, String params);

    void checkExist(Long envId, String kind, String name);
}
