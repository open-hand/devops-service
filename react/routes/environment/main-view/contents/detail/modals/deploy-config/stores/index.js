import React, { createContext, useContext, useMemo } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import AppOptionDataSet from './AppOptionDataSet';

const Store = createContext();

export function useFormStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const {
      AppState: { currentMenuType: { id: projectId } },
      children,
      intlPrefix,
    } = props;
    const appOptionDs = useMemo(() => new DataSet(AppOptionDataSet(projectId)), [projectId]);

    const value = {
      ...props,
      intlPrefix,
      appOptionDs,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  }
));
