import React, { Fragment, useCallback, useState, useEffect } from 'react';
import { Action } from '@choerodon/master';
import { Table, Modal } from 'choerodon-ui/pro';
import { injectIntl, FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import { Button, Icon, Tooltip } from 'choerodon-ui';
import EditForm from './EditForm';
import SourceTable from './SourceTable';

const { Column } = Table;
const modalKey1 = Modal.key();
const modalKey2 = Modal.key();
const modalStyle1 = {
  width: '70%',
};
const modalStyle2 = {
  width: 380,
};

const Platform = injectIntl(observer((props) => {
  const { tableDs, selectedDs, intl: { formatMessage }, intlPrefix, prefixCls, versionOptions, projectId } = props;

  function openModal() {
    const importModal = Modal.open({
      key: modalKey1,
      drawer: true,
      title: <div className={`${prefixCls}-import-source`}>
        <Icon
          type="keyboard_backspace"
          className={`${prefixCls}-import-source-icon`}
          onClick={() => importModal.close()}
        />
        <FormattedMessage id={`${intlPrefix}.import`} />
      </div>,
      children: <SourceTable
        tableDs={tableDs}
        selectedDs={selectedDs}
        intlPrefix={intlPrefix}
        prefixCls={prefixCls}
      />,
      style: modalStyle1,
      okText: formatMessage({ id: 'add' }),
      afterClose: () => tableDs.removeAll(),
    });
  }

  function openEdit() {
    Modal.open({
      key: modalKey2,
      drawer: true,
      title: formatMessage({ id: `${intlPrefix}.edit` }),
      children: <EditForm
        record={selectedDs.current}
        versionOptions={versionOptions}
        projectId={projectId}
      />,
      style: modalStyle2,
      okText: formatMessage({ id: 'save' }),
      onCancel: handleCancelEdit,
    });
  }

  function renderName({ value, record }) {
    return (
      <div
        className={`${prefixCls}-import-wrap-column ${prefixCls}-import-wrap-column-name `}
        onClick={openEdit}
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

  function handleCancelEdit() {
    selectedDs.current.reset();
  }

  function renderType({ value }) {
    return value && <FormattedMessage id={`${intlPrefix}.type.${value}`} />;
  }

  function renderShare({ value }) {
    return <FormattedMessage id={`${intlPrefix}.source.${value}`} />;
  }

  function renderAction() {
    const actionData = [
      {
        service: [],
        text: formatMessage({ id: 'delete' }),
        action: handleDelete,
      },
    ];
    return <Action data={actionData} />;
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
        <Column name="name" renderer={renderName} />
        <Column renderer={renderAction} width="0.5rem" />
        <Column name="code" renderer={renderCode} />
        <Column name="appName" />
        <Column name="share" renderer={renderShare} width="0.8rem" align="left" />
        <Column name="type" renderer={renderType} width="0.8rem" />
        <Column name="version" header={<FormattedMessage id={`${intlPrefix}.version`} />} />
      </Table>
    </div>
  );
}));

export default Platform;
