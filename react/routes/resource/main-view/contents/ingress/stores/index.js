import React, { createContext, useContext, useEffect, useMemo } from 'react';
import { DataSet } from 'choerodon-ui/pro';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { observer } from 'mobx-react-lite';
import TableDataSet from './TableDataSet';
import { useResourceStore } from '../../../../stores';

const Store = createContext();

export function useIngressStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  observer((props) => {
    const { AppState: { currentMenuType: { id } }, children } = props;
    const {
      intlPrefix,
      intl: { formatMessage },
      resourceStore: { getSelectedMenu: { parentId }, getUpTarget, setUpTarget },
      itemTypes: { INGRESS_GROUP },
    } = useResourceStore();
    const ingressDs = useMemo(() => new DataSet(TableDataSet({
      formatMessage,
      intlPrefix,
      projectId: id,
      envId: parentId,
    })), [id, parentId]);

    const value = {
      ...props,
      ingressDs,
    };

    useEffect(() => {
      const { type, id: envId } = getUpTarget;
      if (type === INGRESS_GROUP && envId === parentId) {
        ingressDs.query();
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
