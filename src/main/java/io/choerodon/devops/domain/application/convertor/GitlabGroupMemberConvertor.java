package io.choerodon.devops.domain.application.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabMemberE;
import io.choerodon.devops.infra.dataobject.gitlab.MemberDO;

/**
 * Created by Zenger on 2018/3/29.
 */
@Component
public class GitlabGroupMemberConvertor implements ConvertorI<GitlabMemberE, MemberDO, Object> {

    @Override
    public GitlabMemberE doToEntity(MemberDO memberDO) {
        GitlabMemberE gitlabMemberE = new GitlabMemberE();
        BeanUtils.copyProperties(memberDO, gitlabMemberE);
        gitlabMemberE.initAccessLevel(memberDO.getAccessLevel());
        return gitlabMemberE;
    }
}
