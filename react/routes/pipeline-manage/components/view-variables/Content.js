import React from 'react';
import { observer } from 'mobx-react-lite';
import { Form, Icon, TextField, Tooltip } from 'choerodon-ui/pro';
import { CopyToClipboard } from 'react-copy-to-clipboard';
import { Choerodon } from '@choerodon/boot';
import { useRecordDetailStore } from './stores';
import Loading from '../../../../components/loading';

import './index.less';

export default observer(() => {
  const {
    intl: { formatMessage },
    intlPrefix,
    prefixCls,
    projectFormDs,
    appFormDs,
  } = useRecordDetailStore();

  function handleCopy() {
    Choerodon.prompt(formatMessage({ id: 'copy_success' }));
  }

  function getFormContent(ds) {
    return (
      ds.map((eachRecord) => (
        <Form record={eachRecord} columns={13} key={eachRecord.id}>
          <TextField
            colSpan={9}
            name="key"
            disabled
            addonAfter={
              <Tooltip title={formatMessage({ id: 'copy' })} placement="top">
                <CopyToClipboard
                  text={eachRecord.get('key')}
                  onCopy={handleCopy}
                >
                  <Icon type="content_copy" style={{ cursor: 'pointer' }} />
                </CopyToClipboard>
              </Tooltip>
            }
          />
          <span className={`${prefixCls}-equal`}>=</span>
          <TextField
            colSpan={3}
            value="******"
            label={formatMessage({ id: 'value' })}
            disabled
          />
        </Form>
      ))
    );
  }

  if (!projectFormDs || projectFormDs.status === 'loading') {
    return <Loading display />;
  }

  return (<div className={`${prefixCls}`}>
    <div className={`${prefixCls}-title`}>
      {formatMessage({ id: `${intlPrefix}.settings.project` })}
    </div>
    {projectFormDs.length ? getFormContent(projectFormDs) : (
      <span className={`${prefixCls}-title-empty`}>{formatMessage({ id: `${intlPrefix}.settings.project.empty` })}</span>
    )}
    <div className={`${prefixCls}-title-app`}>
      {formatMessage({ id: `${intlPrefix}.settings.app` })}
    </div>
    {appFormDs.length ? getFormContent(appFormDs) : (
      <span className={`${prefixCls}-title-empty`}>{formatMessage({ id: `${intlPrefix}.settings.app.empty` })}</span>
    )}
  </div>);
});
