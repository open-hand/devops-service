import { useLocalStore } from 'mobx-react-lite';
import { axios } from '@choerodon/boot';
import { handlePromptError } from '../../../../../../../utils';

export default function useStore() {
  return useLocalStore(() => ({

  }));
}
