package io.choerodon.devops.api.vo.hrdsCode;

import java.util.List;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;

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

	public class HarborC7nImageTagVo{
		@ApiModelProperty("TAG名称")
		@SerializedName("name")
		private String tagName;

		@ApiModelProperty("最新push时间")
		@SerializedName("push_time")
		private String pushTime;

		@ApiModelProperty("pull命令")
		private String pullCmd;

		public String getTagName() {
			return tagName;
		}

		public void setTagName(String tagName) {
			this.tagName = tagName;
		}

		public String getPushTime() {
			return pushTime;
		}

		public void setPushTime(String pushTime) {
			this.pushTime = pushTime;
		}

		public String getPullCmd() {
			return pullCmd;
		}

		public void setPullCmd(String pullCmd) {
			this.pullCmd = pullCmd;
		}
	}

	public HarborC7nRepoImageTagVo(){}
	public HarborC7nRepoImageTagVo(String repoType, String harborUrl, String pullAccount, String pullPassword, List<HarborC7nImageTagVo> imageTagList) {
		this.repoType = repoType;
		this.harborUrl = harborUrl;
		this.pullAccount = pullAccount;
		this.pullPassword = pullPassword;
		this.imageTagList = imageTagList;
	}
}
