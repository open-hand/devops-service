import React, { Fragment, useCallback, useState, useEffect, useMemo } from 'react';
import { Table, Select, Form, TextField, Icon } from 'choerodon-ui/pro';
import { injectIntl, FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import filter from 'lodash/filter';
import includes from 'lodash/includes';
import forEach from 'lodash/forEach';
import map from 'lodash/map';

import './index.less';

const { Column } = Table;
const { Option } = Select;

const SourceTable = injectIntl(observer(({ tableDs, selectedDs, store, projectId, importRecord, intl: { formatMessage }, intlPrefix, prefixCls, modal }) => {
  const selectedId = useMemo(() => selectedDs.map((record) => record.get('id')), [selectedDs]);
  const options = useMemo(() => map(store.getAllProject, ({ id, name }) => (
    <Option value={id}>{name}</Option>
  )), [store.getAllProject]);
  const [param, setParam] = useState('');
  const [searchProjectId, setSearchProjectId] = useState(null);

  useEffect(() => {
    store.loadAllProject(projectId, importRecord.get('platformType') === 'share');
    loadData();
  }, []);

  modal.handleOk(() => {
    const records = filter(tableDs.selected, (record) => !includes(selectedId, record.get('id')));
    selectedDs.push(...records);
  });

  async function loadData(urlData = {}) {
    let url = '';
    forEach(urlData, (value, key) => {
      value && (url = `${url}&${key}=${value}`);
    });
    tableDs.transport.read.url = `/devops/v1/projects/${projectId}/app_service/page_by_mode?share=${importRecord.get('platformType') === 'share'}${url}`;

    try {
      if (await tableDs.query() !== false) {
        forEach(selectedId, (id) => {
          tableDs.select(tableDs.find((tableRecord) => tableRecord.get('id') === id));
        });
      }
    } catch (e) {
      Choerodon.handleResponseError(e);
    }
  }

  function handleSelectProject(value) {
    setSearchProjectId(value);
    loadData({
      search_project_id: value,
      param,
    });
  }

  function handleChangeParam(value) {
    setParam(value);
    loadData({
      search_project_id: searchProjectId,
      param: value,
    });
  }

  return (
    <div>
      <Form columns={3}>
        <Select
          label={formatMessage({ id: `${intlPrefix}.project` })}
          onChange={handleSelectProject}
        >
          {options}
        </Select>
        <TextField
          onChange={handleChangeParam}
          colSpan={2}
          prefix={<Icon type="search" />}
          placeholder="请输入查询条件"
        />
      </Form>
      <Table
        dataSet={tableDs}
        queryBar="none"
      >
        <Column name="name" />
        <Column name="code" />
        <Column name="projectName" />
      </Table>
    </div>
  );
}));

export default SourceTable;
