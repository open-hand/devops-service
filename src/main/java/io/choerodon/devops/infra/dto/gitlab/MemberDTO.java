package io.choerodon.devops.infra.dto.gitlab;


/**
 * Created by Sheep on 2019/7/12.
 */
public class MemberDTO {
    private Integer id;
    private Integer accessLevel;
    private String expiresAt;

    public MemberDTO(Integer userId, Integer accessLevel, String expiresAt) {
        this.id = userId;
        this.accessLevel = accessLevel;
        this.expiresAt = expiresAt;
    }

    public MemberDTO(Integer userId, Integer accessLevel) {
        this.id = userId;
        this.accessLevel = accessLevel;
    }

    public MemberDTO() {

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(Integer accessLevel) {
        this.accessLevel = accessLevel;
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(String expiresAt) {
        this.expiresAt = expiresAt;
    }
}
