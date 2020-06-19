import React, { createContext, useContext, useMemo, useEffect, useState } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import VersionDataSet from './VersionDataSet';
import AllocationDataSet from './AllocationDataSet';
import ShareDataSet from './ShareDataSet';
import usePermissionStore from '../modals/stores/useStore';
import OptionsDataSet from './OptionsDataSet';
import { useAppTopStore } from '../../stores';
import DetailDataSet from './DetailDataSet';
import checkPermission from '../../../../utils/checkPermission';

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
    const { appServiceStore, intlPrefix } = useAppTopStore();
    const versionDs = useMemo(() => new DataSet(VersionDataSet(formatMessage, projectId, id)), [formatMessage, id, projectId]);
    const permissionDs = useMemo(() => new DataSet(AllocationDataSet(formatMessage, intlPrefix, projectId, id)), [formatMessage, id, projectId]);
    const shareDs = useMemo(() => new DataSet(ShareDataSet(intlPrefix, formatMessage, projectId, id, organizationId)), [formatMessage, id, projectId]);
    const detailDs = useMemo(() => new DataSet(DetailDataSet(intlPrefix, formatMessage)), []);
    const nonePermissionDs = useMemo(() => new DataSet(OptionsDataSet()), []);
    const permissionStore = usePermissionStore();

    const [accessPermission, setAccessPermission] = useState(false);
    const [accessShare, setAccessShare] = useState(false);

    useEffect(() => {
      nonePermissionDs.transport.read.url = `/devops/v1/projects/${projectId}/app_service/${id}/list_non_permission_users`;
      detailDs.transport.read = {
        url: `/devops/v1/projects/${projectId}/app_service/${id}`,
        method: 'get',
      };
      detailDs.query();
    }, [projectId, id]);

    useEffect(() => {
      async function judgeRole() {
        const codeArr = [
          'choerodon.code.project.develop.app-service.ps.permission',
          'choerodon.code.project.develop.app-service.ps.share',
        ];
        try {
          const [permission, share] = await checkPermission({ organizationId, projectId, codeArr });
          setAccessPermission(permission);
          setAccessShare(share);
        } catch (e) {
          setAccessPermission(false);
          setAccessShare(false);
        }
      }
      judgeRole();
    }, [organizationId, projectId]);

    const value = {
      ...props,
      versionDs,
      permissionDs,
      shareDs,
      detailDs,
      nonePermissionDs,
      permissionStore,
      params: {
        projectId,
        id,
      },
      access: {
        accessPermission,
        accessShare,
      },
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));
