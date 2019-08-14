package io.choerodon.devops.app.service;

import java.util.List;

import com.github.pagehelper.PageInfo;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.AppMarketUploadVO;
import io.choerodon.devops.api.vo.AppServiceMarketVO;
import io.choerodon.devops.api.vo.AppServiceMarketVersionVO;
import io.choerodon.devops.api.vo.HarborMarketVO;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:27 2019/8/8
 * Description:
 */
public interface OrgAppMarketService {
    PageInfo<AppServiceMarketVO> pageByAppId(Long appId,
                                             PageRequest pageRequest,
                                             String params);

    void upload(AppMarketUploadVO marketUploadVO);

    List<AppServiceMarketVO> listAllAppServices();

    List<AppServiceMarketVersionVO> listServiceVersionsByAppServiceId(Long appServiceId);

    void createHarborRepository(HarborMarketVO harborMarketVO);
}
