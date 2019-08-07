import React, { createContext, useContext, useMemo } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import VersionDataSet from './VersionDataSet';
import AllocationDataSet from './AllocationDataSet';
import ShareDataSet from './ShareDataSet';

const Store = createContext();

export function useServiceDetailStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const {
      AppState: { currentMenuType: { projectId } },
      intl: { formatMessage },
      match: { params: { id } },
      children,
    } = props;
    const intlPrefix = 'c7ncd.appService';
    
    const versionDs = useMemo(() => new DataSet(VersionDataSet(formatMessage, projectId, id)), [formatMessage, id, projectId]);
    const permissionDs = useMemo(() => new DataSet(AllocationDataSet(formatMessage, projectId, id)), [formatMessage, id, projectId]);
    const shareDs = useMemo(() => new DataSet(ShareDataSet(intlPrefix, formatMessage, projectId, id)), [formatMessage, id, projectId]);

    const value = {
      ...props,
      prefixCls: 'c7ncd-appService',
      intlPrefix,
      versionDs,
      permissionDs,
      shareDs,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));
