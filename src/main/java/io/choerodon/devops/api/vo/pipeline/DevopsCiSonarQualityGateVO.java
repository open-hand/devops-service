package io.choerodon.devops.api.vo.pipeline;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

public class DevopsCiSonarQualityGateVO {
    @Encrypt
    private Long id;

    private String name;

    private String level;

    @ApiModelProperty("是否开启质量门")
    private Boolean gatesEnable;
    @ApiModelProperty("质量门失败后是否阻塞后续job")
    private Boolean gatesBlockAfterFail;

    @ApiModelProperty("质量门条件")
    private List<DevopsCiSonarQualityGateConditionVO> sonarQualityGateConditionVOList;

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public Boolean getGatesEnable() {
        return gatesEnable;
    }

    public void setGatesEnable(Boolean gatesEnable) {
        this.gatesEnable = gatesEnable;
    }

    public Boolean getGatesBlockAfterFail() {
        return gatesBlockAfterFail;
    }

    public void setGatesBlockAfterFail(Boolean gatesBlockAfterFail) {
        this.gatesBlockAfterFail = gatesBlockAfterFail;
    }

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

    public List<DevopsCiSonarQualityGateConditionVO> getSonarQualityGateConditionVOList() {
        return sonarQualityGateConditionVOList;
    }

    public void setSonarQualityGateConditionVOList(List<DevopsCiSonarQualityGateConditionVO> sonarQualityGateConditionVOList) {
        this.sonarQualityGateConditionVOList = sonarQualityGateConditionVOList;
    }
}
