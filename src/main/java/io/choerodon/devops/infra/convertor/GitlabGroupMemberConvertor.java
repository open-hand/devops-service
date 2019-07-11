package io.choerodon.devops.infra.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.iam.entity.gitlab.GitlabMemberE;
import io.choerodon.devops.infra.dataobject.gitlab.MemberDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * Created by Zenger on 2018/3/29.
 */
@Component
public class GitlabGroupMemberConvertor implements ConvertorI<GitlabMemberE, MemberDTO, Object> {

    @Override
    public GitlabMemberE doToEntity(MemberDTO memberDO) {
        GitlabMemberE gitlabMemberE = new GitlabMemberE();
        BeanUtils.copyProperties(memberDO, gitlabMemberE);
        gitlabMemberE.initAccessLevel(memberDO.getAccessLevel());
        return gitlabMemberE;
    }
}
