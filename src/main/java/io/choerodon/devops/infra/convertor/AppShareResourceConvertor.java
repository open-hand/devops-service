package io.choerodon.devops.infra.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.iam.entity.AppShareResourceE;
import io.choerodon.devops.infra.dataobject.AppShareResourceDO;
import io.choerodon.devops.domain.application.entity.AppShareResourceE;
import io.choerodon.devops.infra.dto.ApplicationShareResourceDTO;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:10 2019/6/28
 * Description:
 */
@Component
public class AppShareResourceConvertor implements ConvertorI<AppShareResourceE, ApplicationShareResourceDTO, Object> {
    @Override
    public ApplicationShareResourceDTO entityToDo(AppShareResourceE applicationMarketE) {
        ApplicationShareResourceDTO devopsAppMarketDO = new ApplicationShareResourceDTO();
        BeanUtils.copyProperties(applicationMarketE, devopsAppMarketDO);
        return devopsAppMarketDO;
    }
}
