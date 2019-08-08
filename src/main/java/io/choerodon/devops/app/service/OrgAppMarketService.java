package io.choerodon.devops.app.service;

import com.github.pagehelper.PageInfo;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.AppServiceMarketVO;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:27 2019/8/8
 * Description:
 */
public interface OrgAppMarketService {
    PageInfo<AppServiceMarketVO> pageByAppId(Long appId,
                                             PageRequest pageRequest,
                                             String params);
}
