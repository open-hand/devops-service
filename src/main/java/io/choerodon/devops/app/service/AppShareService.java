package io.choerodon.devops.app.service;

import java.util.List;

import com.github.pagehelper.PageInfo;
import org.springframework.web.multipart.MultipartFile;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.dto.AppMarketDownloadDTO;
import io.choerodon.devops.api.dto.AppMarketTgzDTO;
import io.choerodon.devops.api.dto.AppMarketVersionDTO;
import io.choerodon.devops.api.dto.AppVersionAndValueDTO;
import io.choerodon.devops.api.dto.ApplicationReleasingDTO;
import io.choerodon.devops.api.dto.ApplicationVersionRepDTO;

/**
 * Created by ernst on 2018/5/12.
 */
public interface AppShareService {

    /**
     * 项目下发布应用
     *
     * @param applicationReleaseDTO 发布应用的信息
     * @param projectId             项目ID
     * @return integer
     */
    Long release(Long projectId, ApplicationReleasingDTO applicationReleaseDTO);

    /**
     * 项目下查询所有发布在应用市场的应用
     *
     * @param projectId   项目id
     * @param pageRequest 分页参数
     * @param searchParam 模糊查询参数
     * @return list of ApplicationReleasingDTO
     */
    PageInfo<ApplicationReleasingDTO> listMarketAppsByProjectId(
            Long projectId,
            PageRequest pageRequest,
            String searchParam);

    /**
     * 平台下查询所有发布在平台层的应用
     *
     * @param pageRequest 分页参数
     * @param searchParam 模糊查询参数
     * @return list of ApplicationReleasingDTO
     */
    PageInfo<ApplicationReleasingDTO> listMarketAppsBySite(
            String publishLevel,
            PageRequest pageRequest,
            String searchParam);

    ApplicationReleasingDTO getAppDetailByShareId(Long shareId);

    List<Long> batchRelease(List<ApplicationReleasingDTO> releasingDTOList);


    PageInfo<ApplicationReleasingDTO> getAppsDetail(PageRequest pageRequest, String params, List<Long> shareIds);

    PageInfo<ApplicationVersionRepDTO> getVersionsByAppId(Long appId, PageRequest pageRequest, String params);

    AppVersionAndValueDTO getValuesAndChart(Long versionId);

    void updateByShareId(Long shareId, Boolean idFree);

    /**
     * 查询发布级别为全局或者在本组织下的所有应用市场的应用
     *
     * @param projectId   项目id
     * @param pageRequest 分页参数
     * @param searchParam 模糊查询参数
     * @return list of ApplicationReleasingDTO
     */
    PageInfo<ApplicationReleasingDTO> listMarketApps(Long projectId, PageRequest pageRequest, String searchParam);

    ApplicationReleasingDTO getMarketAppInProject(Long projectId, Long appMarketId);

    /**
     * 查询单个应用市场的应用
     *
     * @param appMarketId 应用市场ID
     * @param versionId   应用版本ID
     * @return ApplicationReleasingDTO
     */
    ApplicationReleasingDTO getMarketApp(Long appMarketId, Long versionId);


    String getMarketAppVersionReadme(Long appMarketId, Long versionId);

    void unpublish(Long projectId, Long appMarketId);

    void unpublish(Long projectId, Long appMarketId, Long versionId);

    void update(Long projectId, Long appMarketId, ApplicationReleasingDTO applicationRelease);

    void update(Long projectId, Long appMarketId, List<AppMarketVersionDTO> versionDTOList);

    List<AppMarketVersionDTO> getAppVersions(Long projectId, Long appMarketId, Boolean isPublish);

    PageInfo<AppMarketVersionDTO> getAppVersions(Long projectId, Long appMarketId, Boolean isPublish,
                                                 PageRequest pageRequest, String searchParam);

    AppMarketTgzDTO getMarketAppListInFile(Long projectId, MultipartFile file);

    Boolean importApps(Long projectId, String fileName, Boolean isPublic);

    void deleteZip(Long projectId, String fileName);

    /**
     * 导出应用市场应用信息
     *
     * @param appMarkets 应用市场应用信息
     */
    void export(List<AppMarketDownloadDTO> appMarkets, String fileName);
}
