package io.choerodon.devops.infra.dto;


import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.choerodon.mybatis.entity.BaseDTO;

/**
 * @author: 25499
 * @date: 2019/10/23 11:45
 * @description:
 */
@Table(name = "devops_harbor_user")
public class HarborUserDTO extends BaseDTO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String harborProjectUserName;

    private String harborProjectUserEmail;

    private String harborProjectUserPassword;

    private Boolean isPush;

    public HarborUserDTO() {
    }

    public HarborUserDTO(String harborProjectUserName,  String harborProjectUserPassword,String harborProjectUserEmail, boolean isPush) {
        this.harborProjectUserName = harborProjectUserName;
        this.harborProjectUserEmail = harborProjectUserEmail;
        this.harborProjectUserPassword = harborProjectUserPassword;
        this.isPush = isPush;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHarborProjectUserName() {
        return harborProjectUserName;
    }

    public void setHarborProjectUserName(String harborProjectUserName) {
        this.harborProjectUserName = harborProjectUserName;
    }

    public String getHarborProjectUserEmail() {
        return harborProjectUserEmail;
    }

    public void setHarborProjectUserEmail(String harborProjectUserEmail) {
        this.harborProjectUserEmail = harborProjectUserEmail;
    }

    public String getHarborProjectUserPassword() {
        return harborProjectUserPassword;
    }

    public void setHarborProjectUserPassword(String harborProjectUserPassword) {
        this.harborProjectUserPassword = harborProjectUserPassword;
    }

    public boolean isPush() {
        return isPush;
    }

    public void setPush(boolean push) {
        isPush = push;
    }
}