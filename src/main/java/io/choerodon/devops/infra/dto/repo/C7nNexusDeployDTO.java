package io.choerodon.devops.infra.dto.repo;

/**
 * @author scp
 * @date 2020/7/7
 * @description
 */
public class C7nNexusDeployDTO {

    private C7nNexusComponentDTO c7nNexusComponentDTO;

    private NexusMavenRepoDTO nexusMavenRepoDTO;

    public C7nNexusComponentDTO getC7nNexusComponentDTO() {
        return c7nNexusComponentDTO;
    }

    public void setC7nNexusComponentDTO(C7nNexusComponentDTO c7nNexusComponentDTO) {
        this.c7nNexusComponentDTO = c7nNexusComponentDTO;
    }

    public NexusMavenRepoDTO getNexusMavenRepoDTO() {
        return nexusMavenRepoDTO;
    }

    public void setNexusMavenRepoDTO(NexusMavenRepoDTO nexusMavenRepoDTO) {
        this.nexusMavenRepoDTO = nexusMavenRepoDTO;
    }
}
