package io.choerodon.devops.app.service;

import java.util.List;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.infra.dto.ApplicationShareDTO;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by ernst on 2018/5/12.
 */
public interface ApplicationShareService {

    /**
     * 项目下发布应用
     *
     * @param applicationReleaseDTO 发布应用的信息
     * @param projectId             项目ID
     * @return integer
     */
    Long create(Long projectId, ApplicationReleasingVO applicationReleaseDTO);

    /**
     * 项目下查询所有发布在应用市场的应用
     *
     * @param projectId   项目id
     * @param pageRequest 分页参数
     * @param searchParam 模糊查询参数
     * @return baseList of ApplicationReleasingDTO
     */
    PageInfo<ApplicationReleasingVO> pageByOptions(
            Long projectId,
            PageRequest pageRequest,
            String searchParam);

    AppVersionAndValueVO getValuesAndChart(Long versionId);

    /**
     * 查询发布级别为全局或者在本组织下的所有应用市场的应用
     *
     * @param projectId   项目id
     * @param pageRequest 分页参数
     * @param searchParam 模糊查询参数
     * @return baseList of ApplicationReleasingDTO
     */
    PageInfo<ApplicationReleasingVO> listMarketApps(Long projectId, PageRequest pageRequest, String searchParam);

    ApplicationReleasingVO queryById(Long projectId, Long appMarketId);

    /**
     * 查询单个应用市场的应用
     *
     * @param appMarketId 应用市场ID
     * @param versionId   应用版本ID
     * @return ApplicationReleasingDTO
     */
    ApplicationReleasingVO queryShareApp(Long appMarketId, Long versionId);


    String queryAppVersionReadme(Long appMarketId, Long versionId);

    void unpublish(Long projectId, Long appMarketId);

    void unpublish(Long projectId, Long appMarketId, Long versionId);

    void update(Long projectId, Long appMarketId, ApplicationReleasingVO applicationRelease);

    void update(Long projectId, Long appMarketId, List<AppMarketVersionVO> versionDTOList);

    List<AppMarketVersionVO> queryAppVersionsById(Long projectId, Long appMarketId, Boolean isPublish);

    PageInfo<AppMarketVersionVO> queryAppVersionsById(Long projectId, Long appMarketId, Boolean isPublish,
                                                      PageRequest pageRequest, String searchParam);

    AppMarketTgzVO upload(Long projectId, MultipartFile file);

    Boolean importApps(Long projectId, String fileName, Boolean isPublic);

    void importCancel(Long projectId, String fileName);

    /**
     * 导出应用市场应用信息
     *
     * @param appMarkets 应用市场应用信息
     */
    void export(List<AppMarketDownloadVO> appMarkets, String fileName);

    AccessTokenCheckResultVO checkToken(AccessTokenVO tokenDTO);

    void saveToken(AccessTokenVO tokenDTO);

    ApplicationShareDTO baseQueryByAppId(Long appId);

    int baseCountByAppId(Long appId);

}
