package io.choerodon.devops.app.eventhandler.payload;

import io.choerodon.devops.api.vo.GitlabGroupMemberVO;
import io.choerodon.devops.api.vo.GitlabUserVO;

import java.util.List;

/**
 * User: Mr.Wang
 * Date: 2020/2/10
 */
public class CreateAndUpdateUserEventPayload {
    private GitlabUserVO userEventPayload;
    private List<GitlabGroupMemberVO> userMemberEventPayloads;


    public GitlabUserVO getUserEventPayload() {
        return userEventPayload;
    }

    public void setUserEventPayload(GitlabUserVO userEventPayload) {
        this.userEventPayload = userEventPayload;
    }

    public List<GitlabGroupMemberVO> getUserMemberEventPayloads() {
        return userMemberEventPayloads;
    }

    public void setUserMemberEventPayloads(List<GitlabGroupMemberVO> userMemberEventPayloads) {
        this.userMemberEventPayloads = userMemberEventPayloads;
    }
}
