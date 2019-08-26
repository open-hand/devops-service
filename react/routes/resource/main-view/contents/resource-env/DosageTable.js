import React, { useEffect, useState } from 'react';
import { observer } from 'mobx-react-lite';
import { Select, Table } from 'choerodon-ui/pro';
import { useResourceStore } from '../../../stores';
import { useREStore } from './stores';

const { Column } = Table;
const { Option } = Select;

export default observer(() => {
  const {
    prefixCls,
    intlPrefix,
    resourceStore: { getSelectedMenu: { menuId } },
  } = useResourceStore();
  const {
    AppState: { currentMenuType: { id } },
    intl: { formatMessage },
    tableDs,
  } = useREStore();

  const [sortType, setSortType] = useState('memory');

  useEffect(() => {
    tableDs.transport.read.url = `/devops/v1/projects/${id}/pods/pod_ranking?env_id=${menuId}&sort=${sortType}`;
    tableDs.query();
  }, [sortType]);

  function changeSortType(value) {
    setSortType(value);
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
        <Column name="podName" />
        <Column name="instanceName" />
        <Column name="memoryUsed" />
        <Column name="cpuUsed" />
        <Column name="podIp" />
        <Column name="creationDate" />
      </Table>
    </div>
  );
});
