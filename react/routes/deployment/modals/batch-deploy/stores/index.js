import React, { createContext, useContext, useEffect, useMemo } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import BatchDeployDataSet from './BatchDeployDataSet';
import OptionsDataSet from '../../../stores/OptionsDataSet';
import NetworkDataSet from './NetworkDataSet';
import PortDataSet from './PortDataSet';
import PathListDataSet from './PathListDataSet';
import DomainDataSet from './DomainDataSet';
import AnnotationDataSet from '../../deploy/stores/AnnotationDataSet';

const Store = createContext();

export function useBatchDeployStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const {
      AppState: { currentMenuType: { projectId } },
      intl: { formatMessage },
      children,
      intlPrefix,
      deployStore,
      envId,
    } = props;

    const envOptionsDs = useMemo(() => new DataSet(OptionsDataSet()), []);
    const valueIdOptionsDs = useMemo(() => new DataSet(OptionsDataSet()), []);

    const pathListDs = useMemo(() => new DataSet(PathListDataSet({ formatMessage, projectId })), [projectId]);
    const annotationDs = useMemo(() => new DataSet(AnnotationDataSet({ formatMessage })), []);
    const domainDs = useMemo(() => new DataSet(DomainDataSet({ formatMessage, projectId, pathListDs, annotationDs })), [projectId]);
    const portsDs = useMemo(() => new DataSet(PortDataSet({ formatMessage, pathListDs })), []);
    const networkDs = useMemo(() => new DataSet(NetworkDataSet({ formatMessage, projectId, portsDs, pathListDs })), []);
    const batchDeployDs = useMemo(() => new DataSet(BatchDeployDataSet({ intlPrefix, formatMessage, projectId, envOptionsDs, valueIdOptionsDs, deployStore, networkDs, domainDs })), [projectId]);

    useEffect(() => {
      envOptionsDs.transport.read.url = `/devops/v1/projects/${projectId}/envs/list_by_active?active=true`;
      envOptionsDs.query();
      deployStore.loadAppService(projectId, 'normal_service');
      deployStore.loadShareAppService(projectId);
    }, [projectId]);

    const value = {
      ...props,
      batchDeployDs,
      portsDs,
      networkDs,
      pathListDs,
      domainDs,
      annotationDs,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));
