import { useLocalStore } from 'mobx-react-lite';
import { axios } from '@choerodon/boot';
import { handlePromptError } from '../../../../../../utils';

export default function useStore() {
  return useLocalStore(() => ({
    data: [],
    loading: false,

    isEmpty: false,
    setIsEmpty(flag) {
      this.isEmpty = flag;
    },
    get getIsEmpty() {
      return this.isEmpty;
    },

    loadCodeQualityData(projectId, appServiceId) {
      this.loading = true;
      return axios.get(`/devops/v1/projects/${projectId}/app_service/${appServiceId}/sonarqube`)
        .then((data) => {
          this.loading = false;
          if (!data || (data && data.status === 204)) {
            this.setIsEmpty(true);
          } else {
            const res = handlePromptError(data);
            if (res) {
              this.data = data;
            }
          }
        });
    },
  }));
}
