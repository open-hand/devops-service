import React, { createContext, useMemo, useContext, useEffect } from 'react';
import { inject } from 'mobx-react';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import { useClusterStore } from '../../../../../stores';
import { useClusterMainStore } from '../../../../stores';
import useStore from './useStore';


const Store = createContext();


export function usePolarisContentStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  observer((props) => {
    const { intl: { formatMessage }, AppState: { currentMenuType: { id: projectId } }, children } = props;
    const { ClusterDetailDs, mainStore } = useClusterMainStore();
    const { intlPrefix, clusterStore } = useClusterStore();
    const { getSelectedMenu: { id } } = clusterStore;

    useEffect(() => {

    }, []);

    const value = {
      ...props,
      intlPrefix,
      formatMessage,
      //   ClusterDetailDs,
      projectId,
    //   clusterId: id,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  })
));
