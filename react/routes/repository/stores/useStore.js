import { useLocalStore } from 'mobx-react-lite';
import { axios } from '@choerodon/boot';
import map from 'lodash/map';
import { handlePromptError } from '../../../utils';

export default function useStore() {
  return useLocalStore(() => ({

    checkChart(organizationId, data) {
      return axios.post(`/devops/v1/organizations/${organizationId}/organization_config/check_chart`, JSON.stringify(data));
    },

    checkHarbor(organizationId, postData) {
      let url = '';
      map(postData, (value, key) => {
        value && (url = `${url}&${key}=${value}`);
      });

      return axios.get(`/devops/v1/organizations/${organizationId}/organization_config/check_harbor?${url.substr(1)}`);
    },

  }));
}
