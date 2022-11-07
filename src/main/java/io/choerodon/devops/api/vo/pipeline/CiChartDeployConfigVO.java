package io.choerodon.devops.api.vo.pipeline;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * @author hao.wang@zknow.com
 * @since 2022-11-04 14:50:19
 */
public class CiChartDeployConfigVO extends AppDeployConfigVO {

    @ApiModelProperty(value = "valueId,devops_deploy_value.id", required = true)
    @Encrypt
    private Long valueId;
    @ApiModelProperty(value = "部署配置values", required = true)
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Long getValueId() {
        return valueId;
    }

    public void setValueId(Long valueId) {
        this.valueId = valueId;
    }
}
