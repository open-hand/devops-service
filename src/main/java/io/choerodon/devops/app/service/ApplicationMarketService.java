package io.choerodon.devops.app.service;

import org.springframework.web.multipart.MultipartFile;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.ApplicationReleasingDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by ernst on 2018/5/12.
 */
public interface ApplicationMarketService {

    /**
     * 项目下发布应用
     *
     * @param applicationReleaseDTO 发布应用的信息
     */
    void release(Long projectId, ApplicationReleasingDTO applicationReleaseDTO,MultipartFile multipartFile);

    /**
     * 项目下查询所有发布在应用市场的应用
     *
     * @param projectId   项目id
     * @param pageRequest 分页参数
     * @return list of ApplicationReleasingDTO
     */
    Page<ApplicationReleasingDTO> listMarketAppsByProjectId(
            Long projectId,
            PageRequest pageRequest,
            String searchParam);

    /**
     * 查询发布级别为全局或者在本组织下的所有应用市场的应用
     *
     * @param projectId   项目id
     * @param pageRequest 分页参数
     * @return list of ApplicationReleasingDTO
     */
    Page<ApplicationReleasingDTO> listMarketApps(Long projectId, PageRequest pageRequest, String searchParam);
}
