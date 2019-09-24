import React, { createContext, useContext, useEffect, useMemo } from 'react';
import { DataSet } from 'choerodon-ui/pro';
import { inject } from 'mobx-react';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import TableDataSet from './TableDataSet';
import { useResourceStore } from '../../../../stores';

const Store = createContext();

export function useIstListStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  observer((props) => {
    const { AppState: { currentMenuType: { id } }, children } = props;
    const {
      intlPrefix,
      intl: { formatMessage },
      resourceStore: { getSelectedMenu: { parentId }, setUpTarget, getUpTarget },
      itemTypes: { IST_GROUP },
    } = useResourceStore();
    const istListDs = useMemo(() => new DataSet(TableDataSet({
      formatMessage,
      intlPrefix,
      projectId: id,
      envId: parentId,
    })), [id, parentId]);

    const value = {
      ...props,
      istListDs,
    };

    useEffect(() => {
      const { type, id: envId } = getUpTarget;
      if (type === IST_GROUP && envId === parentId) {
        istListDs.query();
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
