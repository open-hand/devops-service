import React, { createContext, useContext, useEffect, useMemo } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import { useAppTopStore } from '../../stores';
import ListDataSet from './ListDataSet';
import useStore from './useStore';

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
    const { intlPrefix } = useAppTopStore();
    const appListStore = useStore();
    const listDs = useMemo(() => new DataSet(ListDataSet(intlPrefix, formatMessage, projectId)), [projectId]);

    useEffect(() => {
      appListStore.checkCreate(projectId);
    }, [projectId]);

    const value = {
      ...props,
      listDs,
      appListStore,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));
