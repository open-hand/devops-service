import { useLocalStore } from 'mobx-react-lite';
import checkPermission from '../../../../../../utils/checkPermission';

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

    async checkPermission({ projectId, organizationId, resourceType }) {
      const res = await checkPermission({
        code: 'devops-service.devops-environment.pageEnvUserPermissions',
        organizationId,
        projectId,
        resourceType,
      });
      this.setPermission(res);
    },
  }));
}
