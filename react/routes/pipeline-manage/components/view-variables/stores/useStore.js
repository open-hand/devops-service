import { useLocalStore } from 'mobx-react-lite';
import { axios, Choerodon } from '@choerodon/boot';

export default function useStore() {
  return useLocalStore(() => ({
    async loadData(projectId, appServiceId) {
      try {
        const urlParams = appServiceId ? `app_service_id=${appServiceId}&level=app` : 'level=project';
        const res = await axios.get(`devops/v1/projects/${projectId}/ci_variable/keys?${urlParams}`);
        if (res && !res.failed) {
          return res;
        } else {
          return false;
        }
      } catch (e) {
        return false;
      }
    },
  }));
}
