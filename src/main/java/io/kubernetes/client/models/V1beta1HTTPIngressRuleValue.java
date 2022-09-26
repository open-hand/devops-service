//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package io.kubernetes.client.models;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ApiModel(
    description = "HTTPIngressRuleValue is a list of http selectors pointing to backends. In the example: http://<host>/<path>?<searchpart> -> backend where where parts of the url correspond to RFC 3986, this resource will be used to match against everything after the last '/' and before the first '?' or '#'."
)
public class V1beta1HTTPIngressRuleValue {
    @SerializedName("paths")
    private List<V1beta1HTTPIngressPath> paths = new ArrayList();

    public V1beta1HTTPIngressRuleValue() {
    }

    public V1beta1HTTPIngressRuleValue paths(List<V1beta1HTTPIngressPath> paths) {
        this.paths = paths;
        return this;
    }

    public V1beta1HTTPIngressRuleValue addPathsItem(V1beta1HTTPIngressPath pathsItem) {
        this.paths.add(pathsItem);
        return this;
    }

    @ApiModelProperty(
        required = true,
        value = "A collection of paths that map requests to backends."
    )
    public List<V1beta1HTTPIngressPath> getPaths() {
        return this.paths;
    }

    public void setPaths(List<V1beta1HTTPIngressPath> paths) {
        this.paths = paths;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            V1beta1HTTPIngressRuleValue v1beta1HTTPIngressRuleValue = (V1beta1HTTPIngressRuleValue)o;
            return Objects.equals(this.paths, v1beta1HTTPIngressRuleValue.paths);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.paths});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class V1beta1HTTPIngressRuleValue {\n");
        sb.append("    paths: ").append(this.toIndentedString(this.paths)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}
