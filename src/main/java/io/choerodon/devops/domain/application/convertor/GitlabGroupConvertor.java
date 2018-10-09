package io.choerodon.devops.domain.application.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabGroupE;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.dataobject.gitlab.GroupDO;

/**
 * Created by younger on 2018/3/29.
 */
@Component
public class GitlabGroupConvertor implements ConvertorI<GitlabGroupE, GroupDO, Object> {

    @Override
    public GitlabGroupE doToEntity(GroupDO groupDO) {
        GitlabGroupE gitlabGroupE = new GitlabGroupE();
        BeanUtils.copyProperties(groupDO, gitlabGroupE);
        gitlabGroupE.setDevopsAppGroupId(TypeUtil.objToLong(groupDO.getId()));
        return gitlabGroupE;
    }

    @Override
    public GroupDO entityToDo(GitlabGroupE gitlabGroupE) {
        GroupDO groupDO = new GroupDO();
        BeanUtils.copyProperties(gitlabGroupE, groupDO);
        return groupDO;
    }

}
