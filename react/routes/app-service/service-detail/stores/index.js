import React, { createContext, useContext, useMemo, useEffect } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import VersionDataSet from './VersionDataSet';
import AllocationDataSet from './AllocationDataSet';
import ShareDataSet from './ShareDataSet';
import NonePermissionDs from '../modals/stores/PermissionDataSet';
import usePermissionStore from '../modals/stores/useStore';
import OptionsDataSet from './OptionsDataSet';
import useStore from '../../stores/useStore';
import ListDataSet from '../../stores/ListDataSet';

const Store = createContext();

export function useServiceDetailStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const {
      AppState: { currentMenuType: { projectId, organizationId } },
      intl: { formatMessage },
      match: { params: { id } },
      children,
    } = props;
    const intlPrefix = 'c7ncd.appService';

    const shareVersionsDs = useMemo(() => new DataSet(OptionsDataSet()), []);
    const shareLevelDs = useMemo(() => new DataSet(OptionsDataSet()), []);

    const versionDs = useMemo(() => new DataSet(VersionDataSet(formatMessage, projectId, id)), [formatMessage, id, projectId]);
    const permissionDs = useMemo(() => new DataSet(AllocationDataSet(formatMessage, projectId, id)), [formatMessage, id, projectId]);
    const shareDs = useMemo(() => new DataSet(ShareDataSet(intlPrefix, formatMessage, projectId, id)), [formatMessage, id, projectId]);
    const detailDs = useMemo(() => new DataSet(ListDataSet(intlPrefix, formatMessage, projectId)), [intlPrefix, formatMessage, projectId]);
    const nonePermissionDs = useMemo(() => new DataSet(NonePermissionDs()));
    const permissionStore = usePermissionStore();


    const AppStore = useStore();


    useEffect(() => {
      nonePermissionDs.transport.read.url = `/devops/v1/projects/${projectId}/app_service/${id}/list_non_permission_users`;
      detailDs.transport.read = {
        url: `/devops/v1/projects/${projectId}/app_service/${id}`,
        method: 'get',
      };
      detailDs.paging = false;
      detailDs.query();
    }, [projectId, id]);

    useEffect(() => {
      shareLevelDs.transport.read.url = `/devops/v1/projects/${projectId}/app_service/${organizationId}/list_projects`;
      AppStore.judgeRole(organizationId, projectId);
    }, [organizationId, projectId]);

    const value = {
      ...props,
      prefixCls: 'c7ncd-appService',
      intlPrefix,
      versionDs,
      permissionDs,
      shareDs,
      detailDs,
      nonePermissionDs,
      permissionStore,
      shareVersionsDs,
      shareLevelDs,
      AppStore,
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
