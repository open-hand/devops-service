import React, { useMemo, useState, useCallback } from 'react';
import { FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import { Action } from '@choerodon/master';
import { Table } from 'choerodon-ui/pro';
import { keys } from 'lodash';
import MouserOverWrapper from '../../../../../components/MouseOverWrapper/MouserOverWrapper';
import StatusTags from '../../../../../components/status-tag';
import TimePopover from '../../../../../components/timePopover/TimePopover';
import { useResourceStore } from '../../../stores';
import { useKeyValueStore } from './stores';
import Modals from './modals';
import KeyValueModal from '../application/modals/key-value';
import { useMainStore } from '../../stores';
import ClickText from '../../../../../components/click-text';
import ResourceListTitle from '../../components/resource-list-title';

import './index.less';

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
  const { mainStore: { openDeleteModal } } = useMainStore();

  const [showModal, setShowModal] = useState(false);

  function refresh() {
    treeDs.query();
    if (itemType === 'configMap') {
      return ConfigMapTableDs.query();
    }
    return SecretTableDs.query();
  }

  function getEnvIsNotRunning() {
    const envRecord = treeDs.find((record) => record.get('key') === parentId);
    const connect = envRecord.get('connect');
    return !connect;
  }

  function renderName({ value, record }) {
    const commandStatus = record.get('commandStatus');
    const disabled = getEnvIsNotRunning() || commandStatus === 'operating';
    return (
      <div>
        <StatusTags
          name={formatMessage({ id: commandStatus || 'null' })}
          colorCode={commandStatus || 'success'}
          style={{ minWidth: 40, marginRight: '0.08rem', height: '0.16rem', lineHeight: '0.16rem' }}
        />
        <MouserOverWrapper width={0.4} text={value}>
          <ClickText
            value={value}
            clickAble={!disabled}
            onClick={openModal}
            permissionCode={permissions.edit}
          />
        </MouserOverWrapper>
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
    const keyarr = keys(value);
    return (
      <MouserOverWrapper width={0.5}>
        {keyarr && keyarr.join(',')}
      </MouserOverWrapper>
    );
  }

  function renderDate({ value }) {
    return <TimePopover content={value} />;
  }

  function renderAction({ record }) {
    const commandStatus = record.get('commandStatus');
    const disabled = getEnvIsNotRunning() || commandStatus === 'operating';
    if (disabled) {
      return null;
    }
    const id = record.get('id');
    const name = record.get('name');
    const type = itemType === 'configMap' ? itemType : 'secret';
    const buttons = [
      {
        service: permissions.delete,
        text: formatMessage({ id: 'delete' }),
        action: () => openDeleteModal(parentId, id, name, type, refresh),
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
      <ResourceListTitle type={itemType === 'configMap' ? 'configMaps' : 'secrets'} />
      {itemType === 'configMap'
        ? <Table
          dataSet={store}
          border={false}
          queryBar="bar"
          key="1"
        >
          <Column name="name" header={formatMessage({ id: `${intlPrefix}.${itemType}` })} renderer={renderName} />
          <Column renderer={renderAction} width="0.7rem" />
          <Column name="key" renderer={renderKey} />
          <Column name="lastUpdateDate" renderer={renderDate} width="1rem" />
        </Table>
        : <Table
          dataSet={store}
          border={false}
          queryBar="bar"
          key="2"
        >
          <Column name="name" header={formatMessage({ id: `${intlPrefix}.${itemType}` })} renderer={renderName} />
          <Column renderer={renderAction} width="0.7rem" />
          <Column name="value" renderer={renderValue} header={formatMessage({ id: 'key' })} />
          <Column name="lastUpdateDate" renderer={renderDate} width="1rem" />
        </Table>}
      {showModal && <KeyValueModal
        modeSwitch={itemType === 'configMap'}
        title={itemType}
        visible={showModal}
        id={store.current.get('id')}
        envId={parentId}
        onClose={closeModal}
        store={formStore}
        intlPrefix={intlPrefix}
      />}
    </div>
  );
});

export default ConfigMap;
