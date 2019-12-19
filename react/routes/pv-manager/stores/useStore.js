import { useLocalStore } from 'mobx-react-lite';
import { axios, Choerodon } from '@choerodon/boot';
import map from 'lodash/map';
import { handlePromptError } from '../../../utils';

export default function useStore() {
  return useLocalStore(() => ({

  }));
}
