import React, { createContext, useContext, useEffect, useMemo } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import ListDataSet from './ListDataSet';
import useStore from './useStore';

const Store = createContext();

export function usePVManagerStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const {
      AppState: { currentMenuType: { projectId } },
      intl: { formatMessage },
      children,
    } = props;
    const intlPrefix = 'c7ncd.pv';
    const listDs = useMemo(() => new DataSet(ListDataSet(intlPrefix, formatMessage, projectId)), [projectId]);

    const pvStore = useStore();

    const value = {
      ...props,
      prefixCls: 'c7ncd-pv',
      intlPrefix,
      permissions: [
        'devops-service.devops-pv.queryAll',
        'devops-service.devops-pv.createPv',
        'devops-service.devops-pv.checkPvName',
        'devops-service.devops-pv.deletePv',
        'devops-service.devops-pv.queryById',
      ],
      listDs,
      pvStore,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));
