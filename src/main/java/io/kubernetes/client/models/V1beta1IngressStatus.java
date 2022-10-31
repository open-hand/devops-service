//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package io.kubernetes.client.models;

import com.google.gson.annotations.SerializedName;
import io.kubernetes.client.openapi.models.V1LoadBalancerStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

@ApiModel(
    description = "IngressStatus describe the current state of the Ingress."
)
public class V1beta1IngressStatus {
    @SerializedName("loadBalancer")
    private V1LoadBalancerStatus loadBalancer = null;

    public V1beta1IngressStatus() {
    }

    public V1beta1IngressStatus loadBalancer(V1LoadBalancerStatus loadBalancer) {
        this.loadBalancer = loadBalancer;
        return this;
    }

    @ApiModelProperty("LoadBalancer contains the current status of the load-balancer.")
    public V1LoadBalancerStatus getLoadBalancer() {
        return this.loadBalancer;
    }

    public void setLoadBalancer(V1LoadBalancerStatus loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            V1beta1IngressStatus v1beta1IngressStatus = (V1beta1IngressStatus)o;
            return Objects.equals(this.loadBalancer, v1beta1IngressStatus.loadBalancer);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.loadBalancer});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class V1beta1IngressStatus {\n");
        sb.append("    loadBalancer: ").append(this.toIndentedString(this.loadBalancer)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}
