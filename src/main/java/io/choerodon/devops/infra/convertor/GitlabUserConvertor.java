package io.choerodon.devops.infra.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.iam.entity.gitlab.GitlabUserE;
import io.choerodon.devops.infra.dto.gitlab.UserDO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * Created by Zenger on 2018/3/29.
 */
@Component
public class GitlabUserConvertor implements ConvertorI<GitlabUserE, UserDO, Object> {

    @Override
    public GitlabUserE doToEntity(UserDO userDO) {
        GitlabUserE gitlabUserE = new GitlabUserE();
        BeanUtils.copyProperties(userDO, gitlabUserE);
        return gitlabUserE;
    }

    @Override
    public UserDO entityToDo(GitlabUserE gitlabUserE) {
        UserDO userDO = new UserDO();
        userDO.setId(gitlabUserE.getId());
        userDO.setUsername(gitlabUserE.getUsername());
        return userDO;
    }
}
