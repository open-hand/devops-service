package io.choerodon.devops.domain.application.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabGroupMemberE;
import io.choerodon.devops.infra.dataobject.gitlab.MemberDO;

/**
 * Created by Zenger on 2018/3/29.
 */
@Component
public class GitlabGroupMemberConvertor implements ConvertorI<GitlabGroupMemberE, MemberDO, Object> {

    @Override
    public GitlabGroupMemberE doToEntity(MemberDO memberDO) {
        GitlabGroupMemberE gitlabGroupMemberE = new GitlabGroupMemberE();
        BeanUtils.copyProperties(memberDO, gitlabGroupMemberE);
        gitlabGroupMemberE.initAccessLevel(memberDO.getAccessLevel());
        return gitlabGroupMemberE;
    }
}
