import React, { createContext, useContext, useMemo } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import FormDataSet from './FormDataSet';
import AppOptionDataSet from './AppOptionDataSet';
import useStore from './useStore';

const Store = createContext();

export function useFormStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const {
      AppState: { currentMenuType: { id: projectId } },
      intl: { formatMessage },
      children,
      intlPrefix,
    } = props;
    const configStore = useStore();
    const formDs = useMemo(() => new DataSet(FormDataSet({ formatMessage, intlPrefix, projectId, store: configStore })), [projectId]);
    const appOptionDs = useMemo(() => new DataSet(AppOptionDataSet(projectId)), [projectId]);

    const value = {
      ...props,
      formDs,
      intlPrefix,
      appOptionDs,
      configStore,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  }
));
