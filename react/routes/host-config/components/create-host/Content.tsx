import React, { useMemo } from 'react';
import { observer } from 'mobx-react-lite';
import {
  Form, Password, SelectBox, TextField,
} from 'choerodon-ui/pro';
import Tips from '@/components/new-tips';
import { useCreateHostStore } from './stores';
import JmeterGuide from './components/jmeter-guide';
import TestConnect from './components/test-connect';

const CreateHost: React.FC<any> = observer((): any => {
  const {
    prefixCls,
    formDs,
    intlPrefix,
    modal,
    formatMessage,
  } = useCreateHostStore();

  return (
    <div className={`${prefixCls}`}>
      <Form dataSet={formDs}>
        <SelectBox name="type" />
        <TextField name="name" />
        <TextField name="ip" />
        <TextField name="port" />
        <Tips
          title={formatMessage({ id: `${intlPrefix}.account` })}
          className={`${prefixCls}-module-title ${prefixCls}-module-title-radio`}
        />
        <SelectBox name="account" />
        <TextField name="userName" />
        {formDs && formDs.current && formDs.current.get('account') === 'token' ? (
          <TextField name="token" />
        ) : <Password name="password" />}
        {formDs && formDs.current && formDs.current.get('type') === 'test' && ([
          <Tips
            title={formatMessage({ id: `${intlPrefix}.jmeter` })}
            className={`${prefixCls}-module-title`}
          />,
          <TextField name="jmeterPort" />,
          <TextField name="jmeterPath" />,
        ])}
      </Form>
      <JmeterGuide />
      <TestConnect />
    </div>
  );
});

export default CreateHost;
