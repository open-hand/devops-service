import React, { createContext, useContext, useMemo } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import useStore from './useStore';
import ListDataSet from './ListDataSet';

const Store = createContext();

export function useAppServiceStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const {
      AppState: { currentMenuType: { projectId } },
      intl: { formatMessage },
      children,
    } = props;
    const intlPrefix = 'c7ncd.appService';
    const AppStore = useMemo(() => useStore(), []);
    const listDs = useMemo(() => new DataSet(ListDataSet(intlPrefix, formatMessage, projectId)), [formatMessage, projectId]);

    const value = {
      ...props,
      prefixCls: 'c7ncd-appService',
      intlPrefix,
      listDs,
      AppStore,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));
