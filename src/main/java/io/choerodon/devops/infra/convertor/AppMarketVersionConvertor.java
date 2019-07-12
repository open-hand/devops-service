package io.choerodon.devops.infra.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.AppMarketVersionDTO;
import io.choerodon.devops.infra.dto.ApplicationShareVersionDTO;

/**
 * Creator: Runge
 * Date: 2018/5/31
 * Time: 15:51
 * Description:
 */
@Component
public class AppMarketVersionConvertor implements ConvertorI<Object, ApplicationShareVersionDTO, AppMarketVersionDTO> {

    @Override
    public AppMarketVersionDTO doToDto(ApplicationShareVersionDTO versionDO) {
        AppMarketVersionDTO versionDTO = new AppMarketVersionDTO();
        BeanUtils.copyProperties(versionDO, versionDTO);
        return versionDTO;
    }
}
