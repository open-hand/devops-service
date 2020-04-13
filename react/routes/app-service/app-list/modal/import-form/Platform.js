import React, { Fragment, useCallback, useState, useEffect, useMemo } from 'react';
import { Table, Modal, Select } from 'choerodon-ui/pro';
import { injectIntl, FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import { Button, Icon, Tooltip } from 'choerodon-ui';
import SourceTable from './SourceTable';
import Tips from '../../../../../components/new-tips';
import { useImportAppServiceStore } from './stores';

const { Column } = Table;
const { Option } = Select;

const modalKey1 = Modal.key();
const modalStyle1 = {
  width: 740,
};

const Platform = injectIntl(observer(({ checkData }) => {
  const {
    AppState: { currentMenuType: { projectId } },
    intl: { formatMessage },
    intlPrefix,
    prefixCls,
    importDs,
    importTableDs,
    selectedDs,
    importStore,
  } = useImportAppServiceStore();
  const importRecord = useMemo(() => importDs.current || importDs.records[0], [importDs.current]);

  function openModal() {
    Modal.open({
      key: modalKey1,
      drawer: true,
      title: <Tips
        helpText={formatMessage({ id: `${intlPrefix}.add.tips` })}
        title={formatMessage({ id: `${intlPrefix}.add` })}
      />,
      children: <SourceTable
        tableDs={importTableDs}
        selectedDs={selectedDs}
        intlPrefix={intlPrefix}
        prefixCls={prefixCls}
        store={importStore}
        projectId={projectId}
        importRecord={importRecord}
      />,
      style: modalStyle1,
      okText: formatMessage({ id: 'add' }),
      afterClose: () => {
        importTableDs.removeAll();
        if (selectedDs.length) {
          checkData();
          selectedDs.forEach((record) => {
            record.getField('versionId').fetchLookup();
          });
        }
      },
    });
  }

  function renderVersion({ record }) {
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
        <Column name="name" editor />
        <Column name="code" editor />
        <Column name="versionId" renderer={renderVersion} align="left" />
        <Column name="projectName" width="1.5rem" header={formatMessage({ id: `${intlPrefix}.belong.${importRecord.get('platformType')}` })} />
        <Column renderer={renderAction} width="0.7rem" />
      </Table>
    </div>
  );
}));

export default Platform;
