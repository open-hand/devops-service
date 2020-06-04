import React, { Fragment } from 'react';
import { Action } from '@choerodon/boot';
import { Table, Modal, SelectBox, Form, Icon } from 'choerodon-ui/pro';
import { Button } from 'choerodon-ui';
import { injectIntl, FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import AddProject from './AddProject';
import Tips from '../../../../components/new-tips';
import { useCertPermissionStore } from './stores';

import './index.less';

const { Column } = Table;
const { Option } = SelectBox;
const modalKey = Modal.key();
const modalStyle = {
  width: 380,
};

export default injectIntl(observer(() => {
  const {
    detailDs,
    allProjectDs,
    permissionProjectDs,
    optionsDs,
    intlPrefix,
    prefixCls,
    intl: { formatMessage },
    modal,
    refresh,
  } = useCertPermissionStore();
  const record = detailDs.current;
  if (!record) return;

  modal.handleOk(async () => {
    try {
      if (await detailDs.submit() !== false) {
        refresh();
      } else {
        return false;
      }
    } catch (e) {
      return false;
    }
  });

  function openModal() {
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
        detailDs={detailDs}
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
      title: formatMessage({ id: 'c7ncd.deployment.permission.delete.title' }),
      children: formatMessage({ id: 'c7ncd.deployment.permission.project.delete.des' }),
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
      <Tips
        helpText={formatMessage({ id: `${intlPrefix}.share.tips` })}
        title={formatMessage({ id: `${intlPrefix}.share` })}
      />
      <Form dataSet={detailDs}>
        <SelectBox name="skipCheckProjectPermission">
          <Option value>
            <span className={`${prefixCls}-permission-wrap-radio`}>{formatMessage({ id: `${intlPrefix}.project.all` })}</span>
          </Option>
          <Option value={false}>
            <span className={`${prefixCls}-permission-wrap-radio`}>
              <Tips
                helpText={formatMessage({ id: `${intlPrefix}.some.tips` })}
                title={formatMessage({ id: `${intlPrefix}.project.some` })}
              />
            </span>
          </Option>
        </SelectBox>
      </Form>
      {!record.get('skipCheckProjectPermission') ? (
        <Fragment>
          <Button
            type="primary"
            icon="add"
            onClick={openModal}
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
      ) : (
        <Table dataSet={allProjectDs}>
          <Column name="name" sortable />
          <Column name="code" sortable />
        </Table>
      )}
    </div>
  );
}));
