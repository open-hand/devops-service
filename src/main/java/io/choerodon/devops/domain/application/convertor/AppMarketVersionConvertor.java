package io.choerodon.devops.domain.application.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.dto.AppMarketVersionDTO;
import io.choerodon.devops.infra.dataobject.DevopsAppMarketVersionDO;

/**
 * Creator: Runge
 * Date: 2018/5/31
 * Time: 15:51
 * Description:
 */
@Component
public class AppMarketVersionConvertor implements ConvertorI<Object, DevopsAppMarketVersionDO, AppMarketVersionDTO> {

    @Override
    public AppMarketVersionDTO doToDto(DevopsAppMarketVersionDO versionDO) {
        AppMarketVersionDTO versionDTO = new AppMarketVersionDTO();
        BeanUtils.copyProperties(versionDO, versionDTO);
        return versionDTO;
    }
}
