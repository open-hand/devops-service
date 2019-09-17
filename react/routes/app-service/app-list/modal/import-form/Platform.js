import React, { Fragment, useCallback, useState, useEffect } from 'react';
import { Action } from '@choerodon/master';
import { Table, Modal, Select } from 'choerodon-ui/pro';
import { injectIntl, FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import { Button, Icon, Tooltip } from 'choerodon-ui';
import map from 'lodash/map';
import SourceTable from './SourceTable';

const { Column } = Table;
const { Option } = Select;

const modalKey1 = Modal.key();
const modalStyle1 = {
  width: 740,
};

const Platform = injectIntl(observer((props) => {
  const { tableDs, selectedDs, intl: { formatMessage }, intlPrefix, prefixCls, AppStore, projectId, record: importRecord } = props;

  function openModal() {
    Modal.open({
      key: modalKey1,
      drawer: true,
      title: formatMessage({ id: `${intlPrefix}.import` }),
      children: <SourceTable
        tableDs={tableDs}
        selectedDs={selectedDs}
        intlPrefix={intlPrefix}
        prefixCls={prefixCls}
        store={AppStore}
        projectId={projectId}
        importRecord={importRecord}
      />,
      style: modalStyle1,
      okText: formatMessage({ id: 'add' }),
      afterClose: () => tableDs.removeAll(),
    });
  }

  function renderName({ value, record }) {
    return (
      <div
        className={`${prefixCls}-import-wrap-column ${prefixCls}-import-wrap-column-name `}
      >
        <span className={`${prefixCls}-import-wrap-column-text`}>{value}</span>
        {record.get('nameFailed') && (
          <Tooltip title={formatMessage({ id: `${intlPrefix}.import.failed` })}>
            <Icon type="info" className={`${prefixCls}-import-platform-failed`} />
          </Tooltip>
        )}
      </div>
    );
  }

  function renderCode({ value, record }) {
    return (
      <div className={`${prefixCls}-import-wrap-column`}>
        <span className={`${prefixCls}-import-wrap-column-text`}>{value}</span>
        {record.get('codeFailed') && (
          <Tooltip title={formatMessage({ id: `${intlPrefix}.import.failed` })}>
            <Icon type="info" className={`${prefixCls}-import-platform-failed`} />
          </Tooltip>
        )}
      </div>
    );
  }

  function renderVersion({ value, record }) {
    const { id: versionId } = value ? record.get('versions')[0] : {};
    const selectOptions = map(value, ({ id, version }) => (
      <Option value={id}>{version}</Option>
    ));

    return (
      <Select
        value={record.get('versionId') || versionId}
        onChange={handleChangeVersion}
        clearButton={false}
        className={`${prefixCls}-import-platform-table-select`}
      >
        {selectOptions}
      </Select>
    );
  }

  function renderAction() {
    return <Button shape="circle" icon="delete" onClick={handleDelete} />;
  }
  
  function handleChangeVersion(value) {
    selectedDs.current.set('versionId', value);
  }

  function handleDelete() {
    selectedDs.remove(selectedDs.current);
  }

  return (
    <div className={`${prefixCls}-import-platform`}>
      <Button
        funcType="raised"
        icon="add"
        onClick={openModal}
        className="platform-button"
      >
        <FormattedMessage id={`${intlPrefix}.add`} />
      </Button>
      <div className={`${prefixCls}-import-platform-selected`}>
        <FormattedMessage id={`${intlPrefix}.selected`} values={{ number: selectedDs.length }} />
      </div>
      <Table
        dataSet={selectedDs}
        queryBar="none"
      >
        <Column name="name" editor />
        <Column name="code" editor />
        <Column name="versions" renderer={renderVersion} />
        <Column name="projectName" width="1.5rem" />
        <Column renderer={renderAction} width="0.7rem" />
      </Table>
    </div>
  );
}));

export default Platform;
