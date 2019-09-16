package io.choerodon.devops.app.service;

import java.util.List;

import com.github.pagehelper.PageInfo;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.iam.AppServiceAndVersionVO;
import io.choerodon.devops.app.eventhandler.payload.*;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:27 2019/8/8
 * Description:
 */
public interface OrgAppMarketService {
    /**
     * 根据appId 查询应用服务
     *
     * @param appId
     * @param pageRequest
     * @param params
     * @return
     */
    PageInfo<AppServiceUploadPayload> pageByAppId(Long appId,
                                                  PageRequest pageRequest,
                                                  String params);

    /**
     * 根据appServiceId 查询所有服务版本
     *
     * @param appServiceId
     * @return
     */
    List<AppServiceVersionUploadPayload> listServiceVersionsByAppServiceId(Long appServiceId);


    /**
     * 应用上传
     *
     * @param marketUploadVO
     */
    void uploadAPP(AppMarketUploadPayload marketUploadVO);

    /**
     * 应用上传 新增修复版本
     *
     * @param appMarketFixVersionPayload
     */
    void uploadAPPFixVersion(AppMarketFixVersionPayload appMarketFixVersionPayload);

    /**
     * 应用下载
     *
     * @param appServicePayload
     */
    void downLoadApp(AppMarketDownloadPayload appServicePayload);

    /**
     * 根据versionId查询应用服务版本
     * 保留原排序
     * @param versionVOList
     */
    List<AppServiceAndVersionVO> listVersions(List<AppServiceAndVersionVO> versionVOList);
}
