package io.choerodon.devops.infra.enums.jenkins;


/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/12/15 16:04
 */
public enum JenkinsJobTypeEnum {

    /**
     * 文件夹
     */
    FOLDER("com.cloudbees.hudson.plugins.folder.Folder", "Folder"),
    WORKFLOW_MULTI_BRANCH_PROJECT("org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject", "WorkflowMultiBranchProject"),
    WORKFLOW_JOB("org.jenkinsci.plugins.workflow.job.WorkflowJob", "WorkflowJob"),
    ORGANIZATION_FOLDER("jenkins.branch.OrganizationFolder", "OrganizationFolder"),
    FREE_STYLE_PROJECT("hudson.model.FreeStyleProject", "FreeStyleProject");

    private final String type;
    private String className;

    JenkinsJobTypeEnum(String className, String value) {
        this.className = className;
        this.type = value;
    }

    public static String getTypeByClassName(String className) {
        for (JenkinsJobTypeEnum value : JenkinsJobTypeEnum.values()) {
            if (value.className().equals(className)) {
                return value.type;
            }
        }
        return "unknow";
    }

    public String className() {
        return className;
    }


}
