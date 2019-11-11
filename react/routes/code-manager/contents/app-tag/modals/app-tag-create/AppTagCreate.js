import React, { useEffect, Fragment, useState } from 'react';
import { observer } from 'mobx-react-lite';
import _ from 'lodash';

import { Icon, Form, TextField, Select } from 'choerodon-ui/pro';

import MdEditor from '../../../../../../components/MdEditor';
import Tips from '../../../../../../components/new-tips';

import '../index.less';


const { Option, OptGroup } = Select;
export default observer((props) => {
  const {
    appTagStore,
    tagStore,
    projectId,
    appServiceId,
  } = props;
  const { appTagCreateDs, formatMessage } = appTagStore;
  const [release, setRelease] = useState('');
  const [size, setSize] = useState(3);

  function handleNoteChange(value) {
    setRelease(value);
    appTagCreateDs.current.set('release', value);
  }

  function changeSize(e) {
    e.stopPropagation();
    setSize(size + 10);
    tagStore.queryBranchData({ projectId, appServiceId, size: size + 10 });
  }
  
  return <Fragment>
    <Form layout="vertical" dataSet={appTagCreateDs} className="c7n-sidebar-form c7n-tag-create" style={{ width: '5.12rem' }}>
      <div className="apptag-formitem">
        <Icon type="local_offer" className="c7n-apptag-icon" />
        <TextField
          name="tag"
          addonAfter={<Tips helpText={formatMessage({ id: 'apptag.name.tip' })} />}
        />
      </div>
      <div className="apptag-formitem">
        <Icon type="wrap_text" className="c7n-apptag-icon" />
        <Select
          name="ref"
          addonAfter={<Tips helpText={formatMessage({ id: 'apptag.tip' })} />}
          renderer={renderer}
          optionRenderer={optionRenderer}
        >
          <OptGroup label={formatMessage({ id: 'apptag.branch' })}>
            {
          _.map(tagStore.branchData, (item) => (<Option
            key={item.branchName}
            value={item.branchName}
            title={item.branchName}
          >
            {item.branchName}
          </Option>))
        }
            {(tagStore.branchTotal > size && size > 0) ? <Option key="more" title={formatMessage({ id: 'loadMore' })}>
              <div
                role="none"
                onClick={changeSize}
                className="c7n-option-popover c7n-dom-more"
              >{formatMessage({ id: 'loadMore' })}</div>
            </Option> : null}
          </OptGroup>
        </Select>
      </div>
    </Form>
    <div className="c7n-creation-title">{formatMessage({ id: 'apptag.release.title' })}</div>
    <MdEditor
      value={release}
      textMaxLength={1000}
      onChange={handleNoteChange}
    />
  </Fragment>;
});

const renderer = ({ text }) => (
  !text ? null
    : <Fragment><Icon className="apptag-branch-icon" type="branch" />{text}</Fragment>
);

const optionRenderer = (data) => {
  const { text } = data;
  if (typeof text !== 'string') return text;
  return !text ? null
    : <Fragment><Icon className="apptag-branch-icon" type="branch" />{text}</Fragment>;
};
