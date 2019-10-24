package io.choerodon.devops.infra.dto;


import javax.persistence.Column;

import io.choerodon.mybatis.entity.BaseDTO;

/**
 * @author: 25499
 * @date: 2019/10/23 11:45
 * @description:
 */
public class HarborUserDTO extends BaseDTO {
    private Long id;

    private String username;

    private String email;

    private String password;

    private boolean isPush;

    public HarborUserDTO() {
    }

    public HarborUserDTO(String username, String email, String password, boolean isPush) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.isPush = isPush;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isPush() {
        return isPush;
    }

    public void setPush(boolean push) {
        isPush = push;
    }
}