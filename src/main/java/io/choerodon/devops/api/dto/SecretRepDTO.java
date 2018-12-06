package io.choerodon.devops.api.dto;

import java.util.Date;
import java.util.Map;

/**
 * Created by n!Ck
 * Date: 18-12-4
 * Time: 上午9:47
 * Description:
 */
public class SecretRepDTO {

    private Long id;
    private String name;
    private Long envId;
    private String description;
    private Map<String, String> secretMaps;
    private Date creationDate;
    private String status;

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

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, String> getSecretMaps() {
        return secretMaps;
    }

    public void setSecretMaps(Map<String, String> secretMaps) {
        this.secretMaps = secretMaps;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
