package io.choerodon.devops.api.vo.iam;

import java.util.Date;

/**
 * @author zongw.lee@gmail.com
 * @date 2019/8/05
 */
public class RemoteTokenAuthorizationVO {

    private Long id;

    private String name;

    private String email;

    private String encryptionToken;

    private String status;

    private Date creationDate;

    private String remoteToken;

    private String authorizationUrl;

    private Long objectVersionNumber;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEncryptionToken() {
        return encryptionToken;
    }

    public void setEncryptionToken(String encryptionToken) {
        this.encryptionToken = encryptionToken;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getRemoteToken() {
        return remoteToken;
    }

    public void setRemoteToken(String remoteToken) {
        this.remoteToken = remoteToken;
    }

    public String getAuthorizationUrl() {
        return authorizationUrl;
    }

    public void setAuthorizationUrl(String authorizationUrl) {
        this.authorizationUrl = authorizationUrl;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }
}
