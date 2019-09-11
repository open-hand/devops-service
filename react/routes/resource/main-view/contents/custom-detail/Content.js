import React, { useState, Fragment, useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import { Icon } from 'choerodon-ui';
import uuidv1 from 'uuid/v1';
import Loading from '../../../../../components/loading';
import { removeEndsChar } from '../../../../../utils';
import { useResourceStore } from '../../../stores';
import { useCustomDetailStore } from './stores';
import Modals from './modals';

import './index.less';

const TIMEOUT_TIME = 50000;

const Content = observer(() => {
  const { prefixCls } = useResourceStore();
  const [value, setValue] = useState('');
  const [loading, setLoading] = useState(true);
  const { detailDs } = useCustomDetailStore();
  let ws;
  let retry = false;

  useEffect(() => {
    initSocket();
    return () => {
      if (ws) {
        destroySocket();
      }
    };
  }, [detailDs.current]);

  function destroySocket() {
    ws.removeEventListener('message', handleMessage);
    ws.removeEventListener('error', handleError);
    ws.close();
  }

  function initSocket() {
    setLoading(true);
    const record = detailDs.current;
    if (record) {
      const id = record.get('id');
      const clusterId = record.get('clusterId');
      const name = record.get('name');
      const kind = record.get('k8sKind');
      const env = record.get('envCode');
      try {
        const wsHost = removeEndsChar(window._env_.DEVOPS_HOST, '/');
        const url = `${wsHost}/devops/describe?key=cluster:${clusterId}.describe:${uuidv1()}&env=${env}&kind=${kind}&name=${name}&describeId=${id}`;
        ws = new WebSocket(url);
        ws.addEventListener('message', handleMessage);
        ws.addEventListener('error', handleError);
      } catch (e) {
        setValue('请求失败，请稍后重试！');
      }
    }
  }

  function handleMessage({ data }) {
    try {
      setValue(JSON.parse(data).data);
      setLoading(false);
      destroySocket();
    } catch (e) {
      setValue('请求失败，请稍后重试！');
    }
  }

  function handleError(e) {
    setValue(JSON.stringify(e));

    if (!retry) {
      retry = true;
      setTimeout(() => {
        retry = false;
        destroySocket();
        initSocket();
      }, TIMEOUT_TIME);
    }
  }

  function getTitle() {
    const record = detailDs.current;
    return record ? <Fragment>
      <div className={`${prefixCls}-detail-content-title`}>
        <Icon type="filter_b_and_w" className={`${prefixCls}-detail-content-title-icon`} />
        <span>{record.get('name')}</span>
      </div>
      <div className={`${prefixCls}-detail-content-section-title`}>
        <span>Description</span>
      </div>
    </Fragment> : null;
  }
  return (
    <div className={`${prefixCls}-custom-detail`}>
      <Modals />
      {getTitle()}
      <pre className="custom-detail-section-content">
        {loading ? <Loading display /> : value}
      </pre>
    </div>
  );
});

export default Content;
