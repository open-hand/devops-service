package io.choerodon.devops.api.vo.deploy;

import io.choerodon.devops.api.vo.rdupm.ProdJarInfoVO;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import javax.validation.constraints.NotNull;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/7/1 9:10
 */
public class JarDeployVO {
    @Encrypt
    @ApiModelProperty("主机id")
    private Long hostId;

    @ApiModelProperty("部署来源")
    private String sourceType;

    /**
     *  部署对象id
     */
    @Encrypt
    private Long deployObjectId;

    @ApiModelProperty("部署values")
    @NotNull(message = "error.value.is.null")
    private String value;

    private ProdJarInfoVO prodJarInfoVO;

    public JarDeployVO() {
    }

    public JarDeployVO(String sourceType, String value, ProdJarInfoVO prodJarInfoVO) {
        this.sourceType = sourceType;
        this.value = value;
        this.prodJarInfoVO = prodJarInfoVO;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Long getDeployObjectId() {
        return deployObjectId;
    }

    public void setDeployObjectId(Long deployObjectId) {
        this.deployObjectId = deployObjectId;
    }

    public ProdJarInfoVO getProdJarInfoVO() {
        return prodJarInfoVO;
    }

    public void setProdJarInfoVO(ProdJarInfoVO prodJarInfoVO) {
        this.prodJarInfoVO = prodJarInfoVO;
    }

    public String getSourceType() {
        return sourceType;
    }

    public Long getHostId() {
        return hostId;
    }

    public void setHostId(Long hostId) {
        this.hostId = hostId;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }


}
