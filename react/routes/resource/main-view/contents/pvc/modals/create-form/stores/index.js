import React, { createContext, useContext, useEffect, useMemo } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import FormDataSet from './FormDataSet';
import OptionsDataSet from './OptionDataSet';

const Store = createContext();

export function usePVCCreateStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const {
      AppState: { currentMenuType: { projectId } },
      intl: { formatMessage },
      children,
      intlPrefix,
      envId,
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

    const pvDs = useMemo(() => new DataSet(OptionsDataSet(projectId, envId)), [projectId, envId]);
    const formDs = useMemo(() => new DataSet(FormDataSet({ intlPrefix, formatMessage, projectId, envId, typeDs, modeDs, storageDs, pvDs })), [projectId, envId]);

    const value = {
      ...props,
      formDs,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));
