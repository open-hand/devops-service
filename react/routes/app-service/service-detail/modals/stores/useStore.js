import { useLocalStore } from 'mobx-react-lite';
import { axios } from '@choerodon/master';
import { handlePromptError } from '../../../../../utils';

export default function useStore() {
  return useLocalStore(() => ({
    checked: false,
    setChecked(value) {
      this.checked = value;
    },
    permissionUsers: [undefined],
    setPermissionUsers(action) {
      const { type, value, index } = action;
      switch (type) {
        case 'add':
          this.permissionUsers.push(undefined);
          break;
        case 'change':
          if (this.permissionUsers.indexOf(value) < 0) {
            this.permissionUsers[index] = value;
          } else {
            handlePromptError('您已经选择过此用户，请勿重复选择');
          }
          return;
        case 'sub':
          return this.permissionUsers.filter((v, k) => k !== index);
        default:
          throw new Error('action type not in [add,sub,change]');
      }
    },
    clearPermissionUsers() {
      this.permissionUsers = [undefined];
    },
    async addUsers({ projectId, id }) {
      let userIds = this.permissionUsers;
      if (!userIds[userIds.length - 1]) {
        userIds = userIds.slice(0, userIds.length - 1);
      }
      const data = {
        userIds,
        skipCheckPermission: this.checked,
      };
      const res = await axios.post(`/devops/v1/projects/${projectId}/app_service/${id}/update_permission`, JSON.stringify(data));
      return handlePromptError(res);
    },
  }));
}
