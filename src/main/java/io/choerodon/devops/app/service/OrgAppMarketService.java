package io.choerodon.devops.app.service;

import java.util.List;

import com.github.pagehelper.PageInfo;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.AppMarketUploadVO;
import io.choerodon.devops.api.vo.AppServiceUploadVO;
import io.choerodon.devops.api.vo.AppServiceVersionUploadVO;
import io.choerodon.devops.api.vo.HarborMarketVO;
import io.choerodon.devops.app.eventhandler.payload.AppMarketDownloadPayload;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:27 2019/8/8
 * Description:
 */
public interface OrgAppMarketService {
    /**
     * 根据appId 查询应用服务
     * @param appId
     * @param pageRequest
     * @param params
     * @return
     */
    PageInfo<AppServiceUploadVO> pageByAppId(Long appId,
                                             PageRequest pageRequest,
                                             String params);

    /**
     * 查询所有应用服务
     * @return
     */
    List<AppServiceUploadVO> listAllAppServices();

    /**
     * 根据appServiceId 查询所有服务版本
     * @param appServiceId
     * @return
     */
    List<AppServiceVersionUploadVO> listServiceVersionsByAppServiceId(Long appServiceId);

    /**
     * 创建harbor仓库
     * @param harborMarketVO
     * @return
     */
    String createHarborRepository(HarborMarketVO harborMarketVO);

    /**
     * 应用上传
     * @param marketUploadVO
     */
    void uploadAPP(AppMarketUploadVO marketUploadVO);

    /**
     * 应用下载
     * @param appServicePayload
     */
    void downLoadApp(AppMarketDownloadPayload appServicePayload);
}
