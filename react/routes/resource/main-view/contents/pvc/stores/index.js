import React, { createContext, useContext, useMemo, useEffect } from 'react';
import { DataSet } from 'choerodon-ui/pro';
import { inject } from 'mobx-react';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import PVCTableDataSet from './PVCTableDataSet';
import { useResourceStore } from '../../../../stores';
import useSecretStore from '../../../stores/useSecretStore';
import getTablePostData from '../../../../../../utils/getTablePostData';

const Store = createContext();

export function usePVCStore() {
  return useContext(Store);
}


export const StoreProvider = injectIntl(inject('AppState')(
  observer((props) => {
    const { AppState: { currentMenuType: { id } }, children } = props;
    const {
      intl: { formatMessage },
      resourceStore: { getSelectedMenu: { parentId }, setUpTarget, getUpTarget },
      // itemTypes: { CIPHER_GROUP },
    } = useResourceStore();

    const PVCtableDS = useMemo(() => new DataSet(PVCTableDataSet({ formatMessage })), []);
    useEffect(() => {

    }, []);

    const value = {
      ...props,
      PVCtableDS,

    };

    useEffect(() => {

    }, []);

    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  })
));
