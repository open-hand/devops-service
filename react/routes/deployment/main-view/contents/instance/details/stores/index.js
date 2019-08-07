import React, { createContext, useMemo, useEffect } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
// import { useDeploymentStore } from '../../../../../stores';
import DetailsStore from './DetailsStore';

const Store = createContext();

export default Store;

const menuId = 7819;
export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const { AppState: { currentMenuType: { id } }, children } = props;
    // const { deploymentStore: { getSelectedMenu: { menuId } } } = useDeploymentStore();
    const detailsStore = useMemo(() => new DetailsStore(), []);

    useEffect(() => {
      detailsStore.loadResource(id, menuId);
    }, [id, detailsStore]);

    const value = {
      ...props,
      detailsStore,
      menuId,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));
