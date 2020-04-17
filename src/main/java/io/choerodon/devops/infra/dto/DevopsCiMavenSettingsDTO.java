package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author zmf
 * @since 20-4-16
 */
@Table(name = "devops_ci_maven_settings")
public class DevopsCiMavenSettingsDTO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty("自增主键")
    private Long id;

    @ApiModelProperty("所属的job的id")
    private Long ciJobId;

    @ApiModelProperty("所属step的序列号")
    private Long sequence;

    @ApiModelProperty("maven settings文件内容")
    private String mavenSettings;


    public DevopsCiMavenSettingsDTO() {
    }


    public DevopsCiMavenSettingsDTO(Long ciJobId, Long sequence, String mavenSettings) {
        this.ciJobId = ciJobId;
        this.sequence = sequence;
        this.mavenSettings = mavenSettings;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCiJobId() {
        return ciJobId;
    }

    public void setCiJobId(Long ciJobId) {
        this.ciJobId = ciJobId;
    }

    public Long getSequence() {
        return sequence;
    }

    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }

    public String getMavenSettings() {
        return mavenSettings;
    }

    public void setMavenSettings(String mavenSettings) {
        this.mavenSettings = mavenSettings;
    }
}
