/**
 * @author ale0720@163.com
 * @date 2019-05-30 15:37
 */
import React, { Component, Fragment } from 'react';
import {
  Form,
  Upload,
  Icon,
} from 'choerodon-ui';
import { injectIntl, FormattedMessage } from 'react-intl';
import _ from 'lodash';
import classnames from 'classnames';

import './CertUploader.scss';

const { Item: FormItem } = Form;
const formItemLayout = {
  labelCol: {
    xs: { span: 24 },
    sm: { span: 100 },
  },
  wrapperCol: {
    xs: { span: 24 },
    sm: { span: 26 },
  },
};

@Form.create({})
@injectIntl
export default class CertUploader extends Component {
  state = {
    keyDisabled: false,
    crtDisabled: false,
  };

  beforeUploadCertFile = file => {
    this.beforeUploadFile(file, 'crt');
  };

  beforeUploadKeyFile = file => {
    this.beforeUploadFile(file, 'key');
  };

  /**
   * 始终返回false，阻止自动上传
   * @param name
   * @param type
   * @returns {boolean}
   */
  beforeUploadFile = ({ name }, type) => {
    const { intl: { formatMessage } } = this.props;

    const isTypeRight = name.endsWith(type);

    const msg = isTypeRight
      ? `${name} ${formatMessage({ id: 'file.uploaded.success' })}`
      : formatMessage({ id: 'file.type.error' });

    Choerodon.prompt(msg);

    return false;
  };

  handleUploadCertFile = e => this.handleUpload(e, 'crt');

  handleUploadKeyFile = e => this.handleUpload(e, 'key');

  /**
   * 表单中Upload的onChange
   * 响应 上传、删除
   * @param e
   * @param type
   * @returns {*}
   */
  handleUpload = (e, type) => {

    if (_.isArray(e)) return e;

    const fileType = `${type}Disabled`;

    this.setState({ [fileType]: true });

    const { file, fileList } = e;
    const keyFileList = [];

    if (fileList.length) {

      const isType = file.name.endsWith(type);

      if (!isType) {
        this.setState({ [fileType]: false });
      } else {
        keyFileList.push(file);
        this.setState({ [fileType]: true });
      }
    } else {
      // 移除
      this.setState({ [fileType]: false });
    }

    return keyFileList;
  };

  render() {
    const {
      form: { getFieldDecorator },
      intl: { formatMessage },
    } = this.props;

    const {
      keyDisabled,
      crtDisabled,
    } = this.state;

    const uploadProps = {
      listType: 'text',
      action: '//jsonplaceholder.typicode.com/posts/',
      multiple: false,
    };

    const crtUploadClass = classnames({
      'c7ncd-upload-select': !crtDisabled,
      'c7ncd-upload-disabled': crtDisabled,
    });

    const keyUploadClass = classnames({
      'c7ncd-upload-select': !keyDisabled,
      'c7ncd-upload-disabled': keyDisabled,
    });

    const uploadBtn = <Fragment>
      <Icon
        className="c7ncd-cert-upload-icon"
        type="add"
      />
      <div className="c7n-upload-text">Upload</div>
    </Fragment>;

    return <div className="c7ncd-cert-upload">
      <div className="c7ncd-cert-upload-item">
        <h4><FormattedMessage id="ctf.certFile" /></h4>
        <FormItem{...formItemLayout}>
          {getFieldDecorator('cert', {
            // valuePropName: 'fileList',
            getValueFromEvent: this.handleUploadCertFile,
            rules: [
              {
                required: true,
                message: formatMessage({
                  id: 'ctf.cert.required',
                }),
              },
            ],
          })(
            <Upload
              {...uploadProps}
              disabled={crtDisabled}
              beforeUpload={this.beforeUploadCertFile}
            >
              <div className={crtUploadClass}>
                {uploadBtn}
              </div>
            </Upload>,
          )}
        </FormItem>
      </div>
      <div className="c7ncd-cert-upload-item">
        <h4><FormattedMessage id="ctf.keyFile" /></h4>
        <FormItem{...formItemLayout}>
          {getFieldDecorator('key', {
            // valuePropName: 'fileList',
            getValueFromEvent: this.handleUploadKeyFile,
            rules: [
              {
                required: true,
                message: formatMessage({
                  id: 'ctf.key.required',
                }),
              },
            ],
          })(
            <Upload
              {...uploadProps}
              disabled={keyDisabled}
              beforeUpload={this.beforeUploadKeyFile}
            >
              <div className={keyUploadClass}>
                {uploadBtn}
              </div>
            </Upload>,
          )}
        </FormItem>
      </div>
    </div>;
  };
}
