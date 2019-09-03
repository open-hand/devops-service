import React, { createContext, useContext, useEffect, useMemo } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import DetailDataSet from './DetailDataSet';
import HomeDataSet from './HomeDataSet';

const Store = createContext();

export function useRepositoryStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const {
      AppState: { currentMenuType: { organizationId } },
      intl: { formatMessage },
      children,
    } = props;
    const intlPrefix = 'c7ncd.repository';

    const detailDs = useMemo(() => new DataSet(DetailDataSet(intlPrefix, formatMessage, organizationId)), [intlPrefix, formatMessage, organizationId]);
    const homeDs = useMemo(() => new DataSet(HomeDataSet(organizationId)), [organizationId]);

    const value = {
      ...props,
      prefixCls: 'c7ncd-repository',
      intlPrefix,
      homeDs,
      detailDs,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));
