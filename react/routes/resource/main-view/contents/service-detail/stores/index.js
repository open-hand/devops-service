import React, { createContext, useContext, useEffect, useMemo } from 'react';
import { DataSet } from 'choerodon-ui/pro';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { observer } from 'mobx-react-lite';
import BaseInfoDataSet from './BaseInfoDataSet';
import { useResourceStore } from '../../../../stores';
import openWarnModal from '../../../../../../utils/openWarnModal';

const Store = createContext();

export function useNetworkDetailStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  observer((props) => {
    const { AppState: { currentMenuType: { id: projectId } }, intl: { formatMessage }, children } = props;
    const {
      resourceStore,
      treeDs,
      itemTypes: { SERVICES_ITEM },
    } = useResourceStore();
    const { getSelectedMenu: { id, parentId, itemType }, getUpTarget, setUpTarget } = resourceStore;

    const baseInfoDs = useMemo(() => new DataSet(BaseInfoDataSet()), []);

    function freshTree() {
      treeDs.query();
    }

    function checkExist() {
      return resourceStore.checkExist({
        projectId,
        type: 'service',
        envId: parentId.split('**')[0],
        id,
      }).then((isExist) => {
        if (!isExist) {
          openWarnModal(freshTree, formatMessage);
        }
        return isExist;
      });
    }

    useEffect(() => {
      checkExist().then((query) => {
        if (query) {
          baseInfoDs.transport.read.url = `/devops/v1/projects/${projectId}/service/${id}`;
          baseInfoDs.query();
        }
      });
    }, [projectId, id]);

    useEffect(() => {
      const { type, id: serviceId } = getUpTarget;
      if (type === SERVICES_ITEM && serviceId === id) {
        baseInfoDs.query();
        setUpTarget({});
      }
    }, [getUpTarget]);

    const value = {
      ...props,
      baseInfoDs,
    };

    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  })
));
