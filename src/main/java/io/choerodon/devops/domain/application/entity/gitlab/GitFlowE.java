package io.choerodon.devops.domain.application.entity.gitlab;

import java.util.HashMap;
import java.util.Map;

/**
 * GitFlow 对象
 *
 * @author zhipeng.zuo
 */
public class GitFlowE {

    private static final String TYPE_FEATURE = "feature分支";
    private static final String TYPE_RELEASE = "release分支";
    private static final String TYPE_HOTFIX = "hotfix分支";
    private static final String TYPE_MAIN = "主分支";
    private static final String TYPE_DEV = "开发分支";
    private static final String TYPE_UNKNOWN = "未知分支";


    private static final String BRANCH_MASTER = "master";
    private static final String BRANCH_DEV = "develop";
    private static final String FEATURE_PREFIX = "feature";
    private static final String RELEASE_PREFIX = "release";
    private static final String HOTFIX_PREFIX = "hotfix";
    private static Map<String, String> branchTypes = new HashMap<>();

    static {
        branchTypes.put(BRANCH_DEV, TYPE_DEV);
        branchTypes.put(BRANCH_MASTER, TYPE_MAIN);
        branchTypes.put(FEATURE_PREFIX, TYPE_FEATURE);
        branchTypes.put(RELEASE_PREFIX, TYPE_RELEASE);
        branchTypes.put(HOTFIX_PREFIX, TYPE_HOTFIX);
    }

    private String name;
    private String issue;
    private String type;
    private CommitE commit;

    /**
     * 根据 branch name 构造 GitFlow
     *
     * @param name   分支名称
     * @param commit 提交
     */
    public GitFlowE(String name, CommitE commit) {
        String[] nameUnit = name.split("-");
        Object branchType = branchTypes.get(nameUnit[0]);
        this.name = name;
        this.issue = nameUnit.length == 2 && nameUnit[1].matches("\\d+$") ? nameUnit[1] : "";
        this.commit = commit;
        this.type = branchType == null ? TYPE_UNKNOWN : branchType.toString();
    }

    public CommitE getCommit() {
        return commit;
    }

    public void setCommit(CommitE commit) {
        this.commit = commit;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIssue() {
        return issue;
    }

    public void setIssue(String issue) {
        this.issue = issue;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "GitFlow { " + "name='" + name + '\'' + ", issue='" + issue + '\'' + ", type='" + type + '\'' + '}';
    }
}
