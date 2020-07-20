import React, { useMemo, useState, useEffect, Fragment } from 'react';
import { observer } from 'mobx-react-lite';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Action, Choerodon } from '@choerodon/boot';
import { Form, Select, TextField, TextArea, NumberField, SelectBox, Table, Button, Modal } from 'choerodon-ui/pro';
import { usePVCreateStore } from './stores';
import StatusDot from '../../../../components/status-dot';
import Tips from '../../../../components/new-tips';
import AddProject from './AddProject';

import './index.less';

const { Column } = Table;
const { Option } = SelectBox;
const modalKey = Modal.key();
const modalStyle = {
  width: 380,
};

const CreateForm = () => {
  const {
    formDs,
    intl: { formatMessage },
    modal,
    refresh,
    intlPrefix,
    prefixCls,
    projectTableDs,
    projectOptionsDs,
    selectDs,
  } = usePVCreateStore();

  modal.handleOk(async () => {
    try {
      if (await formDs.submit() !== false) {
        refresh();
      } else {
        return false;
      }
    } catch (error) {
      Choerodon.handleResponseError(error);
      return false;
    }
  });

  function renderClusterOption({ record, text, value }) {
    return (
      <Fragment>
        {text && <StatusDot
          active
          synchronize
          size="inner"
          connect={record.get('connect')}
        />}
        {text}
      </Fragment>
    );
  }

  function getClusterOptionProp({ record }) {
    return {
      disabled: !record.get('connect'),
    };
  }

  function renderAction({ record }) {
    const actionData = [{
      text: formatMessage({ id: `${intlPrefix}.permission.delete.title` }),
      action: () => projectTableDs.remove(record),
    }];
    return <Action data={actionData} />;
  }

  function openAddPermission() {
    Modal.open({
      key: modalKey,
      style: modalStyle,
      drawer: true,
      title: formatMessage({ id: `${intlPrefix}.project.add` }),
      children: <AddProject
        dataSet={selectDs}
        tableDs={projectTableDs}
        optionsDs={projectOptionsDs}
        intlPrefix={intlPrefix}
        prefixCls={prefixCls}
      />,
      okText: formatMessage({ id: 'add' }),
      onCancel: () => selectDs.reset(),
    });
  }

  return (
    <div className={`${prefixCls}-create-wrap`}>
      <Form dataSet={formDs} columns={3} style={{ width: '5.2rem' }}>
        <Select
          name="clusterId"
          searchable
          colSpan={3}
          clearButton={false}
          optionRenderer={renderClusterOption}
          onOption={getClusterOptionProp}
        />
        <TextField name="name" colSpan={3} disabled={!formDs.current.get('clusterId')} />
        <TextArea name="description" colSpan={3} resize="vertical" />
        <Select name="type" colSpan={3} clearButton={false} />
        <Select name="accessModes" colSpan={3} clearButton={false} />
        <NumberField name="storage" step={1} colSpan={2} />
        <Select name="unit" clearButton={false} />
        <TextField name="path" colSpan={3} />
        {formDs.current.get('type') === 'NFS' && (
          <TextField name="server" colSpan={3} />
        )}
        {formDs.current.get('type') === 'LocalPV' && (
          <Select name="clusterNodeName" clearButton={false} colSpan={3} />
        )}
      </Form>
      <div className={`${prefixCls}-create-wrap-permission`}>
        <Tips
          title={formatMessage({ id: `${intlPrefix}.share` })}
          helpText={formatMessage({ id: `${intlPrefix}.share.tips` })}
        />
      </div>
      <Form dataSet={formDs}>
        <SelectBox name="skipCheckProjectPermission" colSpan={3}>
          <Option value>
            <span className={`${prefixCls}-create-wrap-radio`}>{formatMessage({ id: `${intlPrefix}.project.all` })}</span>
          </Option>
          <Option value={false}>
            <span className={`${prefixCls}-create-wrap-radio`}>{formatMessage({ id: `${intlPrefix}.project.special` })}</span>
          </Option>
        </SelectBox>
      </Form>
      {!formDs.current.get('skipCheckProjectPermission') && (<Fragment>
        <Button
          color="primary"
          icon="add"
          onClick={openAddPermission}
          className={`${prefixCls}-permission-wrap-button`}
          disabled={!formDs.current.get('clusterId')}
        >
          <FormattedMessage id={`${intlPrefix}.project.add`} />
        </Button>
        <Table dataSet={projectTableDs} queryBar="none">
          <Column name="name" />
          <Column renderer={renderAction} />
          <Column name="code" />
        </Table>
      </Fragment>)}
    </div>
  );
};

export default injectIntl(observer(CreateForm));
