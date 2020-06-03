import React, { Fragment } from 'react';
import { observer } from 'mobx-react-lite';
import { Modal } from 'choerodon-ui/pro';
import HeaderButtons from '../../../../../../components/header-buttons';
import { useResourceStore } from '../../../../stores';
import { useModalStore } from './stores';
import { usePVCStore } from '../stores';
import CreateForm from './create-form';

const modalKey = Modal.key();
const modalStyle = {
  width: 380,
};

const PVCModals = observer(() => {
  const {
    intlPrefix,
    prefixCls,
    intl: { formatMessage },
    resourceStore: { getSelectedMenu: { parentId } },
    treeDs,
  } = useResourceStore();
  const {
    tableDs,
  } = usePVCStore();
  const {
    permissions,
    AppState: { currentMenuType: { projectId } },
  } = useModalStore();

  function refresh() {
    treeDs.query();
    tableDs.query();
  }

  function openModal() {
    Modal.open({
      key: modalKey,
      style: modalStyle,
      drawer: true,
      title: formatMessage({ id: `${intlPrefix}.create.pvc` }),
      children: <CreateForm refresh={refresh} envId={parentId} intlPrefix={intlPrefix} prefixCls={prefixCls} />,
      okText: formatMessage({ id: 'create' }),
    });
  }

  function getButtons() {
    const envRecord = treeDs.find((record) => record.get('key') === parentId);
    const connect = envRecord.get('connect');
    const disabled = !connect;

    return ([{
      permissions: ['choerodon.code.project.deploy.app-deployment.resource.ps.create-pvc'],
      name: formatMessage({ id: `${intlPrefix}.create.pvc` }),
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

export default PVCModals;
