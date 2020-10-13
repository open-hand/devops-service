import React, { useMemo, useState } from 'react';
import { observer } from 'mobx-react-lite';
import {
  Form, Password, SelectBox, TextField,
} from 'choerodon-ui/pro';
import pick from 'lodash/pick';
import Tips from '@/components/new-tips';
import HostConfigApis from '@/routes/host-config/apis';
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
    projectId,
    refresh,
    hostId,
    HAS_BASE_PRO,
  } = useCreateHostStore();

  modal.handleOk(async () => {
    try {
      const res = await formDs.submit();
      if (res) {
        refresh();
        return true;
      }
      return false;
    } catch (e) {
      return false;
    }
  });

  const handleTestConnection = async () => {
    try {
      const validate = await formDs.validate();
      const record = formDs.current;
      // @ts-ignore
      // 单独再次校验密码是因为：修改时无任何操作，formDs.validate() 返回为true
      if (!validate || !record || await record.getField('password').checkValidity() === false) {
        return false;
      }
      const postData = pick(record.toData(), ['type', 'authType', 'hostIp', 'sshPort', 'username', 'password', 'jmeterPort']);
      modal.update({
        okProps: { disabled: true },
      });
      record.set('status', 'operating');
      if (postData.type === 'deploy') {
        postData.jmeterPort = null;
      } else {
        record.set('jmeterStatus', 'operating');
      }
      record.set('hostStatus', 'operating');
      const res = await HostConfigApis.testConnection(projectId, postData);
      modal.update({
        okProps: { disabled: false },
      });
      if (res) {
        const {
          hostStatus, jmeterStatus, hostCheckError, jmeterCheckError,
        } = res;
        // eslint-disable-next-line no-nested-ternary
        const status = [hostStatus, jmeterStatus].includes('failed') ? 'failed' : hostStatus === 'success' && jmeterStatus === 'success' ? 'success' : 'operating';
        record.set('hostStatus', hostStatus);
        record.set('jmeterStatus', jmeterStatus);
        record.set('hostCheckError', hostCheckError);
        record.set('jmeterCheckError', jmeterCheckError);
        record.set('status', postData.type === 'deploy' ? hostStatus : status);
      }
      return true;
    } catch (e) {
      const record = formDs.current;
      if (record) {
        record.set('hostStatus', 'wait');
        record.set('jmeterStatus', 'wait');
        record.set('status', 'wait');
      }
      modal.update({
        okProps: { disabled: false },
      });
      return false;
    }
  };

  return (
    <div className={`${prefixCls}`}>
      <Form dataSet={formDs} className={`${prefixCls}-form`}>
        {HAS_BASE_PRO && <SelectBox name="type" disabled={!!hostId} />}
        <TextField name="name" />
        <TextField name="hostIp" />
        <TextField name="sshPort" />
        <Tips
          title={formatMessage({ id: `${intlPrefix}.account` })}
          className={`${prefixCls}-module-title ${prefixCls}-module-title-radio`}
          showHelp={false}
        />
        <SelectBox name="authType" />
        <TextField name="username" />
        {formDs && formDs.current && formDs.current.get('authType') === 'publickey' ? (
          <TextField name="password" />
        ) : <Password name="password" reveal={false} />}
        {formDs && formDs.current && formDs.current.get('type') === 'distribute_test' && ([
          <Tips
            title={formatMessage({ id: `${intlPrefix}.jmeter` })}
            className={`${prefixCls}-module-title`}
            showHelp={false}
          />,
          <TextField name="jmeterPort" />,
          <TextField name="jmeterPath" />,
        ])}
      </Form>
      {formDs && formDs.current && formDs.current.get('type') === 'distribute_test' && (
        <JmeterGuide />
      )}
      <TestConnect handleTestConnection={handleTestConnection} />
    </div>
  );
});

export default CreateHost;
