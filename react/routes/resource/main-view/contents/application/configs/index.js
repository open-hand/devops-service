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
    return mappingDs.query();
  }

  function getEnvIsNotRunning() {
    const envRecord = treeDs.find((record) => record.get('key') === parentId);
    const connect = envRecord.get('connect');
    const synchronize = envRecord.get('synchronize');
    return !connect || !synchronize;
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
    const disabled = getEnvIsNotRunning();
    const commandStatus = record.get('commandStatus');
    return (
      <div>
        <StatusTags
          name={formatMessage({ id: commandStatus || 'null' })}
          colorCode={commandStatus || 'success'}
          style={statusStyle}
        />
        {disabled ? value : <a className="content-table-name" onClick={() => handleEdit(record)}>{value}</a>}
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
    const buttons = [
      {
        service: [],
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
      <Table
        dataSet={mappingDs}
        border={false}
        queryBar="bar"
      >
        <Column name="name" header={formatMessage({ id: `${intlPrefix}.application.tabs.mapping` })} renderer={renderName} />
        {!getEnvIsNotRunning() ? <Column renderer={renderAction} /> : null}
        <Column name="key" renderer={renderKey} />
        <Column name="lastUpdateDate" renderer={renderDate} />
      </Table>
      {showModal && <KeyValueModal
        modeSwitch
        title="configMap"
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
