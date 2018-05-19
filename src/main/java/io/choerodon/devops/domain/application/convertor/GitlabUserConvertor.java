package io.choerodon.devops.domain.application.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabUserE;
import io.choerodon.devops.infra.dataobject.gitlab.UserDO;

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
