import React, { createContext, useContext, useMemo, useEffect } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import FormDataSet from './FormDataSet';
import OptionsDataSet from './OptionsDataSet';
import ProjectTableDataSet from './ProjectTableDataSet';
import ProjectOptionsDataSet from './ProjectOptionsDataSet';
import SelectDataSet from './SelectDataSet';
import NodeNameDataSet from './NodeNameDataSet';

const Store = createContext();

export function usePVCreateStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const {
      AppState: { currentMenuType: { projectId } },
      intl: { formatMessage },
      children,
      intlPrefix,
    } = props;
    const typeDs = useMemo(() => new DataSet({
      data: [
        {
          value: 'NFS',
        },
        {
          value: 'HostPath',
        },
        {
          value: 'LocalPV',
        },
      ],
      selection: 'single',
    }), []);
    const modeDs = useMemo(() => new DataSet({
      data: [
        {
          value: 'ReadWriteOnce',
        },
        {
          value: 'ReadOnlyMany',
        },
        {
          value: 'ReadWriteMany',
        },
      ],
      selection: 'single',
    }), []);
    const storageDs = useMemo(() => new DataSet({
      data: [
        {
          value: 'Mi',
        },
        {
          value: 'Gi',
        },
        {
          value: 'Ti',
        },
      ],
      selection: 'single',
    }), []);
    const clusterDs = useMemo(() => new DataSet(OptionsDataSet(projectId)), [projectId]);
    const projectOptionsDs = useMemo(() => new DataSet(ProjectOptionsDataSet({ projectId })), [projectId]);
    const projectTableDs = useMemo(() => new DataSet(ProjectTableDataSet({ intlPrefix, formatMessage })), []);
    const selectDs = useMemo(() => new DataSet(SelectDataSet({ intlPrefix, formatMessage, projectOptionsDs })), []);
    const nodeNameDs = useMemo(() => new DataSet((NodeNameDataSet({ projectId }))), [projectId]);
    const formDs = useMemo(() => new DataSet(FormDataSet({ intlPrefix, formatMessage, projectId, typeDs, modeDs, storageDs, clusterDs, projectOptionsDs, projectTableDs, nodeNameDs })), [projectId]);

    const value = {
      ...props,
      formDs,
      projectTableDs,
      projectOptionsDs,
      selectDs,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));
