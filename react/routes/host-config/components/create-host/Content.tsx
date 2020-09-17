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
      if (!validate || !record) {
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
      record.set('sshStatus', 'operating');
      const res = await HostConfigApis.testConnection(projectId, postData);
      modal.update({
        okProps: { disabled: false },
      });
      if (res) {
        const { sshStatus, jmeterStatus } = res;
        // eslint-disable-next-line no-nested-ternary
        const status = [sshStatus, jmeterStatus].includes('failed') ? 'failed' : sshStatus === 'success' && jmeterStatus === 'success' ? 'success' : 'operating';
        record.set('sshStatus', sshStatus);
        record.set('jmeterStatus', jmeterStatus);
        record.set('status', postData.type === 'deploy' ? sshStatus : status);
      }
      return true;
    } catch (e) {
      const record = formDs.current;
      if (record) {
        record.set('sshStatus', 'wait');
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
      <Form dataSet={formDs}>
        <SelectBox name="type" disabled={!!hostId} />
        <TextField name="name" />
        <TextField name="hostIp" />
        <TextField name="sshPort" />
        <Tips
          title={formatMessage({ id: `${intlPrefix}.account` })}
          className={`${prefixCls}-module-title ${prefixCls}-module-title-radio`}
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
