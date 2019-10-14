import React, { Component, useEffect } from 'react';
import { inject } from 'mobx-react';
import { observer } from 'mobx-react-lite';
import { Form } from 'choerodon-ui';
import { injectIntl } from 'react-intl';
import { Choerodon } from '@choerodon/boot';
import filter from 'lodash/filter';
import forEach from 'lodash/forEach';
import DomainForm from '../../../../../../../components/domain-form';
import { handlePromptError } from '../../../../../../../utils';

import '../../../../../../main.less';
import './index.less';

const createDomain = injectIntl(observer(({
  AppState: { currentMenuType: { projectId } },
  store,
  envId,
  appServiceId,
  modal,
  id,
  type,
  form,
  refresh,
}) => {
  useEffect(() => {
    store.loadNetwork(projectId, envId, appServiceId);
  }, []);
  
  modal.handleOk(handleSubmit);
  modal.handleCancel(handleClose);

  function formValidate() {
    return new Promise((resolve) => {
      form.validateFieldsAndScroll((err, data) => {
        if (!err) {
          const { domain, name, certId, paths, path, network, port } = data;
          const postData = { domain, name, envId, appServiceId };
          if (certId) {
            postData.certId = certId;
          }
          const pathList = [];
          const networkList = store.getNetwork;
          forEach(paths, (item) => {
            const pt = path[item];
            const serviceId = network[item];
            const servicePort = port[item];
            const serviceName = filter(networkList, ['id', serviceId])[0].name;
            pathList.push({
              path: pt,
              serviceId,
              servicePort,
              serviceName,
            });
          });
          postData.pathList = pathList;
          resolve(postData);
        }
        resolve(false);
      });
    });
  }

  async function handleSubmit() {
    const postData = await formValidate();
    if (!postData) {
      return false;
    }
    try {
      let res;
      if (type === 'create') {
        res = await store.addData(projectId, postData);
      } else {
        postData.domainId = id;
        res = await store.updateData(projectId, id, postData);
      }
      if (handlePromptError(res)) {
        handleClose();
        refresh();
      } else {
        return false;
      }
    } catch (e) {
      Choerodon.handleResponseError(e);
      return false;
    }
  }

  function handleClose() {
    store.setNetwork([]);
    store.setSingleData(null);
    store.setCertificates([]);
    form.resetFields();
  }

  return (
    <div className="c7n-region">
      <DomainForm
        form={form}
        type={type}
        envId={envId}
        ingressId={id}
        DomainStore={store}
      />
    </div>
  );
}));

export default Form.create()(inject('AppState')(createDomain));
