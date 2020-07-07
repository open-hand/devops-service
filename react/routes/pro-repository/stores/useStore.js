import { useLocalStore } from 'mobx-react-lite';
import { axios } from '@choerodon/boot';
import map from 'lodash/map';

export default function useStore() {
  return useLocalStore(() => ({

    checkChart(projectId, data) {
      return axios.post(`/devops/v1/projects/${projectId}/project_config/check_chart`, JSON.stringify(data));
    },

    checkHarbor(projectId, postData) {
      let url = '';
      map(postData, (value, key) => {
        value && (url = `${url}&${key}=${value}`);
      });

      return axios.get(`/devops/v1/projects/${projectId}/project_config/check_harbor?${url.substr(1)}`);
    },

  }));
}
