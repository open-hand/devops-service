import React, { useMemo, useState, useCallback } from 'react';
import { FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import { Action } from '@choerodon/master';
import {
  Button,
  Popover,
} from 'choerodon-ui';
import { Table } from 'choerodon-ui/pro';
import MouserOverWrapper from '../../../../../components/MouseOverWrapper/MouserOverWrapper';
import StatusTags from '../../../../../components/status-tag';
import TimePopover from '../../../../../components/timePopover/TimePopover';
import { useResourceStore } from '../../../stores';
import { useKeyValueStore } from './stores';
import Modals from './modals';

import './index.less';
import KeyValueModal from '../application/modals/key-value';

const { Column } = Table;

const ConfigMap = observer((props) => {
  const {
    prefixCls,
    intlPrefix,
    resourceStore: { getSelectedMenu: { parentId } },
    treeDs,
  } = useResourceStore();
  const {
    intl: { formatMessage },
    itemType,
    permissions,
    formStore,
    SecretTableDs,
    ConfigMapTableDs,
  } = useKeyValueStore();

  const [showModal, setShowModal] = useState(false);

  function refresh() {
    treeDs.query();
    if (itemType === 'configMap') {
      return ConfigMapTableDs.query();
    }
    return SecretTableDs.query();
  }

  function renderName({ value, record }) {
    const commandStatus = record.get('commandStatus');
    return (
      <div>
        <StatusTags
          name={formatMessage({ id: commandStatus || 'null' })}
          colorCode={commandStatus || 'success'}
          style={{ minWidth: 40, marginRight: '0.08rem', height: '0.16rem', lineHeight: '0.16rem' }}
        />
        <span>{value}</span>
      </div>
    );
  }

  function renderKey({ value = [], record }) {
    return (
      <MouserOverWrapper width={0.5}>
        {value.join(',')}
      </MouserOverWrapper>
    );
  }
  function renderValue({ value = [] }) {
    return (
      <MouserOverWrapper width={0.5}>
        {value && JSON.stringify(value)}
      </MouserOverWrapper>
    );
  }

  function renderDate({ value }) {
    return <TimePopover content={value} />;
  }

  function renderAction() {
    const buttons = [
      {
        service: permissions.edit,
        text: formatMessage({ id: 'edit' }),
        action: openModal,
      },
      {
        service: permissions.delete,
        text: formatMessage({ id: 'delete' }),
        action: handleDelete,
      },
    ];
    return <Action data={buttons} />;
  }

  function handleDelete() {
    if (itemType === 'configMap') {
      return ConfigMapTableDs.delete(ConfigMapTableDs.current);
    }
    return SecretTableDs.delete(SecretTableDs.current);
  }

  function openModal() {
    setShowModal(true);
  }

  function closeModal(isLoad) {
    setShowModal(false);
    isLoad && refresh();
  }
  const store = itemType === 'configMap' ? ConfigMapTableDs : SecretTableDs;
  return (
    <div className={`${prefixCls}-keyValue-table`}>
      <Modals />
      {itemType === 'configMap'
        ? <Table
          dataSet={store}
          border={false}
          queryBar="bar"
          key="1"
        >
          <Column name="name" header={formatMessage({ id: `${intlPrefix}.${itemType}` })} renderer={renderName} />
          <Column renderer={renderAction} />
          <Column name="key" renderer={renderKey} />
          <Column name="lastUpdateDate" renderer={renderDate} />
        </Table>
        : <Table
          dataSet={store}
          border={false}
          queryBar="bar"
          key="2"
        >
          <Column name="name" header={formatMessage({ id: `${intlPrefix}.${itemType}` })} renderer={renderName} />
          <Column renderer={renderAction} />
          <Column name="key" renderer={renderKey} />
          <Column name="lastUpdateDate" renderer={renderDate} />
        </Table>}
      {showModal && <KeyValueModal
        modeSwitch={itemType === 'configMap'}
        title={itemType}
        visible={showModal}
        id={store.current.get('id')}
        envId={parentId}
        onClose={closeModal}
        store={formStore}
      />}
    </div>
  );
});

export default ConfigMap;
