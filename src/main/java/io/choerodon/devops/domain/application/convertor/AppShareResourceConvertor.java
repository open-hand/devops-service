package io.choerodon.devops.domain.application.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.iam.entity.AppShareResourceE;
import io.choerodon.devops.infra.dataobject.AppShareResourceDO;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:10 2019/6/28
 * Description:
 */
@Component
public class AppShareResourceConvertor implements ConvertorI<AppShareResourceE, AppShareResourceDO, Object> {
    @Override
    public AppShareResourceDO entityToDo(AppShareResourceE applicationMarketE) {
        AppShareResourceDO devopsAppMarketDO = new AppShareResourceDO();
        BeanUtils.copyProperties(applicationMarketE, devopsAppMarketDO);
        return devopsAppMarketDO;
    }
}
