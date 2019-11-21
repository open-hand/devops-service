import React, { useEffect, Fragment } from 'react';
import { observer } from 'mobx-react-lite';
import { TextField, Form, Button, Icon, Select, SelectBox } from 'choerodon-ui/pro';
import _ from 'lodash';
import useNetWorkStore from './stores';

import './index.less';

const { Option } = Select;

function FormContent() {
  const { 
    formDs,
    portDs,
    targetLabelsDs,
    appInstanceOptionsDs,
    keyOptionsDs,
    modal,
    intl: {
      formatMessage,
    },
  } = useNetWorkStore();

  useEffect(() => {
    appInstanceOptionsDs.query();
    keyOptionsDs.query();
  }, []);

  const current = formDs.current;

  modal.handleOk(() => {
    formDs.submit();
    return false;
  });

  function createPortGroup() {
    portDs.create();
  }

  function removePortGroup(record) {
    portDs.remove(record);
  }

  function createTargetLabelGroup() {
    targetLabelsDs.create();
  }

  function removeTargetLabelGroup(record) {
    targetLabelsDs.remove(record);
  }

  function handleTypeChange(value, oldvalue) {
    portDs.getField('protocol').set('required', value === 'NodePort');
    portDs.reset();
    if (value !== 'ClusterIP') {
      current.set('externalIps', null);
    }
  }

  function handleTargetChange(value, oldvalue) {
    const isParam = value === 'param';
    formDs.getField('appInstance').set('required', !isParam);
    targetLabelsDs.getField('keyword').set('required', isParam);
    targetLabelsDs.getField('value').set('required', isParam);
    if (isParam) {
      current.set('appInstance', null);
    } else {
      targetLabelsDs.reset();
    }
  }

  function targetPortOptionRenderer({ record, text, value }) {
    if (!record.get('resourceName')) return value;
    return `${record.get('resourceName')}: ${value}`;
  }


  function labelOptionRenderer({ record, text, value }) {
    return `${record.get('meaning')}`;
  }


  return (
    <Fragment>
      <div className="c7ncd-create-network">
        <Form dataSet={formDs} columns={3}>
          <TextField name="name" colSpan={3} maxLength={30} />
          <div
            className="network-panel-title"
            colSpan={3}
          >
            <span>{formatMessage({ id: 'network.target' })}</span>
          </div>
          <SelectBox name="target" colSpan={3} onChange={handleTargetChange}>
            <Option value="instance">{formatMessage({ id: 'network.target.instance' })}</Option>
            <Option value="param">{formatMessage({ id: 'network.target.param' })}</Option>
          </SelectBox>
          <div colSpan={3} className="target-form">
            {
              (current && current.get('target') === 'instance')
                ? <Fragment>
                  <Select name="appInstance" colSpan={3} className="app-instance-select" />
                </Fragment> : <Fragment>
                  {
                    _.map(targetLabelsDs.created, (record, index) => (<Form record={record} key={`target-label-record-${index}`} columns={4}>
                      <Select name="keyword" combo optionRenderer={labelOptionRenderer} />
                      <Icon className="network-group-icon" type="drag_handle" />
                      <Select name="value" combo optionRenderer={labelOptionRenderer} />
                      {
                        targetLabelsDs.created.length > 1 ? <Icon
                          colSpan={1}
                          className="delete-icon-target"
                          type="delete"
                          onClick={removeTargetLabelGroup.bind(this, record)}
                        /> : <span colSpan={1} />
                      }
                    </Form>))
                  }
                  <Button
                    color="primary"
                    funcType="flat"
                    onClick={createTargetLabelGroup}
                    icon="add"
                  >
                    {formatMessage({ id: 'network.config.addtarget' })}
                  </Button>
                </Fragment>
            }
          </div>
          <div
            className="network-panel-title"
          >
            <span>{formatMessage({ id: 'network.config' })}</span>
          </div>
          <SelectBox name="type" record={current} onChange={handleTypeChange} newLine colSpan={3}>
            <Option value="ClusterIP">ClusterIP</Option>
            <Option value="NodePort">NodePort</Option>
            <Option value="LoadBalancer">LoadBalancer</Option>
          </SelectBox>
          {current.get('type') === 'ClusterIP'
          && <TextField name="externalIps" record={current} colSpan={3} />}
        </Form>
        <div className="group-port">
          {
            _.map(portDs.created, (record, index) => (<Form record={record} key={`port-record-${index}`} columns={5}>
              
              {
                current.get('type') !== 'ClusterIP'
                  && <TextField name="nodeport" maxLength={5} />
              }
              <TextField name="port" maxLength={5} />
              <Select name="targetport" combo optionRenderer={targetPortOptionRenderer} clearButton={false} />
              {
                current.get('type') === 'NodePort'
                && <Select name="protocol" clearButton={false}>
                  {_.map(['TCP', 'UDP'], (item) => (
                    <Option value={item} key={item}>
                      {item}
                    </Option>
                  ))}
                </Select>
              }
              {
                portDs.created.length > 1 ? <Icon
                  colSpan={3}
                  className="delete-icon"
                  type="delete"
                  onClick={removePortGroup.bind(this, record)}
                /> : <span colSpan={3} />
              }
            </Form>))
          }
          <Button
            color="primary"
            funcType="flat"
            onClick={createPortGroup}
            icon="add"
          >
            {formatMessage({ id: 'network.config.addport' })}
          </Button>
        </div>
      </div>
    </Fragment>
  );
}

export default observer(FormContent);
