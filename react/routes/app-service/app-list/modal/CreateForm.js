import React, { Fragment, useCallback, useState } from 'react';
import { Form, TextField, Select, Upload, SelectBox, UrlField, Password, EmailField } from 'choerodon-ui/pro';
import { injectIntl, FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import { Icon, Input, Button } from 'choerodon-ui';
import { axios } from '@choerodon/master';

import '../index.less';

const { Option } = Select;

const CreateForm = injectIntl(observer(({ record, AppStore, projectId, intl: { formatMessage }, intlPrefix, prefixCls }) => {
  const isModify = record.status !== 'add';
  const [imgback, setImgback] = useState('');
  const [isExpand, setIsExpand] = useState(false);
  const [harborTest, setHarborTest] = useState('');
  const [helmTest, setHelmTest] = useState('');


  /**
   * 触发上传按钮
   */
  function triggerFileBtn() {
    const ele = document.getElementById('file');
    ele.click();
  }

  /**
   * 处理图片回显
   * @param img
   * @param callback
   */
  function getBase64(img, callback) {
    const reader = new FileReader();
    reader.addEventListener('load', () => callback(reader.result));
    reader.readAsDataURL(img);
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
        getBase64(formdata.get('file'), (imgUrl) => {
          const ele = document.getElementById('img');
          ele.style.backgroundImage = `url(${imgUrl})`;
          setImgback(imgUrl);
        });
      }
    } catch (err) {
      // asfd
    }
  }

  async function handleTestDocker() {
    const postData = {
      url: record.get('dockerAddress'),
      userName: record.get('loginName'),
      password: record.get('password'),
      email: record.get('email'),
      project: record.get('harborProject'),
    };
    try {
      const res = await AppStore.checkHarbor(projectId, postData);
      if (res && res.failed) {
        setHarborTest('failed');
      } else {
        setHarborTest('success');
      }
    } catch (e) {
      setHarborTest('failed');
    }
  }

  async function handleTestHelm() {
    try {
      const res = await AppStore.checkChart(projectId, record.get('helmAddress'));
      if (res && res.failed) {
        setHelmTest('failed');
      } else {
        setHelmTest('success');
      }
    } catch (e) {
      setHelmTest('failed');
    }
  }

  function handleExpand() {
    setIsExpand(pre => !pre);
  }

  return (<div className={`${prefixCls}-create-wrap`}>
    <div
      style={{
        backgroundImage: imgback ? `url(${imgback})` : '',
      }}
      className={`${prefixCls}-create-img`}
      id="img"
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
      <TextField name="code" />
      <TextField name="name" />
    </Form>
    {isModify && <div className="content-settings">
      <div className="content-settings-title">
        <FormattedMessage id={`${intlPrefix}.create.settings`} />
        <Icon
          type={isExpand ? 'expand_less' : 'expand_more'}
          className="content-settings-title-icon"
          onClick={handleExpand}
        />
      </div>
      <div className={!isExpand ? 'content-settings-detail' : ''}>
        <div className="content-settings-tips">
          <Icon type="info" className="content-settings-tips-icon" />
          <FormattedMessage id={`${intlPrefix}.create.settings.tips`} />
        </div>
        <Form record={record}>
          <SelectBox name="dockerType">
            <Option value="default">
              {formatMessage({ id: `${intlPrefix}.docker.default` })}
            </Option>
            <Option value="custom">
              {formatMessage({ id: `${intlPrefix}.docker.custom` })}
            </Option>
          </SelectBox>
        </Form>
        {record.get('dockerType') === 'custom' && (<Fragment>
          <Form record={record}>
            <UrlField name="dockerAddress" />
            <TextField name="loginName" />
            <Password name="password" />
            <EmailField name="email" />
            <TextField name="harborProject" />
          </Form>
          <div>
            <Button onClick={handleTestDocker}>
              <FormattedMessage id={`${intlPrefix}.test`} />
            </Button>
            {harborTest && <FormattedMessage id={harborTest} />}
          </div>
        </Fragment>)}
        <Form record={record}>
          <SelectBox name="helmType">
            <Option value="default">
              {formatMessage({ id: `${intlPrefix}.helm.default` })}
            </Option>
            <Option value="custom">
              {formatMessage({ id: `${intlPrefix}.helm.custom` })}
            </Option>
          </SelectBox>
        </Form>
        {record.get('helmType') === 'custom' && (<Fragment>
          <Form record={record}>
            <UrlField name="helmAddress" />
          </Form>
          <div>
            <Button onClick={handleTestHelm}>
              <FormattedMessage id={`${intlPrefix}.test`} />
            </Button>
            {helmTest && <FormattedMessage id={helmTest} />}
          </div>
        </Fragment>)}
      </div>
    </div>}
  </div>);
}));

export default CreateForm;
