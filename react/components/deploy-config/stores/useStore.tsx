import { useLocalStore } from 'mobx-react-lite';
import { axios, Choerodon } from '@choerodon/boot';

interface RefObject<T> {
  readonly current: T | null;
}

export default function useStore() {
  return useLocalStore(() => ({
    value: '',
    setValue(value: string) {
      this.value = value;
    },
    get getValue() {
      return this.value;
    },
    async loadValue(projectId: number, id: string) {
      try {
        const res = await axios.get(`/devops/v1/projects/${projectId}/app_service_versions/value?app_service_id=${id}`);
        if (res && !res.failed) {
          this.setValue(res);
          return res;
        }
        this.setValue('');
        return '';
      } catch (e) {
        this.setValue('');
        Choerodon.handleResponseError(e);
        return '';
      }
    },
  }));
}

export type StoreProps = ReturnType<typeof useStore>;
