import React, { createContext, useContext, useEffect, useMemo } from 'react';
import { DataSet } from 'choerodon-ui/pro';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { observer } from 'mobx-react-lite';
import { useResourceStore } from '../../../../stores';
import DetailDataSet from './DetailDataSet';
import openWarnModal from '../../../../../../utils/openWarnModal';

const Store = createContext();

export function useCertDetailStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  observer((props) => {
    const { AppState: { currentMenuType: { id: projectId } }, intl: { formatMessage }, children } = props;
    const {
      resourceStore,
      treeDs,
    } = useResourceStore();
    const { getSelectedMenu: { id, parentId } } = resourceStore;
    const detailDs = useMemo(() => new DataSet(DetailDataSet()), []);

    function freshTree() {
      treeDs.query();
    }

    function checkExist() {
      return resourceStore.checkExist({
        projectId,
        type: 'certificate',
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
          detailDs.transport.read.url = `/devops/v1/projects/${projectId}/certifications/${id}`;
          detailDs.query();
        }
      });
    }, [projectId, id]);

    const value = {
      ...props,
      detailDs,
    };

    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  })
));
