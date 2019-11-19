import { useLocalStore } from 'mobx-react-lite';
import { axios, Choerodon } from '@choerodon/boot';
import map from 'lodash/map';
import { handlePromptError } from '../../../../../utils';
import getTablePostData from '../../../../../utils/getTablePostData';

export default function useStore() {
  return useLocalStore(() => ({

    appService: [],
    setAppService(data) {
      this.appService = data;
    },
    get getAppService() {
      return this.appService.slice();
    },

    async loadAppService(projectId, type) {
      this.setAppService([]);
      try {
        const res = await axios.get(`/devops/v1/projects/${projectId}/app_service/list_all_app_services?deploy_only=false&type=${type}`);
        if (handlePromptError(res)) {
          this.setAppService(res);
        }
      } catch (e) {
        Choerodon.handleResponseError(e);
      }
    },
  }));
}
