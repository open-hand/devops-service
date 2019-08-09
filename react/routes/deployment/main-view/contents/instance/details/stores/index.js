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
    const { getSelectedMenu: { menuId } } = deploymentStore;
    const { istStore } = useInstanceStore();
    const detailsStore = useMemo(() => new DetailsStore(), []);

    useEffect(() => {
      detailsStore.loadResource(id, menuId);
    }, [id, detailsStore, menuId]);

    const value = {
      ...props,
      detailsStore,
      istStore,
      instanceId: menuId,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));
