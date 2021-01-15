package io.choerodon.devops.api.vo.harbor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;


/**
 * description
 *
 * @author chenxiuhong 2020/04/24 11:37 上午
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class HarborImageTagVo {

	@ApiModelProperty("摘要")
	private String digest;

	@ApiModelProperty("TAG名称")
	@SerializedName("name")
	private String tagName;

	@ApiModelProperty("TAG大小：102400")
	private Integer size;

	@ApiModelProperty("架构")
	private String architecture;

	@ApiModelProperty("操作系统")
	private String os;

	@ApiModelProperty("操作系统版本")
	@SerializedName("os.version")
	private String osVersion;

	@ApiModelProperty("docker版本")
	@SerializedName("docker_version")
	private String dockerVersion;

	@ApiModelProperty("创建人")
	private String author;

	@ApiModelProperty("创建时间")
	@SerializedName("created")
	private String createTime;

	@ApiModelProperty("最新push时间")
	@SerializedName("push_time")
	private String pushTime;

	@ApiModelProperty("最近pull时间")
	@SerializedName("pull_time")
	private String pullTime;

	@ApiModelProperty("TAG大小显示：19MB")
	private String sizeDesc;

	@ApiModelProperty("登录名")
	private String loginName;

	@ApiModelProperty("用户姓名")
	private String realName;

	@ApiModelProperty("用户头像地址")
	private String userImageUrl;

	public String getDigest() {
		return digest;
	}

	public void setDigest(String digest) {
		this.digest = digest;
	}

	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	public String getArchitecture() {
		return architecture;
	}

	public void setArchitecture(String architecture) {
		this.architecture = architecture;
	}

	public String getOs() {
		return os;
	}

	public void setOs(String os) {
		this.os = os;
	}

	public String getOsVersion() {
		return osVersion;
	}

	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}

	public String getDockerVersion() {
		return dockerVersion;
	}

	public void setDockerVersion(String dockerVersion) {
		this.dockerVersion = dockerVersion;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getPushTime() {
		return pushTime;
	}

	public void setPushTime(String pushTime) {
		this.pushTime = pushTime;
	}

	public String getPullTime() {
		return pullTime;
	}

	public void setPullTime(String pullTime) {
		this.pullTime = pullTime;
	}

	public String getSizeDesc() {
		return sizeDesc;
	}

	public void setSizeDesc(String sizeDesc) {
		this.sizeDesc = sizeDesc;
	}

	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public String getUserImageUrl() {
		return userImageUrl;
	}

	public void setUserImageUrl(String userImageUrl) {
		this.userImageUrl = userImageUrl;
	}
}
