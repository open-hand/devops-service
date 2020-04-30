package io.choerodon.devops.infra.dto.gitlab.ci;

import java.util.List;

/**
 * https://docs.gitlab.com/ee/ci/yaml/README.html#onlyexcept-advanced
 *
 * only和except的取值
 *
 * @author zmf
 * @since 20-4-2
 */
public class OnlyExceptPolicy {
    private List<String> refs;
    private List<String> variables;
    private List<String> changes;

    public OnlyExceptPolicy() {
    }

    public OnlyExceptPolicy(List<String> refs, List<String> variables, List<String> changes) {
        this.refs = refs;
        this.variables = variables;
        this.changes = changes;
    }

    public List<String> getRefs() {
        return refs;
    }

    public void setRefs(List<String> refs) {
        this.refs = refs;
    }

    public List<String> getVariables() {
        return variables;
    }

    public void setVariables(List<String> variables) {
        this.variables = variables;
    }

    public List<String> getChanges() {
        return changes;
    }

    public void setChanges(List<String> changes) {
        this.changes = changes;
    }
}
