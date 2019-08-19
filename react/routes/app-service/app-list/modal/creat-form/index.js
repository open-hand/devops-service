import React, { Fragment, useCallback, useState, useEffect } from 'react';
import { Form, TextField, Select, Upload } from 'choerodon-ui/pro';
import { injectIntl, FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import { Icon, Input, Button, Avatar } from 'choerodon-ui';
import { axios } from '@choerodon/master';
import pick from 'lodash/pick';
import isEmpty from 'lodash/isEmpty';
import Settings from './Settings';

import './index.less';
import { handlePromptError } from '../../../../../utils';

const { Option } = Select;

const CreateForm = injectIntl(observer((props) => {
  const { dataSet, record, AppStore, projectId, intl: { formatMessage }, intlPrefix, prefixCls } = props;
  const isModify = record.status !== 'add';

  props.modal.handleOk(async () => {
    if (isModify) {
      if (record.get('harborStatus') === 'failed' || record.get('chartStatus') === 'failed') return false;
      const harborTestFailed = record.get('harborType') === 'custom' && !isEmpty(record.get('harbor')) && !record.get('harborStatus') && !await handleTestHarbor();
      const chartTestFailed = record.get('chartType') === 'custom' && !isEmpty(record.get('chart')) && !record.get('chartStatus') && !await handleTestChart();
      if (!harborTestFailed && !chartTestFailed) {
        const res = await handleCreate();
        return res;
      } else {
        return false;
      }
    } else {
      const res = await handleCreate();
      return res;
    }
  });

  async function handleCreate() {
    try {
      if ((await dataSet.submit()) !== false) {
        dataSet.query();
        return true;
      } else {
        return false;
      }
    } catch (e) {
      return false;
    }
  }

  /**
   * 触发上传按钮
   */
  function triggerFileBtn() {
    const ele = document.getElementById('file');
    ele.click();
  }

  /**
   * 选择文件
   * @param e
   */
  async function selectFile(e) {
    const formdata = new FormData();
    const img = e.target.files[0];
    formdata.append('file', e.target.files[0]);
    try {
      const data = await axios.post(
        `/file/v1/files?bucket_name=devops-service&file_name=${img.name.split('.')[0]}`,
        formdata,
        {
          header: { 'Content-Type': 'multipart/form-data' },
        },
      );
      if (data) {
        record.set('imgUrl', data);
      }
    } catch (err) {
      Choerodon.handleResponseError(e);
    }
  }

  async function handleTestHarbor() {
    try {
      const postData = pick(record.toData(), ['url', 'userName', 'password', 'email', 'project']);
      const res = await AppStore.checkHarbor(projectId, postData);
      if (handlePromptError(res, false)) {
        record.set('harborStatus', 'success');
        return true;
      } else {
        record.set('harborStatus', 'failed');
        return false;
      }
    } catch (e) {
      record.set('harborStatus', 'failed');
      return false;
    }
  }

  async function handleTestChart() {
    try {
      const res = await AppStore.checkChart(projectId, record.get('chartUrl'));
      if (handlePromptError(res, false)) {
        record.set('chartStatus', 'success');
        return true;
      } else {
        record.set('chartStatus', 'failed');
        return false;
      }
    } catch (e) {
      record.set('chartStatus', 'failed');
      return false;
    }
  }

  return (<div className={`${prefixCls}-create-wrap`}>
    <div
      style={{
        backgroundImage: `url(${record.get('imgUrl') || ''})`,
      }}
      className={`${prefixCls}-create-img`}
      onClick={triggerFileBtn}
      role="none"
    >
      <div className="create-img-mask">
        <Icon type="photo_camera" className="create-img-icon" />
      </div>
      <Input
        id="file"
        type="file"
        onChange={selectFile}
        style={{ display: 'none' }}
      />
    </div>
    <div className={`${prefixCls}-create-text`}>
      <FormattedMessage id={`${intlPrefix}.icon`} />
    </div>
    <Form record={record}>
      {!isModify && (
        <Select
          name="type"
          clearButton={false}
        >
          <Option value="normal">
            {formatMessage({ id: `${intlPrefix}.type.normal` })}
          </Option>
          <Option value="test">
            {formatMessage({ id: `${intlPrefix}.type.test` })}
          </Option>
        </Select>
      )}
      {!isModify && <TextField name="code" />}
      <TextField name="name" />
    </Form>
    {isModify && <Settings {...props} handleTestHarbor={handleTestHarbor} handleTestChart={handleTestChart} />}
  </div>);
}));

export default CreateForm;
