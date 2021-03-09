package io.choerodon.devops.api.vo.hrdsCode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * 返回给猪齿鱼
 *
 * @author chenxiuhong 2020/04/24 11:37 上午
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class HarborC7nRepoVo {
    @Encrypt
    @ApiModelProperty("仓库ID")
    private Long repoId;

    @ApiModelProperty("仓库名称")
    private String repoName;

    @ApiModelProperty("仓库类型")
    private String repoType;

    public HarborC7nRepoVo() {
    }

    public HarborC7nRepoVo(Long repoId, String repoName, String repoType) {
        this.repoId = repoId;
        this.repoName = repoName;
        this.repoType = repoType;
    }

    public Long getRepoId() {
        return repoId;
    }

    public void setRepoId(Long repoId) {
        this.repoId = repoId;
    }

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public String getRepoType() {
        return repoType;
    }

    public void setRepoType(String repoType) {
        this.repoType = repoType;
    }
}
