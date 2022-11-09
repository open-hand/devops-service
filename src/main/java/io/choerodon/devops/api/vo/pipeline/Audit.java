package io.choerodon.devops.api.vo.pipeline;

import java.util.List;

import io.choerodon.devops.infra.dto.iam.IamUserDTO;

public class Audit {
    private List<IamUserDTO> appointUsers;
    private List<IamUserDTO> reviewedUsers;
    private String status;

    public Audit() {
    }

    public Audit(List<IamUserDTO> appointUsers, List<IamUserDTO> reviewedUsers, String status) {
        this.appointUsers = appointUsers;
        this.reviewedUsers = reviewedUsers;
        this.status = status;
    }

    public List<IamUserDTO> getAppointUsers() {
        return appointUsers;
    }

    public void setAppointUsers(List<IamUserDTO> appointUsers) {
        this.appointUsers = appointUsers;
    }

    public List<IamUserDTO> getReviewedUsers() {
        return reviewedUsers;
    }

    public void setReviewedUsers(List<IamUserDTO> reviewedUsers) {
        this.reviewedUsers = reviewedUsers;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}