import React, { createContext, useContext, useMemo } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import ListDataSet from './ListDataSet';

const Store = createContext();

export function useAppTopStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const {
      AppState: { currentMenuType: { id: projectId } },
      intl: { formatMessage },
      children,
    } = props;
    const intlPrefix = 'c7ncd.appService';
    const listPermissions = useMemo(() => ([
      'devops-service.app-service.pageByOptions',
      'devops-service.app-service.create',
      'devops-service.app-service.importApp',
      'devops-service.app-service.update',
      'devops-service.app-service.updateActive',
      'devops-service.app-service.delete',
    ]), []);
    const detailPermissions = useMemo(() => ([
      'devops-service.app-service.query',
      'devops-service.app-service.update',
      'devops-service.app-service.updateActive',
      'devops-service.app-service-version.pageByOptions',
      'devops-service.app-share-rule.create',
      'devops-service.app-share-rule.update',
      'devops-service.app-share-rule.delete',
      'devops-service.app-share-rule.query',
      'devops-service.app-share-rule.pageByOptions',
      'devops-service.app-service.pagePermissionUsers',
      'devops-service.app-service.updatePermission',
      'devops-service.app-service.deletePermission',
      'devops-service.app-service.listNonPermissionUsers',
    ]), []);
    const listDs = useMemo(() => new DataSet(ListDataSet(intlPrefix, formatMessage, projectId, 'list')), [projectId]);

    const value = {
      ...props,
      prefixCls: 'c7ncd-appService',
      intlPrefix,
      listDs,
      listPermissions,
      detailPermissions,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));
