import React from 'react';
import { observer } from 'mobx-react-lite';
import { Button, Form, Icon, Select, SelectBox, TextField } from 'choerodon-ui/pro';
import map from 'lodash/map';
import { useManualDeployStore } from './stores';

const { Option } = SelectBox;

export default observer(() => {
  const {
    intl: { formatMessage },
    prefixCls,
    manualDeployDs,
    portsDs,
    networkDs,
  } = useManualDeployStore();

  const record = networkDs.current;

  function handleRemovePort(portRecord) {
    portsDs.remove(portRecord);
  }

  function handlePortAdd() {
    portsDs.create();
  }

  return (
    <div className={`${prefixCls}-resource-network`}>
      <Form dataSet={networkDs}>
        <TextField
          name="name"
          disabled={!manualDeployDs.current.get('envId')}
        />
        <SelectBox name="type">
          <Option value="ClusterIP"><span className="type-span">ClusterIP</span></Option>
          <Option value="NodePort"><span className="type-span">NodePort</span></Option>
          <Option value="LoadBalancer">LoadBalancer</Option>
        </SelectBox>
      </Form>
      {map(portsDs.data, (portRecord) => (
        <Form record={portRecord} key={portRecord.id} columns={5}>
          {
            record.get('type') !== 'ClusterIP'
            && <TextField name="nodePort" />
          }
          <TextField name="port" />
          <TextField name="targetPort" />
          {
            record.get('type') === 'NodePort'
            && <Select name="protocol" clearButton={false} />
          }
          {
            portsDs.length > 1 ? <Button
              funcType="flat"
              icon="delete"
              onClick={() => handleRemovePort(portRecord)}
              className={`${prefixCls}-resource-delete-btn`}
            /> : <span colSpan={3} />
          }
        </Form>
      ))}
      <Button
        color="primary"
        funcType="flat"
        onClick={handlePortAdd}
        icon="add"
      >
        {formatMessage({ id: 'network.config.addport' })}
      </Button>
    </div>
  );
});
