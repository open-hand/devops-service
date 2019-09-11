import { useLocalStore } from 'mobx-react-lite';
import { axios } from '@choerodon/master';
import map from 'lodash/map';
import { handlePromptError } from '../../../utils';

export default function useStore() {
  return useLocalStore(() => ({

    checkChart(organizationId, url) {
      return axios.get(`/devops/v1/organizations/${organizationId}/organization_config/check_chart?url=${url}`);
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
