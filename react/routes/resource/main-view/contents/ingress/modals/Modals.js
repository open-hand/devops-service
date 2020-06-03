import React, { Fragment, useState } from 'react';
import { observer } from 'mobx-react-lite';
import { Modal } from 'choerodon-ui/pro';
import HeaderButtons from '../../../../../../components/header-buttons';
import { useResourceStore } from '../../../../stores';
import { useModalStore } from './stores';
import { useIngressStore } from '../stores';
import DomainForm from '../../../components/domain-form';
import { useMainStore } from '../../../stores';

const modalKey = Modal.key();
const modalStyle = {
  width: 740,
};

const EnvModals = observer(() => {
  const {
    intlPrefix,
    intl: { formatMessage },
    prefixCls,
    resourceStore,
    treeDs,
  } = useResourceStore();
  const {
    ingressDs,
  } = useIngressStore();
  const { ingressStore } = useMainStore();
  const { permissions } = useModalStore();
  const { parentId } = resourceStore.getSelectedMenu;

  function refresh() {
    treeDs.query();
    ingressDs.query();
  }

  function openModal() {
    Modal.open({
      key: modalKey,
      style: modalStyle,
      drawer: true,
      title: formatMessage({ id: 'domain.create.head' }),
      children: <DomainForm
        envId={parentId}
        refresh={refresh}
        intlPrefix={intlPrefix}
        prefixCls={prefixCls}
        ingressStore={ingressStore}
      />,
      okText: formatMessage({ id: 'create' }),
    });
  }

  function getButtons() {
    const envRecord = treeDs.find((record) => record.get('key') === parentId);
    const connect = envRecord.get('connect');
    const disabled = !connect;

    return ([{
      permissions: ['choerodon.code.project.deploy.app-deployment.resource.ps.domain'],
      name: formatMessage({ id: `${intlPrefix}.create.ingress` }),
      icon: 'playlist_add',
      handler: openModal,
      display: true,
      group: 1,
      service: permissions,
      disabled,
    }, {
      name: formatMessage({ id: 'refresh' }),
      icon: 'refresh',
      handler: refresh,
      display: true,
      group: 1,
    }]);
  }

  return (
    <Fragment>
      <HeaderButtons items={getButtons()} />
    </Fragment>
  );
});

export default EnvModals;
