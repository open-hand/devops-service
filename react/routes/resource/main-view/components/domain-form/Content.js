import React, { Fragment, useMemo } from 'react';
import { observer } from 'mobx-react-lite';
import { Form, Select, TextField, SelectBox, Button, Icon, Tooltip, Spin } from 'choerodon-ui/pro';
import { Choerodon } from '@choerodon/boot';
import map from 'lodash/map';
import { useDomainFormStore } from './stores';
import Tips from '../../../../../components/new-tips';

import './index.less';

const { Option } = Select;

export default observer(() => {
  const {
    formDs,
    pathListDs,
    serviceDs,
    annotationDs,
    intl: { formatMessage },
    prefixCls,
    modal,
    refresh,
    saveNetworkIds,
  } = useDomainFormStore();

  const record = useMemo(() => formDs.current, [formDs.current]);
  if (!record) {
    return <Spin />;
  }
  const isModify = record.status !== 'add';

  function formValidate() {
    return new Promise((resolve) => {
      pathListDs.forEach(async (pathRecord) => {
        const res = await pathRecord.getField('serviceId').checkValidity();
        if (!res) {
          resolve(false);
        } else {
          resolve(true);
        }
      });
    });
  }

  modal.handleOk(async () => {
    const serviceIds = map(pathListDs.toData(), 'serviceId');
    saveNetworkIds && saveNetworkIds(serviceIds);
    try {
      if (!isModify || await formValidate() === true) {
        if (await formDs.submit() !== false) {
          refresh();
          return true;
        } else {
          return false;
        }
      } else {
        return false;
      }
    } catch (e) {
      Choerodon.handleResponseError(e);
      return false;
    }
  });

  function handleAddPath() {
    pathListDs.create();
  }

  function handleAddAnnotation() {
    annotationDs.create();
  }

  function handleRemovePath(removeRecord) {
    pathListDs.remove(removeRecord);
  }

  function handleRemoveAnnotation(annotationRecord) {
    annotationDs.remove(annotationRecord);
  }

  function renderService({ serviceRecord, text }) {
    const status = serviceRecord.get('status');
    const serviceError = serviceRecord.get('serviceError');
    return (
      <Fragment>
        <Tooltip title={text}>
          <span className={`${prefixCls}-domain-form-network-text`}>{text}</span>
        </Tooltip>
        {status && status !== 'running' && (
          <Tooltip title={serviceError ? `failed: ${serviceError}` : formatMessage({ id: status })}>
            <Icon type="error" className={`${prefixCls}-domain-form-network-status-icon`} />
          </Tooltip>
        )}
      </Fragment>
    );
  }

  function serviceOptionRender({ record: serviceRecord, text }) {
    const content = renderService({ serviceRecord, text });
    return content;
  }
  
  function serviceRender({ text, value }) {
    const serviceRecord = serviceDs.find((eachRecord) => eachRecord.get('id') === value);
    if (!serviceRecord) {
      return text;
    } else {
      const content = renderService({ serviceRecord, text });
      return content;
    }
  }

  return (
    <div className={`${prefixCls}-domain-form`}>
      <Form dataSet={formDs}>
        <TextField name="name" autoFocus={!isModify} disabled={isModify} />
        <div className={`${prefixCls}-domain-form-text`}>
          <Tips
            title={formatMessage({ id: 'domain.protocol' })}
            helpText={formatMessage({ id: 'domain.protocol.tip' })}
          />
        </div>
        <SelectBox name="isNormal" className={`${prefixCls}-domain-form-radio`}>
          <Option value>
            <span className={`${prefixCls}-domain-form-radio-text`}>{formatMessage({ id: 'domain.protocol.normal' })}</span>
          </Option>
          <Option value={false}>
            <span className={`${prefixCls}-domain-form-radio-text`}>{formatMessage({ id: 'domain.protocol.secret' })}</span>
          </Option>
        </SelectBox>
        <TextField name="domain" autoFocus={isModify} />
        {!record.get('isNormal') && <Select name="certId" searchable />}
      </Form>
      {map(pathListDs.data, (pathRecord) => (
        <Form record={pathRecord} columns={6} style={{ width: '115%' }} key={pathRecord.id}>
          <TextField name="path" colSpan={2} disabled={!record.get('domain')} />
          <Select name="serviceId" searchable colSpan={2} optionRenderer={serviceOptionRender} renderer={serviceRender} />
          <Select name="servicePort">
            {map(pathRecord.get('ports'), ({ port }) => <Option value={port} key={port}>{port}</Option>)}
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
      <div className={`${prefixCls}-domain-form-annotation-title`}>
        Annotations
      </div>
      {map(annotationDs.data, (annotationRecord) => (
        <Form columns={14} record={annotationRecord} style={{ width: '103.3%' }} key={annotationRecord.id}>
          <TextField colSpan={3} name="domain" />
          <span className={`${prefixCls}-domain-form-annotation-equal`}>/</span>
          <TextField colSpan={3} name="key" />
          <span className={`${prefixCls}-domain-form-annotation-equal`}>=</span>
          <TextField colSpan={5} name="value" />
          {annotationDs.length > 1 ? (
            <Button
              funcType="flat"
              icon="delete"
              onClick={() => handleRemoveAnnotation(annotationRecord)}
            />
          ) : <span />}
        </Form>
      ))}
      <Button
        funcType="flat"
        color="primary"
        icon="add"
        onClick={handleAddAnnotation}
      >
        {formatMessage({ id: 'domain.annotation.add' })}
      </Button>
    </div>
  );
});
