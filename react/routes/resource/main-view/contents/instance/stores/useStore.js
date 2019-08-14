import { useLocalStore } from 'mobx-react-lite';
import { axios } from '@choerodon/master';
import { handlePromptError } from '../../../../../../utils';

export default function useStore() {
  return useLocalStore(() => ({
    tabKey: 'cases',
    detailLoading: false,
    detail: {},
    valueLoading: true,
    upgradeValue: {},

    setTabKey(data) {
      this.tabKey = data;
    },
    get getTabKey() {
      return this.tabKey;
    },
    setUpgradeValue(value) {
      this.upgradeValue = value;
    },
    get getUpgradeValue() {
      return this.upgradeValue;
    },
    setValueLoading(data) {
      this.valueLoading = data;
    },
    get getValueLoading() {
      return this.valueLoading;
    },

    redeploy(projectId, id) {
      return axios.put(`/devops/v1/projects/${projectId}/app_service_instances/${id}/restart`);
    },

    upgrade(projectId, data) {
      return axios.post(`/devops/v1/projects/${projectId}/app_service_instances`, JSON.stringify(data));
    },

    async loadValue(projectId, id, versionId) {
      this.setValueLoading(true);
      try {
        const data = await axios.get(`/devops/v1/projects/${projectId}/app_service_instances/${id}/appServiceService/${versionId}/upgrade_value`);
        const result = handlePromptError(data);
        this.setValueLoading(false);
        if (result) {
          this.setUpgradeValue(data);
          return true;
        }
        return false;
      } catch (e) {
        this.setValueLoading(false);
        Choerodon.handleResponseError(e);
        return false;
      }
    },

    loadUpVersion({ projectId, appId, page, param = '', initId = '' }) {
      return axios.get(
        `/devops/v1/projects/${projectId}/app_service_versions/page_by_app_service/${appId}?page=${page}&app_version_id=${initId}&version=${param}&size=15`,
      );
    },
  }));
}
