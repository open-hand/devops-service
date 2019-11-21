import { useLocalStore } from 'mobx-react-lite';
import { axios, Choerodon } from '@choerodon/boot';
import { handlePromptError } from '../../../../../../utils';

export default function useStore({ NODE_TAB }) {
  return useLocalStore(() => ({
    tabKey: NODE_TAB,

    setTabKey(data) {
      this.tabKey = data;
    },
    get getTabKey() {
      return this.tabKey;
    },

    grafanaUrl: null,
    setGrafanaUrl(data) {
      this.grafanaUrl = data;
    },
    get getGrafanaUrl() {
      return this.grafanaUrl;
    },

    componentList: [],
    setComponentList(data) {
      this.componentList = data;
    },
    get getComponentList() {
      return this.componentList;
    },

    prometheusStatus: [],
    setPrometheusStatus(data) {
      this.prometheusStatus = data;
    },
    get getPrometheusStatus() {
      return this.prometheusStatus;
    },

    async loadGrafanaUrl(projectId, clusterId) {
      try {
        const res = await axios.get(`/devops/v1/projects/${projectId}/cluster_resource/grafana_url?cluster_id=${clusterId}&type=cluster`);
        if (handlePromptError(res)) {
          this.setGrafanaUrl(res);
        }
      } catch (e) {
        Choerodon.handleResponseError(e);
      }
    },

    async loadComponentList(projectId, clusterId) {
      try {
        const res = await axios.get(`/devops/v1/projects/${projectId}/cluster_resource?cluster_id=${clusterId}`);
        if (handlePromptError(res)) {
          this.setComponentList(res);
        }
      } catch (e) {
        Choerodon.handleResponseError(e);
      }
    },

    async installCertManager(projectId, clusterId) {
      try {
        const res = await axios.post(`/devops/v1/projects/${projectId}/cluster_resource/cert_manager/deploy?cluster_id=${clusterId}`);
        return handlePromptError(res);
      } catch (e) {
        Choerodon.handleResponseError(e);
        return false;
      }
    },

    checkUninstallCert(projectId, clusterId) {
      return axios.get(`/devops/v1/projects/${projectId}/cluster_resource/cert_manager/check?cluster_id=${clusterId}`);
    },

    async uninstallCert(projectId, clusterId) {
      try {
        const res = await axios.delete(`/devops/v1/projects/${projectId}/cluster_resource/cert_manager/unload?cluster_id=${clusterId}`);
        return handlePromptError(res);
      } catch (e) {
        Choerodon.handleResponseError(e);
        return false;
      }
    },

    async uninstallMonitor(projectId, clusterId) {
      try {
        const res = await axios.delete(`/devops/v1/projects/${projectId}/cluster_resource/prometheus/unload?cluster_id=${clusterId}`);
        return handlePromptError(res);
      } catch (e) {
        Choerodon.handleResponseError(e);
        return false;
      }
    },

    async loadPrometheusStatus(projectId, clusterId) {
      try {
        const res = await axios.get(`/devops/v1/projects/${projectId}/cluster_resource/prometheus/deploy_status?cluster_id=${clusterId}`);
        if (handlePromptError(res)) {
          this.setPrometheusStatus(res);
        }
      } catch (e) {
        Choerodon.handleResponseError(e);
      }
    },
  }));
}
