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

import './index.less';

const { Column } = Table;

const CustomContent = observer(() => {
  const {
    prefixCls,
    intlPrefix,
    resourceStore: { getSelectedMenu: { parentId } },
  } = useResourceStore();
  const {
    customDs,
    intl: { formatMessage },
  } = useCustomStore();
  const { customStore } = useMainStore();

  const [showModal, setShowModal] = useState(false);

  function refresh() {
    customDs.query();
  }

  function renderName({ value, record }) {
    return (
      <StatusIcon
        status={record.get('commandStatus')}
        name={value}
        error={record.get('commandErrors')}
      />
    );
  }

  function renderTime({ value }) {
    return <TimePopover content={value} />;
  }

  function renderAction() {
    const buttons = [
      {
        service: [],
        text: formatMessage({ id: 'edit' }),
        action: openShow,
      },
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
