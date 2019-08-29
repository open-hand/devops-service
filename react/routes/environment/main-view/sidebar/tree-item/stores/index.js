import React, { createContext, useContext, useMemo } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import { useEnvironmentStore } from '../../../../stores';
import GroupModifyDataSet from './GroupModifyDataSet';
import useStore from './useStore';

const Store = createContext();

export function useTreeItemStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const {
      intlPrefix,
    } = useEnvironmentStore();
    const {
      AppState: { currentMenuType: { id } },
      intl: { formatMessage },
      children,
    } = props;
    const groupFormDs = useMemo(() => new DataSet(GroupModifyDataSet({ formatMessage, intlPrefix, id })), [id]);
    const envItemStore = useStore();

    const value = {
      ...props,
      groupFormDs,
      envItemStore,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));
