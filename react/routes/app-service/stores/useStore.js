import { useLocalStore } from 'mobx-react-lite';
import { axios } from '@choerodon/master';
import map from 'lodash/map';
import { handlePromptError } from '../../../utils';

export default function useStore() {
  return useLocalStore(() => ({

    appServiceId: null,
    setAppServiceId(data) {
      this.appServiceId = data;
    },
    get getAppServiceId() {
      return this.appServiceId;
    },

    loadAppById(projectId, id) {
      return axios.get(`/devops/v1/projects/${projectId}/app_service/${id}`);
    },

    checkChart(projectId, url) {
      return axios.get(`/devops/v1/projects/${projectId}/app_service/check_chart?url=${url}`);
    },

    checkHarbor(projectId, postData) {
      let url = '';
      map(postData, (value, key) => {
        url = `${url}&${key}=${value}`;
      });

      return axios.get(`/devops/v1/projects/${projectId}/app_service/check_harbor?${url}`);
    },

    async changeActive(projectId, id, active) {
      try {
        const res = await axios.get(`/devops/v1/projects/${projectId}/app_service/${id}?active=${active}`);
        return handlePromptError(res);
      } catch (e) {
        Choerodon.handleResponseError(e);
      }
    },

    batchCheck(projectId, listCode, listName) {
      return axios.post(`/devops/v1/projects/${projectId}/app_service/batch_check`, JSON.stringify({ listCode, listName }));
    },
  }));
}
