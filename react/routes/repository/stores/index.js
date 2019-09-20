import React, { createContext, useContext, useEffect, useMemo } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import DetailDataSet from './DetailDataSet';
import HomeDataSet from './HomeDataSet';
import useStore from './useStore';

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
    const url = useMemo(() => `/devops/v1/organizations/${organizationId}/organization_config`, [organizationId]);

    const detailDs = useMemo(() => new DataSet(DetailDataSet(intlPrefix, formatMessage, url)), [intlPrefix, formatMessage, url]);
    const homeDs = useMemo(() => new DataSet(HomeDataSet()), []);

    const repositoryStore = useStore();

    useEffect(() => {
      homeDs.transport.read.url = `/devops/v1/organizations/${organizationId}/organization_config/default_config`;
      homeDs.query();
    }, [organizationId]);

    const value = {
      ...props,
      prefixCls: 'c7ncd-repository',
      permissions: [
        'devops-service.devops-organization-config.queryOrganizationDefaultConfig',
        'devops-service.devops-organization-config.create',
        'devops-service.devops-organization-config.query',
      ],
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
