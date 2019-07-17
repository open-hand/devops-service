package io.choerodon.devops.infra.convertor;

import io.choerodon.devops.api.vo.RunningInstanceVO;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.iam.entity.ApplicationInstanceE;

/**
 * Created by Zenger on 2018/4/18.
 */
@Component
public class AppInstanceCodeConvertor implements ConvertorI<ApplicationInstanceE, Object, RunningInstanceVO> {

    @Override
    public RunningInstanceVO entityToDto(ApplicationInstanceE entity) {
        RunningInstanceVO runningInstanceVO = new RunningInstanceVO();
        runningInstanceVO.setId(entity.getId().toString());
        runningInstanceVO.setCode(entity.getCode());
        runningInstanceVO.setIsEnabled(entity.getIsEnabled());
        if (entity.getApplicationVersionE() != null) {
            runningInstanceVO.setAppVersion(entity.getApplicationVersionE().getVersion());
        }
        return runningInstanceVO;
    }
}
