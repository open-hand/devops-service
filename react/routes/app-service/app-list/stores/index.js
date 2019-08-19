import React, { createContext, useContext, useMemo } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import useStore from './useStore';
import ListDataSet from './ListDataSet';
import ImportDataSet from './ImportDataSet';
import ImportTableDataSet from './ImportTableDataSet';

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
    const importDs = useMemo(() => new DataSet(ImportDataSet(intlPrefix, formatMessage, projectId)), [formatMessage, projectId]);
    const importTableDs = useMemo(() => new DataSet(ImportTableDataSet(intlPrefix, formatMessage, projectId)), [formatMessage, projectId]);

    const value = {
      ...props,
      prefixCls: 'c7ncd-appService',
      intlPrefix,
      listDs,
      importDs,
      importTableDs,
      AppStore,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));
