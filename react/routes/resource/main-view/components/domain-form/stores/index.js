import React, { createContext, useContext, useMemo, useEffect } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { axios } from '@choerodon/boot';
import { DataSet } from 'choerodon-ui/pro';
import forEach from 'lodash/forEach';
import find from 'lodash/find';
import FormDataSet from './FormDataSet';
import PathListDataSet from './PathListDataSet';
import ServiceDataSet from './ServiceDataSet';
import AnnotationDataSet from './AnnotationDataSet';
import { handlePromptError } from '../../../../../../utils';

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
        loadData();
      } else {
        serviceDs.query();
        formDs.create();
        pathListDs.create();
        annotationDs.create();
      }
    }, []);

    async function loadData() {
      const [ingress, serviceList] = await axios.all([formDs.query(), serviceDs.query()]);
      if (handlePromptError(ingress) && handlePromptError(serviceList)) {
        forEach(ingress.pathList || [], ({ serviceId, serviceStatus, serviceName, serviceError, path, servicePort }) => {
          const service = serviceDs.find((eachRecord) => eachRecord.get('id') === serviceId);
          const pathRecord = pathListDs.find((eachRecord) => eachRecord.get('serviceId') === serviceId && eachRecord.get('path') === path);
          if (serviceStatus !== 'running' && !service) {
            const serviceRecord = serviceDs.create({
              id: serviceId,
              name: serviceName,
              status: serviceStatus,
              serviceError,
              ports: [{ port: servicePort }],
            });
            pathRecord.init('ports', [{ port: servicePort }]);
            serviceDs.push(serviceRecord);
          } else {
            const { config, ports } = service.toData() || {};
            pathRecord.init('ports', config ? config.ports || [] : ports);
          }
        });
      }
    }

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
