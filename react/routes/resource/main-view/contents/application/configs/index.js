import React, { useMemo, useState } from 'react';
import { observer } from 'mobx-react-lite';
import { Action } from '@choerodon/master';
import { Table } from 'choerodon-ui/pro';
import MouserOverWrapper from '../../../../../../components/MouseOverWrapper/MouserOverWrapper';
import StatusTags from '../../../../../../components/status-tag';
import TimePopover from '../../../../../../components/timePopover/TimePopover';
import KeyValueModal from '../modals/key-value';
import { useResourceStore } from '../../../../stores';
import { useApplicationStore } from '../stores';

import './index.less';
import ClickText from '../../../../../../components/click-text';

const { Column } = Table;

const AppConfigs = observer(() => {
  const {
    intl: { formatMessage },
    prefixCls,
    intlPrefix,
    resourceStore: { getSelectedMenu: { id, parentId } },
    treeDs,
  } = useResourceStore();
  const { mappingStore, mappingDs } = useApplicationStore();
  const statusStyle = useMemo(() => ({ marginRight: '0.08rem' }), []);
  const [showModal, setShowModal] = useState(false);
  const [recordId, setRecordId] = useState(null);

  function refresh() {
    treeDs.query();
    return mappingDs.query();
  }

  function getEnvIsNotRunning() {
    const envRecord = treeDs.find((record) => record.get('key') === parentId);
    const connect = envRecord.get('connect');
    return !connect;
  }

  function closeSideBar(fresh) {
    setRecordId(null);
    setShowModal(false);
    fresh && refresh();
  }

  function handleEdit(record) {
    setRecordId(record.get('id'));
    setShowModal(true);
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
    const disabled = getEnvIsNotRunning() || commandStatus === 'operating';
    if (disabled) {
      return null;
    }
    const buttons = [
      {
        service: ['devops-service.devops-config-map.delete'],
        text: formatMessage({ id: 'delete' }),
        action: () => {
          mappingDs.delete(record);
        },
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
          <Column renderer={renderAction} />
          <Column name="key" renderer={renderKey} />
          <Column name="lastUpdateDate" renderer={renderDate} />
        </Table>
      </div>
      {showModal && <KeyValueModal
        modeSwitch
        intlPrefix={intlPrefix}
        title="mapping"
        visible={showModal}
        id={recordId}
        envId={parentId}
        appId={id}
        onClose={closeSideBar}
        store={mappingStore}
      />}
    </div>
  );
});

export default AppConfigs;
