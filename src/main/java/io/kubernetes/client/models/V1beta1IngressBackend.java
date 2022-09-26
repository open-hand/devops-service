//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package io.kubernetes.client.models;

import com.google.gson.annotations.SerializedName;
import io.kubernetes.client.custom.IntOrString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

@ApiModel(
    description = "IngressBackend describes all endpoints for a given service and port."
)
public class V1beta1IngressBackend {
    @SerializedName("serviceName")
    private String serviceName = null;
    @SerializedName("servicePort")
    private IntOrString servicePort = null;

    public V1beta1IngressBackend() {
    }

    public V1beta1IngressBackend serviceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    @ApiModelProperty(
        required = true,
        value = "Specifies the name of the referenced service."
    )
    public String getServiceName() {
        return this.serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public V1beta1IngressBackend servicePort(IntOrString servicePort) {
        this.servicePort = servicePort;
        return this;
    }

    @ApiModelProperty(
        required = true,
        value = "Specifies the port of the referenced service."
    )
    public IntOrString getServicePort() {
        return this.servicePort;
    }

    public void setServicePort(IntOrString servicePort) {
        this.servicePort = servicePort;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            V1beta1IngressBackend v1beta1IngressBackend = (V1beta1IngressBackend)o;
            return Objects.equals(this.serviceName, v1beta1IngressBackend.serviceName) && Objects.equals(this.servicePort, v1beta1IngressBackend.servicePort);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.serviceName, this.servicePort});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class V1beta1IngressBackend {\n");
        sb.append("    serviceName: ").append(this.toIndentedString(this.serviceName)).append("\n");
        sb.append("    servicePort: ").append(this.toIndentedString(this.servicePort)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}
