import React, { createContext, useContext, useEffect, useMemo } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import DetailDataSet from '../../repository/stores/DetailDataSet';
import HomeDataSet from '../../repository/stores/HomeDataSet';
import useStore from './useStore';

const Store = createContext();

export function useRepositoryStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const {
      AppState: { currentMenuType: { id } },
      intl: { formatMessage },
      children,
    } = props;
    const intlPrefix = 'c7ncd.repository';
    const url = useMemo(() => `/devops/v1/projects/${id}/project_config`, [id]);

    const detailDs = useMemo(() => new DataSet(DetailDataSet(intlPrefix, formatMessage, url)), [intlPrefix, formatMessage, url]);
    const homeDs = useMemo(() => new DataSet(HomeDataSet()), []);

    const repositoryStore = useStore();

    useEffect(() => {
      homeDs.transport.read.url = `/devops/v1/projects/${id}/project_config/default_config`;
      homeDs.query();
    }, [id]);

    const value = {
      ...props,
      prefixCls: 'c7ncd-repository',
      intlPrefix,
      homeDs,
      detailDs,
      repositoryStore,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));
