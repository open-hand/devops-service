import React, { createContext, useContext, useEffect, useMemo } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import PipelineDataSet from './PipelineDataSet';

const Store = createContext();

export function useProcessStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const {
      AppState: { currentMenuType: { projectId } },
      intl: { formatMessage },
      children,
      intlPrefix,
    } = props;

    const pipelineDs = useMemo(() => new DataSet(PipelineDataSet(intlPrefix, formatMessage, projectId)), [projectId]);

    const value = {
      ...props,
      pipelineDs,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));
