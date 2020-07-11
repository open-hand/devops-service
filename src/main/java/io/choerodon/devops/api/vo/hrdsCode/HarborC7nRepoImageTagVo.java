package io.choerodon.devops.api.vo.hrdsCode;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.devops.api.vo.HarborC7nImageTagVo;

/**
 * 返回给猪齿鱼
 *
 * @author chenxiuhong 2020/04/24 11:37 上午
 */
public class HarborC7nRepoImageTagVo {

	@ApiModelProperty("仓库类型")
	private String repoType;

	@ApiModelProperty("url")
	private String harborUrl;

	@ApiModelProperty("拉取账号")
	private String pullAccount;

	@ApiModelProperty("拉取密码")
	private String pullPassword;

	@ApiModelProperty("镜像版本列表")
	private List<HarborC7nImageTagVo> imageTagList;

	public String getRepoType() {
		return repoType;
	}

	public void setRepoType(String repoType) {
		this.repoType = repoType;
	}

	public String getHarborUrl() {
		return harborUrl;
	}

	public void setHarborUrl(String harborUrl) {
		this.harborUrl = harborUrl;
	}

	public String getPullAccount() {
		return pullAccount;
	}

	public void setPullAccount(String pullAccount) {
		this.pullAccount = pullAccount;
	}

	public String getPullPassword() {
		return pullPassword;
	}

	public void setPullPassword(String pullPassword) {
		this.pullPassword = pullPassword;
	}

	public List<HarborC7nImageTagVo> getImageTagList() {
		return imageTagList;
	}

	public void setImageTagList(List<HarborC7nImageTagVo> imageTagList) {
		this.imageTagList = imageTagList;
	}
}
