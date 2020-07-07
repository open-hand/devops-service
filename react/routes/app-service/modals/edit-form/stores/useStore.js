import { useLocalStore } from 'mobx-react-lite';
import { axios, Choerodon } from '@choerodon/boot';
import map from 'lodash/map';
import { handlePromptError } from '../../../../../utils';
import getTablePostData from '../../../../../utils/getTablePostData';

export default function useStore() {
  return useLocalStore(() => ({

    checkChart(projectId, data) {
      return axios.post(`/devops/v1/projects/${projectId}/app_service/check_chart`, JSON.stringify(data));
    },

    checkHarbor(projectId, postData) {
      let url = '';
      map(postData, (value, key) => {
        value && (url = `${url}&${key}=${value}`);
      });

      return axios.get(`/devops/v1/projects/${projectId}/app_service/check_harbor?${url.substr(1)}`);
    },
  }));
}
