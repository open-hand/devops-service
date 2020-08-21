//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.kubernetes.client.models;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ApiModel(
    description = "A node selector represents the union of the results of one or more label queries over a set of nodes; that is, it represents the OR of the selectors represented by the node selector terms."
)
public class V1NodeSelector {
    public static final String SERIALIZED_NAME_NODE_SELECTOR_TERMS = "nodeSelectorTerms";
    @SerializedName("nodeSelectorTerms")
    private List<V1NodeSelectorTerm> nodeSelectorTerms = new ArrayList();

    public V1NodeSelector() {
    }

    public V1NodeSelector nodeSelectorTerms(List<V1NodeSelectorTerm> nodeSelectorTerms) {
        this.nodeSelectorTerms = nodeSelectorTerms;
        return this;
    }

    public V1NodeSelector addNodeSelectorTermsItem(V1NodeSelectorTerm nodeSelectorTermsItem) {
        this.nodeSelectorTerms.add(nodeSelectorTermsItem);
        return this;
    }

    @ApiModelProperty(
        required = true,
        value = "Required. A list of node selector terms. The terms are ORed."
    )
    public List<V1NodeSelectorTerm> getNodeSelectorTerms() {
        return this.nodeSelectorTerms;
    }

    public void setNodeSelectorTerms(List<V1NodeSelectorTerm> nodeSelectorTerms) {
        this.nodeSelectorTerms = nodeSelectorTerms;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            V1NodeSelector v1NodeSelector = (V1NodeSelector)o;
            return Objects.equals(this.nodeSelectorTerms, v1NodeSelector.nodeSelectorTerms);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.nodeSelectorTerms});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class V1NodeSelector {\n");
        sb.append("    nodeSelectorTerms: ").append(this.toIndentedString(this.nodeSelectorTerms)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}
