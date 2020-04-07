import { useLocalStore } from 'mobx-react-lite';
import { axios } from '@choerodon/boot';
import { handlePromptError } from '../../../../../../utils';

export default function useStore() {
  return useLocalStore(() => ({
    data: [],
    loading: false,
    loadCodeQualityData() {
      this.loading = true;
      // return axios.get(`/devops/v1/projects/${projectId}/app_service/${appServiceId}/sonarqube`)
      //   .then((data) => {
      //     this.loading = false;
      //     const res = handlePromptError(data);
      //     if (res) {
      //       this.data = data;
      //     }
      //   });
    },
  }));
}
