import React, { createContext, useContext, useEffect, useMemo } from 'react';
import { observer } from 'mobx-react-lite';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import useStore from './useStore';

import ResourceSecurityDataSet from './ResourceSecurityDataSet';
import ResourceSecurityCreateDataSet from './ResourceSecurityCreateDataSet';


const resourceSecurityStore = createContext();

export function useResourceSecurityStore() {
  return useContext(resourceSecurityStore);
}


export const StoreProvider = injectIntl(inject('AppState')(observer((props) => {
  const {
    AppState: { currentMenuType: { id: projectId } },
    intl: { formatMessage },
    children,
    envId,
  } = props;

  const resourceSecurityDs = useMemo(() => new DataSet(ResourceSecurityDataSet(projectId, envId, formatMessage)), []);
  const resourceSecurityCreateDs = useMemo(() => new DataSet(ResourceSecurityCreateDataSet({ projectId, formatMessage })), []);
  const resourceSecurityLocalStore = useStore();

  useEffect(() => {
    resourceSecurityDs.transport = {
      read: {
        url: `/devops/v1/projects/${projectId}/notification/page_by_options?env_id=${envId}`,
        method: 'post',
      },
      destroy: ({ data: [data] }) => ({
        url: `/devops/v1/projects/${projectId}/notification/${data.id}`,
        method: 'delete',
      }),
    };
    resourceSecurityDs.query();
  }, [projectId, envId]);

  useEffect(() => {
    resourceSecurityCreateDs.transport.submit = ({ data: [data] }) => ({
      url: `/devops/v1/projects/${projectId}/notification`,
      method: 'post',
      data,
    });
    resourceSecurityCreateDs.transport.update = ({ data: [data] }) => ({
      url: `/devops/v1/projects/${projectId}/notification`,
      method: 'put',
      data,
    });
  }, [projectId]);

  const value = {
    ...props,
    projectId,
    resourceSecurityDs,
    formatMessage,
    resourceSecurityCreateDs,
    resourceSecurityLocalStore,
  };
  return (
    <resourceSecurityStore.Provider value={value}>
      {children}
    </resourceSecurityStore.Provider>
  );
})));
