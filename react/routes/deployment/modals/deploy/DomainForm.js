import React from 'react';
import { observer } from 'mobx-react-lite';
import { Button, Form, Icon, Select, SelectBox, TextField } from 'choerodon-ui/pro';
import map from 'lodash/map';
import { useManualDeployStore } from './stores';
import Tips from '../../../../components/new-tips';

const { Option } = SelectBox;

export default observer(() => {
  const {
    intl: { formatMessage },
    prefixCls,
    manualDeployDs,
    pathListDs,
    domainDs,
  } = useManualDeployStore();

  const record = domainDs.current;
  const envId = manualDeployDs.current.get('environmentId');

  function handleAddPath() {
    pathListDs.create();
  }

  function handleRemovePath(removeRecord) {
    pathListDs.remove(removeRecord);
  }

  return (
    <div className={`${prefixCls}-resource-domain`}>
      <Form dataSet={domainDs}>
        <TextField name="name" disabled={!envId} />
        <SelectBox name="isNormal">
          <Option value>
            <span className={`${prefixCls}-manual-deploy-radio`}>{formatMessage({ id: 'domain.protocol.normal' })}</span>
          </Option>
          <Option value={false}>
            <span className={`${prefixCls}-manual-deploy-radio`}>{formatMessage({ id: 'domain.protocol.secret' })}</span>
          </Option>
        </SelectBox>
        <TextField name="domain" disabled={!envId} />
        {!record.get('isNormal') && <Select name="certId" disabled={!envId} searchable />}
      </Form>
      {map(pathListDs.data, (pathRecord) => (
        <Form record={pathRecord} columns={6} style={{ width: '115%' }} key={pathRecord.id}>
          <TextField name="path" colSpan={2} disabled={!record.get('domain')} />
          <TextField name="serviceName" colSpan={2} disabled />
          <Select name="servicePort" disabled={!pathRecord.get('serviceName')}>
            {map(pathRecord.get('ports'), (port) => <Option value={port} key={port}>{port}</Option>)}
          </Select>
          {pathListDs.length > 1 ? (
            <Button
              funcType="flat"
              icon="delete"
              className={`${prefixCls}-domain-form-delete`}
              onClick={() => handleRemovePath(pathRecord)}
            />
          ) : <span />}
        </Form>
      ))}
      <Button
        funcType="flat"
        color="primary"
        icon="add"
        onClick={handleAddPath}
      >
        {formatMessage({ id: 'domain.path.add' })}
      </Button>
    </div>
  );
});
