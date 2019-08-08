package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Map;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.AppServiceMarketVO;
import io.choerodon.devops.api.vo.AppServiceMarketVersionVO;
import io.choerodon.devops.app.service.AppServiceVersionService;
import io.choerodon.devops.app.service.OrgAppMarketService;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.mapper.AppServiceMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.PageRequestUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.swagger.annotation.CustomPageRequest;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:28 2019/8/8
 * Description:
 */
@Component
public class OrgAppMarketServiceImpl implements OrgAppMarketService {
    @Autowired
    private AppServiceMapper appServiceMapper;
    @Autowired
    private AppServiceVersionService appServiceVersionService;

    @Override
    public PageInfo<AppServiceMarketVO> pageByAppId(Long appId,
                                                    PageRequest pageRequest, String params) {
        Map<String, Object> mapParams = TypeUtil.castMapParams(params);
        List<String> paramList = mapParams.get(TypeUtil.PARAMS) == null ? null : (List<String>) mapParams.get(TypeUtil.PARAMS);
        PageInfo<AppServiceDTO> appServiceDTOPageInfo = PageHelper
                .startPage(pageRequest.getPage(),
                        pageRequest.getSize(),
                        PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() ->
                        appServiceMapper.listByAppId(appId, (Map<String, Object>) mapParams.get(TypeUtil.SEARCH_PARAM), paramList));

        PageInfo<AppServiceMarketVO> appServiceMarketVOPageInfo = ConvertUtils.convertPage(appServiceDTOPageInfo, this::dtoToMarketVO);
        List<AppServiceMarketVO> list = appServiceMarketVOPageInfo.getList();
        list.forEach(appServiceMarketVO -> appServiceMarketVO.setAppServiceMarketVersionVOS(
                ConvertUtils.convertList(appServiceVersionService.baseListByAppServiceId(appServiceMarketVO.getAppServiceId()), AppServiceMarketVersionVO.class)));
        appServiceMarketVOPageInfo.setList(list);
        return appServiceMarketVOPageInfo;
    }

    private AppServiceMarketVO dtoToMarketVO(AppServiceDTO applicationDTO) {
        AppServiceMarketVO appServiceMarketVO = new AppServiceMarketVO();
        appServiceMarketVO.setAppServiceId(applicationDTO.getId());
        appServiceMarketVO.setAppServiceCode(applicationDTO.getCode());
        appServiceMarketVO.setAppServiceName(applicationDTO.getName());
        return appServiceMarketVO;
    }

}
