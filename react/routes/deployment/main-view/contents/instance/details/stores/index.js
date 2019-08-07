import React, { createContext, useMemo, useEffect } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { useDeploymentStore } from '../../../../../stores';
import { useInstanceStore } from '../../stores';
import DetailsStore from './DetailsStore';

const Store = createContext();

export default Store;

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const { AppState: { currentMenuType: { id } }, children } = props;
    const { deploymentStore } = useDeploymentStore();
    const { baseInfoDs } = useInstanceStore();
    const { getSelectedMenu: { menuId } } = deploymentStore;
    const detailsStore = useMemo(() => new DetailsStore(), []);
    const instanceStatus = useMemo(() => {
      const info = baseInfoDs.data;
      const record = info[0];
      if (record) {
        const status = record.get('status');
        return { status };
      }
      return null;
    }, [baseInfoDs.data]);

    useEffect(() => {
      detailsStore.loadResource(id, menuId);
    }, [id, detailsStore, menuId]);

    const value = {
      ...props,
      detailsStore,
      instanceId: menuId,
      instanceStatus,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));
