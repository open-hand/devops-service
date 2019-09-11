import React, { useEffect, useState } from 'react';
import { observer } from 'mobx-react-lite';
import { Select, Table } from 'choerodon-ui/pro';
import { useResourceStore } from '../../../stores';
import { useREStore } from './stores';
import TimePopover from '../../../../../components/timePopover';
import StatusTag from '../../../../../components/status-tag';

const { Column } = Table;
const { Option } = Select;

export default observer(() => {
  const {
    prefixCls,
    intlPrefix,
    resourceStore: { getSelectedMenu: { id } },
  } = useResourceStore();
  const {
    AppState: { currentMenuType: { id: projectId } },
    intl: { formatMessage },
    tableDs,
  } = useREStore();

  const [sortType, setSortType] = useState('memory');

  useEffect(() => {
    tableDs.transport.read.url = `/devops/v1/projects/${projectId}/pods/pod_ranking?env_id=${id}&sort=${sortType}`;
    tableDs.query();
  }, [sortType]);

  function changeSortType(value) {
    setSortType(value);
  }

  function renderName({ value, record }) {
    const status = record.get('status');
    const wrapStyle = {
      width: 54,
    };

    const statusMap = {
      Completed: [true, '#00bf96'],
      Running: [false, '#00bf96'],
      Error: [false, '#f44336'],
      Pending: [false, '#ff9915'],
    };

    const [wrap, color] = statusMap[status] || [true, 'rgba(0, 0, 0, 0.36)'];

    return (
      <div>
        <StatusTag
          ellipsis={wrap}
          color={color}
          name={status}
          style={wrapStyle}
        />
        <span>{value}</span>
      </div>
    );
  }

  function renderDate({ value }) {
    return <TimePopover content={value} />;
  }

  return (
    <div className={`${prefixCls}-re-card`}>
      <div className={`${prefixCls}-re-card-title`}>{formatMessage({ id: `${intlPrefix}.resource.dosage` })}</div>
      <Select
        value={sortType}
        onChange={changeSortType}
        clearButton={false}
        className={`${prefixCls}-re-select`}
      >
        <Option value="memory">{formatMessage({ id: `${intlPrefix}.sort.memory` })}</Option>
        <Option value="cpu">{formatMessage({ id: `${intlPrefix}.sort.cpu` })}</Option>
      </Select>
      <Table dataSet={tableDs} queryBar="none">
        <Column name="name" renderer={renderName} />
        <Column name="instanceName" width="1.5rem" />
        <Column name="memoryUsed" width="1rem" />
        <Column name="cpuUsed" width="1rem" />
        <Column name="podIp" width="1.2rem" />
        <Column name="creationDate" renderer={renderDate} width="0.8rem" />
      </Table>
    </div>
  );
});
