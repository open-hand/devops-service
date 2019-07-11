package io.choerodon.devops.api.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * Creator: Runge
 * Date: 2018/4/18
 * Time: 20:59
 * Description:
 */
public class EnvInstanceDTO {
    private Long envId;
    private List<EnvVersionDTO> envVersions;

    public EnvInstanceDTO() {
    }

    public EnvInstanceDTO(Long envId) {
        this.envId = envId;
        this.envVersions = new ArrayList<>();
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public List<EnvVersionDTO> getEnvVersions() {
        return envVersions;
    }

    public void setEnvVersions(List<EnvVersionDTO> envVersions) {
        this.envVersions = envVersions;
    }

    public EnvVersionDTO queryLastEnvVersionDTO() {
        return envVersions.get(envVersions.size() - 1);
    }

    public void addEnvVersionDTOS(EnvVersionDTO envVersionDTO) {
        this.envVersions.add(envVersionDTO);
    }
}
