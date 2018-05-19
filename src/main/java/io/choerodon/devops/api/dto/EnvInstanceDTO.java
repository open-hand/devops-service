package io.choerodon.devops.api.dto;

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
    private List<EnvVersionDTO> envVersionDTOS;

    public EnvInstanceDTO() {
    }

    public EnvInstanceDTO(Long envId) {
        this.envId = envId;
        this.envVersionDTOS = new ArrayList<>();
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public List<EnvVersionDTO> getEnvVersionDTOS() {
        return envVersionDTOS;
    }

    public void setEnvVersionDTOS(List<EnvVersionDTO> envVersionDTOS) {
        this.envVersionDTOS = envVersionDTOS;
    }

    public EnvVersionDTO queryLastEnvVersionDTO() {
        return envVersionDTOS.get(envVersionDTOS.size() - 1);
    }

    public void addEnvVersionDTOS(EnvVersionDTO envVersionDTO) {
        this.envVersionDTOS.add(envVersionDTO);
    }
}
