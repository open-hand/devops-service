/**
 * @author ale0720@163.com
 * @date 2019-05-30 15:37
 */
import React, { PureComponent, Fragment } from 'react';
import {
  Form,
  Upload,
  Icon,
} from 'choerodon-ui';
import { injectIntl, FormattedMessage } from 'react-intl';
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

const uploadProps = {
  listType: 'text',
  // TODO: 安全起见，该处应该替换为系统内地址，而不是使用公共地址
  action: '//jsonplaceholder.typicode.com/posts/',
  multiple: false,
};

@injectIntl
export default class CertUploader extends PureComponent {
  state = {
    keyDisabled: false,
    crtDisabled: false,
  };

  /**
   * 始终返回false，阻止自动上传
   * @param file
   * @param type
   * @returns {boolean}
   */
  beforeUpload = (file, type) => {
    this.setState({ [`${type}Disabled`]: true });

    // beforeUpload 需要返回一个bool值
    return false;
  };

  checkKeyFile = (...arg) => {
    this.checkFile('key', ...arg);
  };

  checkCrtFile = (...arg) => {
    this.checkFile('crt', ...arg);
  };

  checkFile = (type, rule, value, callback) => {
    const { intl: { formatMessage } } = this.props;

    if (!value) {
      callback(formatMessage({ id: `ctf.${type}.required` }));
    } else {
      const { file: { name }, fileList } = value;
      if (!fileList.length) {
        callback(formatMessage({ id: `ctf.${type}.required` }));
      } else if(fileList.length > 1) {
        callback(formatMessage({ id: 'file.type.multiple' }));
      } else if (!name.endsWith(`.${type}`)) {
        callback(formatMessage({ id: 'file.type.error' }));
      } else {
        callback();
      }
    }
  };

  removeCert = () => {
    this.setState({ crtDisabled: false });
  };

  removeKey = () => {
    this.setState({ keyDisabled: false });
  };

  render() {
    const {
      propsForm: {
        getFieldDecorator,
      },
    } = this.props;
    const {
      keyDisabled,
      crtDisabled,
    } = this.state;

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
            rules: [{
              validator: this.checkCrtFile,
            }],
          })(
            <Upload
              {...uploadProps}
              disabled={crtDisabled}
              onChange={this.fileChange}
              beforeUpload={file => this.beforeUpload(file, 'crt')}
              onRemove={this.removeCert}
              accept=".crt"
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
            rules: [{
              validator: this.checkKeyFile,
            }],
          })(
            <Upload
              {...uploadProps}
              disabled={keyDisabled}
              beforeUpload={file => this.beforeUpload(file, 'key')}
              onRemove={this.removeKey}
              accept=".key"
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
