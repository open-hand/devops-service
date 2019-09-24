import React, { createContext, useContext, useEffect, useMemo } from 'react';
import { DataSet } from 'choerodon-ui/pro';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { observer } from 'mobx-react-lite';
import TableDataSet from './TableDataSet';
import { useResourceStore } from '../../../../stores';

const Store = createContext();

export function useCertificateStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  observer((props) => {
    const { AppState: { currentMenuType: { id } }, children } = props;
    const {
      intlPrefix,
      intl: { formatMessage },
      resourceStore: { getSelectedMenu: { parentId }, setUpTarget, getUpTarget },
      itemTypes: { CERT_GROUP },
    } = useResourceStore();
    const certificateDs = useMemo(() => new DataSet(TableDataSet({
      formatMessage,
      intlPrefix,
      projectId: id,
      envId: parentId,
    })), [id, parentId]);

    const value = {
      ...props,
      certificateDs,
    };

    useEffect(() => {
      const { type, id: envId } = getUpTarget;
      if (type === CERT_GROUP && envId === parentId) {
        certificateDs.query();
        setUpTarget({});
      }
    }, [getUpTarget]);

    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  })
));
