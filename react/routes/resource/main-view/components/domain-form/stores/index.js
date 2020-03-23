import React, { createContext, useContext, useMemo, useEffect } from 'react/index';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import FormDataSet from './FormDataSet';
import PathListDataSet from './PathListDataSet';
import ServiceDataSet from './ServiceDataSet';
import AnnotationDataSet from './AnnotationDataSet';

const Store = createContext();

export function useDomainFormStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const {
      children,
      AppState: { currentMenuType: { projectId } },
      intl: { formatMessage },
      intlPrefix,
      envId,
      appServiceId,
      ingressId,
    } = props;

    const serviceDs = useMemo(() => new DataSet(ServiceDataSet({ projectId, envId, appServiceId })), [projectId, envId, appServiceId]);
    const pathListDs = useMemo(() => new DataSet(PathListDataSet({ formatMessage, intlPrefix, projectId, envId, ingressId, serviceDs })), [projectId, envId, ingressId]);
    const annotationDs = useMemo(() => new DataSet(AnnotationDataSet({ formatMessage })), []);
    const formDs = useMemo(() => new DataSet(FormDataSet({ formatMessage, intlPrefix, projectId, envId, ingressId, appServiceId, pathListDs, serviceDs, annotationDs })), [projectId, envId, ingressId, appServiceId]);

    useEffect(() => {
      if (ingressId) {
        formDs.query();
      } else {
        formDs.create();
        pathListDs.create();
        annotationDs.create();
      }
    }, []);

    const value = {
      ...props,
      formDs,
      pathListDs,
      serviceDs,
      annotationDs,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));
