import { useLocalStore } from 'mobx-react-lite';
import { axios, Choerodon } from '@choerodon/boot';
import { handlePromptError } from '../../../../../../utils';

export default function useStore({ RESOURCE_TAB }) {
  return useLocalStore(() => ({
    tabKey: RESOURCE_TAB,

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

    async loadGrafanaUrl(projectId, clusterId) {
      try {
        const res = await axios.get(`/devops/v1/projects/${projectId}/cluster_resource/grafana_url?cluster_id=${clusterId}&type=node`);
        if (handlePromptError(res)) {
          this.setGrafanaUrl(res);
          return res;
        } else {
          this.setGrafanaUrl(null);
        }
      } catch (e) {
        Choerodon.handleResponseError(e);
      }
    },
  }));
}
