import React, { createContext, useMemo, useContext, useEffect } from 'react';
import { inject } from 'mobx-react';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import ResourceCountDataSet from './ResourceCountDataSet';
import { useResourceStore } from '../../../../stores';
import { useMainStore } from '../../../stores';
import GitopsLogDataSet from '../../environment/stores/GitopsLogDataSet';
import GitopsSyncDataSet from '../../environment/stores/GitopsSyncDataSet';
import RetryDataSet from '../../environment/stores/RetryDataSet';

const Store = createContext();

export function useREStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  observer((props) => {
    const {
      AppState: { currentMenuType: { id: projectId } },
      intl: { formatMessage },
      children,
    } = props;
    const { resourceStore: { getSelectedMenu: { id } }, intlPrefix } = useResourceStore();
    const { baseInfoDs } = useMainStore();

    const resourceCountDs = useMemo(() => new DataSet(ResourceCountDataSet()), []);
    const gitopsLogDs = useMemo(() => new DataSet(GitopsLogDataSet({ formatMessage, intlPrefix })), []);
    const gitopsSyncDs = useMemo(() => new DataSet(GitopsSyncDataSet()), []);
    const retryDs = useMemo(() => new DataSet(RetryDataSet()), []);

    useEffect(() => {
      resourceCountDs.transport.read.url = `/devops/v1/projects/${projectId}/envs/${id}/resource_count`;
      gitopsLogDs.transport.read.url = `/devops/v1/projects/${projectId}/envs/${id}/error_file/page_by_env`;
      gitopsSyncDs.transport.read.url = `/devops/v1/projects/${projectId}/envs/${id}/status`;
      resourceCountDs.query();
      gitopsSyncDs.query();
      gitopsLogDs.query();
    }, [projectId, id]);

    const value = {
      ...props,
      baseInfoDs,
      resourceCountDs,
      gitopsLogDs,
      gitopsSyncDs,
      retryDs,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  })
));
