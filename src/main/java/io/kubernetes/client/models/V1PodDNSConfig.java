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
import javax.annotation.Nullable;

@ApiModel(
    description = "PodDNSConfig defines the DNS parameters of a pod in addition to those generated from DNSPolicy."
)
public class V1PodDNSConfig {
    public static final String SERIALIZED_NAME_NAMESERVERS = "nameservers";
    @SerializedName("nameservers")
    private List<String> nameservers = null;
    public static final String SERIALIZED_NAME_OPTIONS = "options";
    @SerializedName("options")
    private List<V1PodDNSConfigOption> options = null;
    public static final String SERIALIZED_NAME_SEARCHES = "searches";
    @SerializedName("searches")
    private List<String> searches = null;

    public V1PodDNSConfig() {
    }

    public V1PodDNSConfig nameservers(List<String> nameservers) {
        this.nameservers = nameservers;
        return this;
    }

    public V1PodDNSConfig addNameserversItem(String nameserversItem) {
        if (this.nameservers == null) {
            this.nameservers = new ArrayList();
        }

        this.nameservers.add(nameserversItem);
        return this;
    }

    @Nullable
    @ApiModelProperty("A list of DNS name server IP addresses. This will be appended to the base nameservers generated from DNSPolicy. Duplicated nameservers will be removed.")
    public List<String> getNameservers() {
        return this.nameservers;
    }

    public void setNameservers(List<String> nameservers) {
        this.nameservers = nameservers;
    }

    public V1PodDNSConfig options(List<V1PodDNSConfigOption> options) {
        this.options = options;
        return this;
    }

    public V1PodDNSConfig addOptionsItem(V1PodDNSConfigOption optionsItem) {
        if (this.options == null) {
            this.options = new ArrayList();
        }

        this.options.add(optionsItem);
        return this;
    }

    @Nullable
    @ApiModelProperty("A list of DNS resolver options. This will be merged with the base options generated from DNSPolicy. Duplicated entries will be removed. Resolution options given in Options will override those that appear in the base DNSPolicy.")
    public List<V1PodDNSConfigOption> getOptions() {
        return this.options;
    }

    public void setOptions(List<V1PodDNSConfigOption> options) {
        this.options = options;
    }

    public V1PodDNSConfig searches(List<String> searches) {
        this.searches = searches;
        return this;
    }

    public V1PodDNSConfig addSearchesItem(String searchesItem) {
        if (this.searches == null) {
            this.searches = new ArrayList();
        }

        this.searches.add(searchesItem);
        return this;
    }

    @Nullable
    @ApiModelProperty("A list of DNS search domains for host-name lookup. This will be appended to the base search paths generated from DNSPolicy. Duplicated search paths will be removed.")
    public List<String> getSearches() {
        return this.searches;
    }

    public void setSearches(List<String> searches) {
        this.searches = searches;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            V1PodDNSConfig v1PodDNSConfig = (V1PodDNSConfig)o;
            return Objects.equals(this.nameservers, v1PodDNSConfig.nameservers) && Objects.equals(this.options, v1PodDNSConfig.options) && Objects.equals(this.searches, v1PodDNSConfig.searches);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.nameservers, this.options, this.searches});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class V1PodDNSConfig {\n");
        sb.append("    nameservers: ").append(this.toIndentedString(this.nameservers)).append("\n");
        sb.append("    options: ").append(this.toIndentedString(this.options)).append("\n");
        sb.append("    searches: ").append(this.toIndentedString(this.searches)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}
