import React, { createContext, useContext, useMemo, useEffect } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import VersionDataSet from './VersionDataSet';
import AllocationDataSet from './AllocationDataSet';
import ShareDataSet from './ShareDataSet';
import DetailDataSet from '../../stores/DetailDataSet';
import NonePermissionDs from '../modals/stores/PermissionDataSet';

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
    const detailDs = useMemo(() => new DataSet(DetailDataSet(projectId, id)));
    const nonePermissionDs = useMemo(() => new DataSet(NonePermissionDs()));

    useEffect(() => {
      nonePermissionDs.transport.read.url = `/devops/v1/projects/${projectId}/app_service/${id}/list_non_permission_users`;
    }, [projectId, id]);

    const value = {
      ...props,
      prefixCls: 'c7ncd-appService',
      intlPrefix,
      versionDs,
      permissionDs,
      shareDs,
      detailDs,
      nonePermissionDs,
      params: {
        projectId,
        id,
      },
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));
