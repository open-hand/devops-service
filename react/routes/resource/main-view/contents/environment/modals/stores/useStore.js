import { useLocalStore } from 'mobx-react-lite';
import { axios } from '@choerodon/boot';
import { handlePromptError } from '../../../../../../../utils';

export default function useStore() {
  return useLocalStore(() => ({
    serviceData: [],
    setServiceData(data) {
      this.serviceData = data;
    },
    get getServiceData() {
      return this.serviceData;
    },

    appServiceIds: [undefined],
    setAppServiceIds(data) {
      this.appServiceIds = data;
    },
    get getAppServiceIds() {
      return this.appServiceIds;
    },

    users: [],
    setUsersData(data) {
      this.users = data;
    },
    get getUsersData() {
      return this.users;
    },

    userIds: [undefined],
    setUserIds(data) {
      this.userIds = data;
    },
    get getUserIds() {
      return this.userIds;
    },

    skipCheckPermission: true,
    setSkipCheckPermission(flag) {
      this.skipCheckPermission = flag;
    },
    get getSkipCheckPermission() {
      return this.skipCheckPermission;
    },

    async loadServiceData(projectId, envId) {
      const res = await axios.get(`/devops/v1/projects/${projectId}/env/app_services/non_related_app_service?env_id=${envId}`);
      if (handlePromptError(res)) {
        this.setServiceData(res);
      }
    },

    AddService(projectId, envId) {
      const data = {
        envId,
        appServiceIds: this.appServiceIds,
      };
      return axios.post(`/devops/v1/projects/${projectId}/env/app_services/batch_create`, JSON.stringify(data));
    },

    async loadUsers(projectId, envId) {
      const res = await axios.get(`/devops/v1/projects/${projectId}/envs/${envId}/permission/list_non_related`);
      if (handlePromptError(res)) {
        this.setUsersData(res);
      }
    },

    AddUsers(projectId, envId, objectVersionNumber) {
      const data = {
        envId,
        userIds: this.userIds,
        skipCheckPermission: this.skipCheckPermission,
        objectVersionNumber,
      };
      return axios.post(`/devops/v1/projects/${projectId}/envs/${envId}/permission`, JSON.stringify(data));
    },
  }));
}
