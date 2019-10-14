import React, { useMemo, useState } from 'react';
import { observer } from 'mobx-react-lite';
import { Action } from '@choerodon/boot';
import { Modal, Table } from 'choerodon-ui/pro';
import MouserOverWrapper from '../../../../../../components/MouseOverWrapper/MouserOverWrapper';
import StatusTags from '../../../../../../components/status-tag';
import TimePopover from '../../../../../../components/timePopover/TimePopover';
import KeyValueModal from '../modals/key-value';
import { useResourceStore } from '../../../../stores';
import { useApplicationStore } from '../stores';
import ClickText from '../../../../../../components/click-text';
import { useMainStore } from '../../../stores';

import './index.less';

const { Column } = Table;
const modalKey = Modal.key();
const modalStyle = {
  width: 'calc(100vw - 3.52rem)',
};

const AppConfigs = observer(() => {
  const {
    intl: { formatMessage },
    prefixCls,
    intlPrefix,
    resourceStore: { getSelectedMenu: { id, parentId } },
    treeDs,
  } = useResourceStore();
  const { mappingStore, mappingDs } = useApplicationStore();
  const { mainStore: { openDeleteModal } } = useMainStore();
  const statusStyle = useMemo(() => ({ marginRight: '0.08rem' }), []);

  function refresh() {
    treeDs.query();
    return mappingDs.query();
  }

  function getEnvIsNotRunning() {
    const envRecord = treeDs.find((record) => record.get('key') === parentId);
    const connect = envRecord.get('connect');
    return !connect;
  }

  function handleEdit(record) {
    Modal.open({
      key: modalKey,
      style: modalStyle,
      drawer: true,
      title: formatMessage({ id: `${intlPrefix}.configMap.edit` }),
      children: <KeyValueModal
        modeSwitch
        intlPrefix={intlPrefix}
        title="mapping"
        id={record.get('id')}
        envId={parentId}
        appId={id}
        store={mappingStore}
        refresh={refresh}
      />,
      okText: formatMessage({ id: 'save' }),
    });
  }

  function renderName({ value, record }) {
    const commandStatus = record.get('commandStatus');
    const disabled = getEnvIsNotRunning() || commandStatus === 'operating';
    return (
      <div>
        <StatusTags
          name={formatMessage({ id: commandStatus || 'null' })}
          colorCode={commandStatus || 'success'}
          style={statusStyle}
        />
        <ClickText
          value={value}
          clickAble={!disabled}
          onClick={handleEdit}
          record={record}
          permissionCode={['devops-service.devops-config-map.update']}
        />
      </div>
    );
  }

  function renderKey({ value = [] }) {
    return (
      <MouserOverWrapper width={0.5}>
        {value.join(',')}
      </MouserOverWrapper>
    );
  }

  function renderDate({ value }) {
    return <TimePopover content={value} />;
  }

  function renderAction({ record }) {
    const commandStatus = record.get('commandStatus');
    const configId = record.get('id');
    const name = record.get('name');
    const disabled = getEnvIsNotRunning() || commandStatus === 'operating';
    if (disabled) {
      return null;
    }
    const buttons = [
      {
        service: ['devops-service.devops-config-map.delete'],
        text: formatMessage({ id: 'delete' }),
        action: () => openDeleteModal(parentId, configId, name, 'configMap', refresh),
      },
    ];
    return <Action data={buttons} />;
  }

  return (
    <div className={`${prefixCls}-mapping-content`}>
      <div className="c7ncd-tab-table">
        <Table
          dataSet={mappingDs}
          border={false}
          queryBar="bar"
        >
          <Column name="name" header={formatMessage({ id: `${intlPrefix}.application.tabs.mapping` })} renderer={renderName} />
          <Column renderer={renderAction} width="0.7rem" />
          <Column name="key" renderer={renderKey} />
          <Column name="lastUpdateDate" renderer={renderDate} width="1rem" />
        </Table>
      </div>
    </div>
  );
});

export default AppConfigs;
