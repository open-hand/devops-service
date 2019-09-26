import React, { Fragment, useState } from 'react';
import { FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import { Action } from '@choerodon/master';
import { Table } from 'choerodon-ui/pro';
import StatusIcon from '../../../../../components/StatusIcon';
import TimePopover from '../../../../../components/timePopover';
import { useResourceStore } from '../../../stores';
import { useCustomStore } from './stores';
import Modals from './modals';
import CustomForm from './modals/form-view';
import { useMainStore } from '../../stores';
import ResourceListTitle from '../../components/resource-list-title';

import './index.less';

const { Column } = Table;

const CustomContent = observer(() => {
  const {
    prefixCls,
    intlPrefix,
    resourceStore: { getSelectedMenu: { parentId } },
    treeDs,
  } = useResourceStore();
  const {
    customDs,
    intl: { formatMessage },
  } = useCustomStore();
  const { customStore } = useMainStore();

  const [showModal, setShowModal] = useState(false);

  function refresh() {
    treeDs.query();
    customDs.query();
  }

  function getEnvIsNotRunning() {
    const envRecord = treeDs.find((record) => record.get('key') === parentId);
    const connect = envRecord.get('connect');
    return !connect;
  }

  function renderName({ value, record }) {
    const commandStatus = record.get('commandStatus');
    const error = record.get('commandErrors');
    const disabled = getEnvIsNotRunning() || commandStatus === 'operating';
    return (
      <StatusIcon
        status={commandStatus}
        name={value}
        error={error}
        clickAble={!disabled}
        onClick={openShow}
        permissionCode={['devops-service.devops-customize-resource.createResource']}
      />
    );
  }

  function renderTime({ value }) {
    return <TimePopover content={value} />;
  }

  function renderAction({ record }) {
    const commandStatus = record.get('commandStatus');
    const disabled = getEnvIsNotRunning() || commandStatus === 'operating';
    if (disabled) {
      return null;
    }
    const buttons = [
      {
        service: ['devops-service.devops-customize-resource.deleteResource'],
        text: formatMessage({ id: 'delete' }),
        action: handleDelete,
      },
    ];

    return (<Action data={buttons} />);
  }

  function openShow() {
    setShowModal(true);
  }

  function closeModal(isLoad) {
    setShowModal(false);
    isLoad && refresh();
  }

  function handleDelete() {
    customDs.delete(customDs.current);
  }

  return (
    <div className={`${prefixCls}-ingress-table`}>
      <Modals />
      <ResourceListTitle type="customResources" />
      <Table
        dataSet={customDs}
        border={false}
        queryBar="bar"
      >
        <Column name="name" renderer={renderName} />
        <Column renderer={renderAction} width="0.7rem" />
        <Column name="k8sKind" />
        <Column name="lastUpdateDate" renderer={renderTime} />
      </Table>
      {showModal && <CustomForm
        id={customDs.current.get('id')}
        envId={parentId}
        type="edit"
        store={customStore}
        visible={showModal}
        onClose={closeModal}
      />}
    </div>
  );
});

export default CustomContent;
