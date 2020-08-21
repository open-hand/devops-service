package io.choerodon.devops.infra.constant;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;

/**
 * description
 *
 * @author chenxiuhong 2020/04/21 9:58 上午
 */
public interface HarborConstants {

	String Y = "Y";

	String TRUE = "true";

	String FALSE = "false";

	String KB = "KB";

	String MB = "MB";

	String GB = "GB";

	String TB = "TB";

	String DEFAULT_PASSWORD = "Abcd1234";

	String ASSIGN_AUTH = "assign";

	String UPDATE_AUTH = "update";

	String REVOKE_AUTH = "revoke";

	String LOWER_CREATE = "create";

	String HARBOR_UI = "harbor-ui";

	String DEFAULT_DATE = "0001-01-01T00:00:00Z";

	/**
	* 危害等级
	* */
	interface SeverityLevel{

		String LOW = "low";

		String MEDIUM = "medium";

		String HIGH = "high";

		String CRITICAL = "critical";
	}

	interface HarborSagaCode{
		/**
		* 创建Docker仓库
		* 创建用户、创建镜像仓库、保存存储容量配置、保存CVE白名单、保存项目到数据库
		* */
		String CREATE_PROJECT = "rdupm-docker-repo-create";

		String CREATE_PROJECT_USER = "rdupm-docker-repo-create.user";

		String CREATE_PROJECT_REPO = "rdupm-docker-repo-create.repo";

		String CREATE_PROJECT_QUOTA = "rdupm-docker-repo-create.quota";

		String CREATE_PROJECT_CVE = "rdupm-docker-repo-create.cve";

		String CREATE_PROJECT_DB = "rdupm-docker-repo-create.db";

		String CREATE_PROJECT_AUTH = "rdupm-docker-repo-create.auth";

		String ROBOT_SAGA_TASK_CODE = "rdupm-docker-robot-create";

		/**
		* 更新Docke仓库
		* */
		String UPDATE_PROJECT = "rdupm-docker-repo-update";

		String UPDATE_PROJECT_REPO = "rdupm-docker-repo-update.repo";

		String UPDATE_PROJECT_QUOTA = "rdupm-docker-repo-update.quota";

		String UPDATE_PROJECT_CVE = "rdupm-docker-repo-update.cve";

		String UPDATE_PROJECT_DB = "rdupm-docker-repo-update.db";

		/**
		* 分配权限
		* */
		String CREATE_AUTH = "rdupm-docker-auth-create";

		String CREATE_AUTH_USER = "rdupm-docker-auth-create.user";

		String CREATE_AUTH_AUTH = "rdupm-docker-auth-create.auth";

		String CREATE_AUTH_DB = "rdupm-docker-auth-create.db";

		String UPDATE_PWD = "rdupm-docker-user-update";

		String UPDATE_PWD_HARBOR = "rdupm-docker-user-update.harbor";

		String UPDATE_PWD_NEXUS = "rdupm-docker-user-update.nexus";

        /**
         * 创建自定义镜像仓库
         * */
        String CREATE_CUSTOMIZE_REPOSITORY = "rdupm-docker-customize-repository-create";

        String CREATE_HARBOR_REGISTRY = "rdupm-docker-harbor-registry-create";
	}

	enum HarborApiEnum{

		COUNT("/api/statistics", HttpMethod.GET,"获取所有项目数量、镜像数量"),

		SEARCH("/api/search", HttpMethod.GET,"获取所有项目、所有镜像"),

		/**
		* 用户API
		* */
		CREATE_USER("/api/users", HttpMethod.POST,"创建用户"),

		SELECT_USER_BY_USERNAME("/api/users/search", HttpMethod.GET,"根据用户名查询用户信息"),

		/**
		* 项目API
		* */
		CREATE_PROJECT("/api/projects", HttpMethod.POST,"创建项目"),

		DETAIL_PROJECT("/api/projects/%s", HttpMethod.GET,"查询项目详情-项目ID"),

		DELETE_PROJECT("/api/projects/%s", HttpMethod.DELETE,"删除项目-项目ID"),

		UPDATE_PROJECT("/api/projects/%s", HttpMethod.PUT,"修改项目-项目ID"),

		CHECK_PROJECT_NAME("/api/projects", HttpMethod.HEAD,"检查项目名称是否存在"),

		LIST_PROJECT("/api/projects", HttpMethod.GET,"查询项目列表"),

		/**
		 * 元数据API
		 *
		 * */
		GET_PROJECT_METADATA("/api/projects/%s/metadatas/%s", HttpMethod.GET,"根据项目ID和元数据名称 获取元数据值"),

		UPDATE_PROJECT_METADATA("/api/projects/%s/metadatas/%s", HttpMethod.PUT,"根据项目ID和元数据名称 更新元数据值"),    //有问题

		DELETE_PROJECT_METADATA("/api/projects/%s/metadatas/%s", HttpMethod.DELETE,"根据项目ID和元数据名称 更新元数据值"), //有问题

		/**
		* 项目概览
		* */
		GET_PROJECT_SUMMARY("/api/projects/%s/summary", HttpMethod.GET,"获取存储容量使用情况--项目ID"),

		/**
		* 项目资源API
		* */
		GET_PROJECT_QUOTA("/api/quotas/%s", HttpMethod.GET,"获取项目资源使用情况--项目ID"),

		UPDATE_PROJECT_QUOTA("/api/quotas/%s", HttpMethod.PUT,"更新项目资源配额--项目ID"),

		UPDATE_GLOBAL_QUOTA("/api/configurations", HttpMethod.PUT,"全局更新项目资源配额"),

		GET_GLOBAL_QUOTA("/api/configurations", HttpMethod.GET,"获得全局资源配额"),

		/**
		* 镜像API
		* */
		LIST_IMAGE("/api/repositories", HttpMethod.GET,"查询镜像列表"),

		UPDATE_IMAGE_DESC("/api/repositories/%s", HttpMethod.PUT,"更新镜像描述--  仓库名/镜像名称"),

		DELETE_IMAGE("/api/repositories/%s", HttpMethod.DELETE,"删除镜像-- 仓库名/镜像名称"),

		/**
		* 镜像TAG API
		* */
		GET_IMAGE_BUILD_LOG("/api/repositories/%s/tags/%s/manifest", HttpMethod.GET,"获取构建日志-- 仓库名/镜像名称、版本号"),

		DETAIL_IMAGE_TAG("/api/repositories/%s/tags/%s", HttpMethod.GET,"获取镜像TAG明细-- 仓库名/镜像名称、版本号"),

		DELETE_IMAGE_TAG("/api/repositories/%s/tags/%s", HttpMethod.DELETE,"删除镜像TAG-- 仓库名/镜像名称、版本号"),

		LIST_IMAGE_TAG("/api/repositories/%s/tags", HttpMethod.GET,"获取镜像TAG列表-- 仓库名/镜像名称"),

		COPY_IMAGE_TAG("/api/repositories/%s/tags", HttpMethod.POST,"复制镜像TAG-- 仓库名/镜像名称"),

		/**
		* 获取项目用户
		* */
		LIST_AUTH("/api/projects/%s/members", HttpMethod.GET,"获取项目中权限列表--项目ID"),

		GET_ONE_AUTH("/api/projects/%s/members/%s", HttpMethod.GET,"获取项目中某个用户的权限情况--项目ID、harbor中权限ID"),

		DELETE_ONE_AUTH("/api/projects/%s/members/%s", HttpMethod.DELETE,"删除项目中某个用户的权限情况--项目ID、harbor中权限ID"),

		UPDATE_ONE_AUTH("/api/projects/%s/members/%s", HttpMethod.PUT,"修改项目中某个用户的权限情况--项目ID、harbor中权限ID"),

		CREATE_ONE_AUTH("/api/projects/%s/members", HttpMethod.POST,"分配项目中某个用户的权限情况--项目ID"),

		/**
		* 日志API
		* */
		LIST_LOGS_PROJECT("/api/projects/%s/logs", HttpMethod.GET,"查询项目日志-项目ID"),

		LIST_LOGS("/api/logs", HttpMethod.GET,"查询全局日志"),

		/**
		 * 修改密码
		 * */
		CHANGE_PASSWORD("/api/users/%s/password", HttpMethod.PUT,"修改用户密码-Harbor用户ID"),

		/**
		 * 机器人账户API
		 * */
		CREATE_ROBOT("/api/projects/%s/robots", HttpMethod.POST, "创建机器人账户-项目ID"),

		GET_PROJECT_ALL_ROBOTS("/api/projects/%s/robots", HttpMethod.GET, "查询项目的所有机器人账户-项目ID"),

		GET_ONE_ROBOT("/api/projects/%s/robots/%s", HttpMethod.GET, "查询指定ID的机器人账户-项目ID、机器人账户ID"),

		DELETE_ROBOT("/api/projects/%s/robots/%s", HttpMethod.DELETE, "删除指定ID的机器人账户-项目ID、机器人账户ID"),

        /**
         * 自定义仓库API
         * */
        CURRENT_USER("/api/users/current", HttpMethod.GET, "查询当前用户信息"),

        GET_SYSTEM_INFO("/api/systeminfo", HttpMethod.GET, "查询当前系统信息");

		String apiUrl;

		HttpMethod httpMethod;

		String apiDesc;

		public String getApiUrl() {
			return apiUrl;
		}

		public void setApiUrl(String apiUrl) {
			this.apiUrl = apiUrl;
		}

		public HttpMethod getHttpMethod() {
			return httpMethod;
		}

		public void setHttpMethod(HttpMethod httpMethod) {
			this.httpMethod = httpMethod;
		}

		public String getApiDesc() {
			return apiDesc;
		}

		public void setApiDesc(String apiDesc) {
			this.apiDesc = apiDesc;
		}

		HarborApiEnum(String apiUrl, HttpMethod method, String apiDesc) {
			this.apiUrl = apiUrl;
			this.httpMethod = method;
			this.apiDesc = apiDesc;
		}

	}

	enum HarborRoleEnum{
		PROJECT_ADMIN(1L,"projectAdmin","仓库管理员"),
		DEVELOPER(2L,"developer","开发人员"),
		GUEST(3L,"guest","访客"),
		MASTER(4L,"master","维护人员"),
		LIMITED_GUEST(5L,"limitedGuest","受限访客");

		Long roleId;

		String roleValue;

		String roleName;

		public static Long getIdByName(String harborRoleName) {
			if(StringUtils.isEmpty(harborRoleName)){
				return null;
			}
			for (HarborRoleEnum authorityEnum : HarborRoleEnum.values()) {
				if (harborRoleName.equals(authorityEnum.getRoleName())) {
					return authorityEnum.getRoleId();
				}
			}
			return null;
		}

		public Long getRoleId() {
			return roleId;
		}

		public void setRoleId(Long roleId) {
			this.roleId = roleId;
		}

		public String getRoleValue() {
			return roleValue;
		}

		public void setRoleValue(String roleValue) {
			this.roleValue = roleValue;
		}

		public String getRoleName() {
			return roleName;
		}

		public void setRoleName(String roleName) {
			this.roleName = roleName;
		}

		HarborRoleEnum(Long roleId, String roleValue, String roleName) {
			this.roleId = roleId;
			this.roleValue = roleValue;
			this.roleName = roleName;
		}

		public static String getValueById(Long roleId){
			if(roleId == null){
				return null;
			}
			for (HarborRoleEnum authorityEnum : HarborRoleEnum.values()) {
				if (roleId.equals(authorityEnum.getRoleId())) {
					return authorityEnum.getRoleValue();
				}
			}
			return null;
		}

		public static String getNameById(Long roleId){
			if(roleId == null){
				return null;
			}
			for (HarborRoleEnum authorityEnum : HarborRoleEnum.values()) {
				if (roleId.equals(authorityEnum.getRoleId())) {
					return authorityEnum.getRoleName();
				}
			}
			return null;
		}

		public static Long getIdByValue(String value){
			if(StringUtils.isEmpty(value)){
				return null;
			}
			for (HarborRoleEnum authorityEnum : HarborRoleEnum.values()) {
				if (value.equals(authorityEnum.getRoleValue())) {
					return authorityEnum.getRoleId();
				}
			}
			return null;
		}
	}

	enum HarborImageOperateEnum{
		DELETE("delete","删除"),
		PULL("pull","拉取"),
		PUSH("push","推送");

		String operateType;

		String operateName;

		public String getOperateType() {
			return operateType;
		}

		public void setOperateType(String operateType) {
			this.operateType = operateType;
		}

		public String getOperateName() {
			return operateName;
		}

		public void setOperateName(String operateName) {
			this.operateName = operateName;
		}

		HarborImageOperateEnum(String operateType, String operateName) {
			this.operateType = operateType;
			this.operateName = operateName;
		}


		public static String getNameByValue(String value){
			for (HarborImageOperateEnum operateEnum : HarborImageOperateEnum.values()) {
				if (value.equals(operateEnum.getOperateType())) {
					return operateEnum.getOperateName();
				}
			}
			return null;
		}
	}

	interface HarborRobot{

		String ENABLE_FLAG_Y = "Y";

		String ENABLE_FLAG_N = "N";

		String ROBOT = "robot";

		String ACTION_PULL = "pull";

		String ACTION_PUSH = "push";

		String ROBOT_RESOURCE = "/project/%s/repository";

		String ROBOT_NAME_PREFIX = "robot$";
	}
}
