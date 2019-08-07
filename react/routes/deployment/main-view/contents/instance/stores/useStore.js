import { useLocalStore } from 'mobx-react-lite';
import { axios } from '@choerodon/boot';
import { handlePromptError } from '../../../../../../utils';

export default function useStore() {
  return useLocalStore(() => ({
    tabKey: 'cases',
    detailLoading: false,
    detail: {},

    setTabKey(data) {
      this.tabKey = data;
    },
    get getTabKey() {
      return this.tabKey;
    },
    redeploy(projectId, id) {
      axios.put(`/devops/v1/projects/${projectId}/app_service_instances/${id}/restart`);
    },
    setDetailLoading(data) {
      this.detailLoading = data;
    },
    get getDetailLoading() {
      return this.detailLoading;
    },
    setDetail(data) {
      this.detail = data;
    },
    get getDetail() {
      return this.detail;
    },

    async detailFetch(projectId, id) {
      this.setDetailLoading(true);
      try {
        const detail = await axios.get(`/devops/v1/projects/${projectId}/app_service_instances/${id}`);
        if (handlePromptError(detail)) {
          this.setDetail(detail);
        }
        this.setDetailLoading(false);
      } catch (e) {
        this.setDetailLoading(false);
      }
    },
  }));
}
