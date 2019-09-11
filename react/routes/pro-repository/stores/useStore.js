import { useLocalStore } from 'mobx-react-lite';
import { axios } from '@choerodon/master';
import map from 'lodash/map';

export default function useStore() {
  return useLocalStore(() => ({

    checkChart(projectId, url) {
      return axios.get(`/devops/v1/projects/${projectId}/project_config/check_chart?url=${url}`);
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
