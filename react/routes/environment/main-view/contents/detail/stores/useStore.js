import { useLocalStore } from 'mobx-react-lite';
import { axios } from '@choerodon/master';
import { handlePromptError } from '../../../../../../utils';

export default function useStore({ SYNC_TAB }) {
  return useLocalStore(() => ({
    tabKey: SYNC_TAB,

    setTabKey(data) {
      this.tabKey = data;
    },
    get getTabKey() {
      return this.tabKey;
    },

    value: '',
    setValue(value) {
      this.value = value;
    },
    get getValue() {
      return this.value;
    },

    async loadValue(projectId, id) {
      try {
        const res = await axios.get(`/devops/v1/projects/${projectId}/app_service_versions/value?app_service_id=${id}`);
        if (handlePromptError(res)) {
          this.setValue(res);
        }
      } catch (e) {
        Choerodon.handleResponseError(e);
      }
    },
    checkDelete(projectId, id) {
      return axios.get(`/devops/v1/projects/${projectId}/deploy_value/check_delete?value_id=${id}`);
    },
  }));
}
