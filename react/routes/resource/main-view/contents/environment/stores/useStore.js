import { useLocalStore } from 'mobx-react-lite';
import { axios, Choerodon } from '@choerodon/boot';
import checkPermission from '../../../../../../utils/checkPermission';
import { handlePromptError } from '../../../../../../utils';

export default function useStore({ defaultTab }) {
  return useLocalStore(() => ({
    tabKey: defaultTab,

    setTabKey(data) {
      this.tabKey = data;
    },
    get getTabKey() {
      return this.tabKey;
    },
    hasPermission: false,
    setPermission(data) {
      this.hasPermission = data;
    },
    get getPermission() {
      return this.hasPermission;
    },

    value: '',
    setValue(value) {
      this.value = value;
    },
    get getValue() {
      return this.value;
    },

    async checkPermission({ projectId, organizationId, resourceType }) {
      const res = await checkPermission({
        code: 'devops-service.devops-environment.pageEnvUserPermissions',
        organizationId,
        projectId,
        resourceType,
      });
      this.setPermission(res);
    },

    async loadValue(projectId, id) {
      try {
        const res = await axios.get(`/devops/v1/projects/${projectId}/app_service_versions/value?app_service_id=${id}`);
        if (handlePromptError(res)) {
          this.setValue(res);
        }
      } catch (e) {
        Choerodon.handleResponseError(e);
      }
    },

    checkDelete(projectId, id) {
      return axios.get(`/devops/v1/projects/${projectId}/deploy_value/check_delete?value_id=${id}`);
    },
  }));
}
