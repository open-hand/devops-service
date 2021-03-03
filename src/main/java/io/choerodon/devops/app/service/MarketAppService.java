package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.ApplicationImportInternalVO;

/**
 * Created by wangxiang on 2021/3/2
 */
public interface MarketAppService {
    void importAppService(Long projectId, List<ApplicationImportInternalVO> applicationImportInternalVOS);
}
