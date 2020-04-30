import { useLocalStore } from 'mobx-react-lite';
import { axios } from '@choerodon/boot';
import { handlePromptError } from '../../../../utils';

export default function useStore() {
  return useLocalStore(() => ({
    canCreate: false,
    get getCanCreate() {
      return this.canCreate;
    },
    setCanCreate(flag) {
      this.canCreate = flag;
    },

    async checkCreate(projectId) {
      try {
        const res = await axios.get(`devops/v1/projects/${projectId}/app_service/check_enable_create`);
        this.setCanCreate(handlePromptError(res));
      } catch (e) {
        this.setCanCreate(false);
      }
    },
  }));
}
