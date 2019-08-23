import React, { Fragment, useCallback, useState, useEffect } from 'react';
import { Action } from '@choerodon/master';
import { Table, Modal } from 'choerodon-ui/pro';
import { injectIntl, FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import { Button, Icon } from 'choerodon-ui';
import EditForm from './EditForm';
import SourceTable from './SourceTable';

const { Column } = Table;
const modalKey1 = Modal.key();
const modalKey2 = Modal.key();
const modalStyle = {
  width: 380,
};

const Platform = injectIntl(observer((props) => {
  const { tableDs, intl: { formatMessage }, intlPrefix, prefixCls, versionOptions, projectId } = props;

  function openModal() {
    Modal.open({
      key: modalKey1,
      drawer: true,
      title: formatMessage({ id: `${intlPrefix}.import` }),
      children: <SourceTable {...props} />,
      style: modalStyle,
      okText: formatMessage({ id: 'add' }),
      onCancel: handleCancelAdd,
    });
  }

  function openEdit() {
    Modal.open({
      key: modalKey2,
      drawer: true,
      title: formatMessage({ id: `${intlPrefix}.edit` }),
      children: <EditForm
        record={tableDs.current}
        versionOptions={versionOptions}
        projectId={projectId}
      />,
      style: modalStyle,
      okText: formatMessage({ id: 'save' }),
      onCancel: handleCancelEdit,
    });
  }

  function renderName({ value, record }) {
    return (
      <span>
        {value}
        {record.get('nameFailed') && <Icon type="info" className={`${prefixCls}-import-platform-failed`} />}
      </span>
    );
  }

  function renderCode({ value, record }) {
    return (
      <span>
        {value}
        {record.get('codeFailed') && <Icon type="info" className={`${prefixCls}-import-platform-failed`} />}
      </span>
    );
  }

  function handleCancelEdit() {
    tableDs.current.reset();
    tableDs.current.set('selected', true);
  }

  function handleCancelAdd() {
    tableDs.reset();
  }

  function renderType({ value }) {
    return <FormattedMessage id={`${intlPrefix}.type.${value}`} />;
  }

  function renderShare({ value }) {
    return <FormattedMessage id={`${intlPrefix}.source.${value}`} />;
  }

  function renderAction() {
    const actionData = [
      {
        service: [],
        text: formatMessage({ id: 'edit' }),
        action: openEdit,
      },
      {
        service: [],
        text: formatMessage({ id: 'delete' }),
        action: handleDelete,
      },
    ];
    return <Action data={actionData} />;
  }

  function handleDelete() {
    tableDs.current.set('selected', false);
  }

  function selectedFilter(tableRecord) {
    return tableRecord.get('selected') && tableRecord.get('appId');
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
      <Table
        dataSet={tableDs}
        filter={selectedFilter}
        queryBar="none"
      >
        <Column name="name" renderer={renderName} />
        <Column renderer={renderAction} width="0.7rem" />
        <Column name="code" renderer={renderCode} />
        <Column name="appName" />
        <Column name="share" renderer={renderShare} />
        <Column name="type" renderer={renderType} />
        <Column name="version" />
      </Table>
    </div>
  );
}));

export default Platform;
