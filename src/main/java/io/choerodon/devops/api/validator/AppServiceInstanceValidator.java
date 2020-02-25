package io.choerodon.devops.api.validator;

import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.AppServiceDeployVO;

/**
 * Created by n!Ck
 * Date: 2018/11/20
 * Time: 9:51
 * Description:
 */
@Component
public class AppServiceInstanceValidator {
    //appServiceInstance name
    private static final String NAME_PATTERN = "[a-z]([-a-z0-9]*[a-z0-9])?";

    private AppServiceInstanceValidator() {
    }


    public static void checkName(String name) {
        if (!Pattern.matches(NAME_PATTERN, name)) {
            throw new CommonException("error.app.instance.name.notMatch");
        }
    }

    public void validateBatchDeployment(List<AppServiceDeployVO> appServiceDeployVOS) {
        // TODO by zmf
    }
}
