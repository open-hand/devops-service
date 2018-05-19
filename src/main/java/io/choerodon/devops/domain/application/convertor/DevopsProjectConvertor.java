package io.choerodon.devops.domain.application.convertor;

import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabGroupE;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.dataobject.DevopsProjectDO;

/**
 * Created by younger on 2018/4/2.
 */
@Component
public class DevopsProjectConvertor implements ConvertorI<GitlabGroupE, DevopsProjectDO, Object> {

    @Override
    public GitlabGroupE doToEntity(DevopsProjectDO devopsProjectDO) {
        GitlabGroupE gitlabGroupE = new GitlabGroupE();
        gitlabGroupE.initId(devopsProjectDO.getGitlabGroupId());
        gitlabGroupE.initProjectE(devopsProjectDO.getId());
        return gitlabGroupE;
    }

    @Override
    public DevopsProjectDO entityToDo(GitlabGroupE gitlabGroupE) {
        DevopsProjectDO devopsProjectDO = new DevopsProjectDO();
        devopsProjectDO.setId(gitlabGroupE.getProjectE().getId());
        devopsProjectDO.setGitlabGroupId(TypeUtil.objToInteger(gitlabGroupE.getId()));
        return devopsProjectDO;
    }
}
