import React, { useCallback, Fragment, useEffect, useMemo } from 'react';
import { Action } from '@choerodon/boot';
import { Table, Modal, SelectBox, Form, Icon, Button } from 'choerodon-ui/pro';
import { injectIntl, FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import AddProject from './AddProject';

import './index.less';
import { usePVPermissionStore } from './stores';

const { Column } = Table;
const { Option } = SelectBox;
const modalKey = Modal.key();
const modalStyle = {
  width: 380,
};

export default injectIntl(observer(() => {
  const {
    allProjectDs,
    permissionProjectDs,
    DetailDs,
    optionsDs,
    intlPrefix,
    prefixCls,
    intl: { formatMessage },
    modal,
    refresh,
  } = usePVPermissionStore();

  const record = DetailDs.current;

  modal.handleOk(async () => {
    try {
      if (await DetailDs.submit() !== false) {
        refresh();
      } else {
        return false;
      }
    } catch (e) {
      return false;
    }
  });

  function openAddPermission() {
    Modal.open({
      key: modalKey,
      style: modalStyle,
      drawer: true,
      title: formatMessage({ id: `${intlPrefix}.project.add` }),
      children: <AddProject
        dataSet={permissionProjectDs}
        optionsDs={optionsDs}
        intlPrefix={intlPrefix}
        prefixCls={prefixCls}
      />,
      okText: formatMessage({ id: 'add' }),
      onCancel: handleCancel,
    });
  }

  function handleCancel() {
    permissionProjectDs.reset();
  }

  function handleDelete() {
    const modalProps = {
      title: formatMessage({ id: `${intlPrefix}.permission.delete.title` }),
      children: formatMessage({ id: `${intlPrefix}.permission.delete.des` }),
      okText: formatMessage({ id: 'delete' }),
      okProps: { color: 'red' },
      cancelProps: { color: 'dark' },
    };
    permissionProjectDs.delete(permissionProjectDs.current, modalProps);
  }

  function renderAction() {
    const actionData = [{
      service: [],
      text: formatMessage({ id: `${intlPrefix}.permission.delete` }),
      action: handleDelete,
    }];
    return <Action data={actionData} />;
  }

  return (
    <div className={`${prefixCls}-permission-wrap`}>
      <Form dataSet={DetailDs}>
        <SelectBox name="skipCheckProjectPermission">
          <Option value>
            <span className={`${prefixCls}-permission-wrap-radio`}>{formatMessage({ id: `${intlPrefix}.project.all` })}</span>
          </Option>
          <Option value={false}>
            <span className={`${prefixCls}-permission-wrap-radio`}>{formatMessage({ id: `${intlPrefix}.project.special` })}</span>
          </Option>
        </SelectBox>
      </Form>
      {record && record.get('skipCheckProjectPermission') ? (
        <Table dataSet={allProjectDs}>
          <Column name="name" sortable />
          <Column name="code" sortable />
        </Table>
      ) : (
        <Fragment>
          <Button
            color="primary"
            icon="add"
            onClick={openAddPermission}
            className={`${prefixCls}-permission-wrap-button`}
          >
            <FormattedMessage id={`${intlPrefix}.project.add`} />
          </Button>
          <Table dataSet={permissionProjectDs} pristine>
            <Column name="name" sortable />
            <Column renderer={renderAction} />
            <Column name="code" sortable />
          </Table>
        </Fragment>
      )}
    </div>
  );
}));
