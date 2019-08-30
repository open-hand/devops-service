import { useLocalStore } from 'mobx-react-lite';
import { axios } from '@choerodon/master';
import { handlePromptError } from '../../../../../../../../utils';

export default function useStore() {
  return useLocalStore(() => ({
    loading: true,
    value: '',
    setValue(value) {
      this.value = value;
    },
    get getValue() {
      return this.value;
    },
    setLoading(flag) {
      this.loading = flag;
    },
    get getLoading() {
      return this.loading;
    },

    async loadValue(projectId, id) {
      this.setLoading(true);
      try {
        const res = await axios.get(`/devops/v1/projects/${projectId}/app_service_versions/value?app_service_id=${id}`);
        if (handlePromptError(res)) {
          this.setValue(res);
        }
      } catch (e) {
        Choerodon.handleResponseError(e);
      } finally {
        this.setLoading(false);
      }
    },
  }));
}
