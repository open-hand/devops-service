import React, { Fragment, useCallback, useState, useEffect, useMemo } from 'react';
import { Action } from '@choerodon/boot';
import { Table, Modal, Select } from 'choerodon-ui/pro';
import { injectIntl, FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import { Button, Icon, Tooltip } from 'choerodon-ui';
import map from 'lodash/map';
import classnames from 'classnames';
import SourceTable from './SourceTable';
import Tips from '../../../../../components/new-tips';

const { Column } = Table;
const { Option } = Select;

const modalKey1 = Modal.key();
const modalStyle1 = {
  width: 740,
};

const Platform = injectIntl(observer((props) => {
  const { tableDs, selectedDs, intl: { formatMessage }, intlPrefix, prefixCls, appServiceStore, projectId, record: importRecord, checkData } = props;

  function openModal() {
    Modal.open({
      key: modalKey1,
      drawer: true,
      title: <Tips
        helpText={formatMessage({ id: `${intlPrefix}.add.tips` })}
        title={formatMessage({ id: `${intlPrefix}.add` })}
      />,
      children: <SourceTable
        tableDs={tableDs}
        selectedDs={selectedDs}
        intlPrefix={intlPrefix}
        prefixCls={prefixCls}
        store={appServiceStore}
        projectId={projectId}
        importRecord={importRecord}
        checkData={checkData}
      />,
      style: modalStyle1,
      okText: formatMessage({ id: 'add' }),
      afterClose: () => {
        tableDs.removeAll();
        selectedDs.length && checkData();
      },
    });
  }

  function renderNameOrCode({ value, name, record }) {
    const flag = name === 'name' ? record.get('nameFailed') : record.get('codeFailed');
    const nameClass = classnames({
      [`${prefixCls}-import-platform-input`]: true,
      [`${prefixCls}-import-platform-input-failed`]: flag,
      'c7n-pro-output-invalid': flag,
    });
    return <span className={nameClass}>{value}</span>;
  }

  function renderVersion({ value, record }) {
    return (
      <Select
        record={record}
        name="versionId"
        searchable
        searchMatcher="version"
        clearButton={false}
        className={`${prefixCls}-import-platform-table-select`}
      />
    );
  }

  function renderAction() {
    return (
      <Tooltip title={formatMessage({ id: 'delete' })}>
        <Button shape="circle" icon="delete" onClick={handleDelete} />
      </Tooltip>
    );
  }

  function handleDelete() {
    selectedDs.remove(selectedDs.current);
    selectedDs.length && checkData();
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
        <Tips
          helpText={formatMessage({ id: `${intlPrefix}.import.tips` })}
          title={formatMessage({ id: `${intlPrefix}.selected` }, { number: selectedDs.length })}
        />
      </div>
      <Table
        dataSet={selectedDs}
        queryBar="none"
      >
        <Column name="name" editor renderer={renderNameOrCode} />
        <Column name="code" editor renderer={renderNameOrCode} />
        <Column name="versionId" renderer={renderVersion} align="left" />
        <Column name="projectName" width="1.5rem" header={formatMessage({ id: `${intlPrefix}.belong.${importRecord.get('platformType')}` })} />
        <Column renderer={renderAction} width="0.7rem" />
      </Table>
    </div>
  );
}));

export default Platform;
