import React, {
  createContext, useContext, useEffect, useMemo,
} from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import FormDataSet from '@/components/deploy-config/stores/FormDataSet';
import useStore, { StoreProps } from '@/components/deploy-config/stores/useStore';
import AppOptionDataSet from './AppOptionDataSet';

interface ContextProps {
  prefixCls: string,
  intlPrefix: string,
  formatMessage(arg0: object): string,
  formDs: DataSet,
  projectId: number,
  modal: any,
  refresh(arg?: string): void,
  configId?: string,
  store: StoreProps,
}

const Store = createContext({} as ContextProps);

export function useFormStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  (props: any) => {
    const {
      AppState: { currentMenuType: { id: projectId } },
      children,
      intl: { formatMessage },
      envId,
      configId,
    } = props;
    const intlPrefix = 'c7ncd.deployment';

    const store = useStore();
    const appOptionDs = useMemo(
      () => new DataSet(AppOptionDataSet(projectId)), [projectId],
    );
    const formDs = useMemo(
      () => new DataSet(FormDataSet({
        formatMessage,
        intlPrefix,
        projectId,
        envId,
        configId,
        store,
        appOptionDs,
      })), [projectId, envId],
    );

    useEffect(() => {
      if (configId) {
        formDs.query();
      } else {
        appOptionDs.query();
        formDs.create();
      }
    }, [projectId, envId, configId]);

    const value = {
      ...props,
      prefixCls: 'c7ncd-deployment',
      intlPrefix,
      formatMessage,
      appOptionDs,
      store,
      formDs,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));
