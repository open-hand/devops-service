/**
 * @author ale0720@163.com
 * @date 2019-05-31 10:52
 */
/**
 * @author ale0720@163.com
 * @date 2019-05-30 15:37
 */
import React, { Fragment } from 'react';
import {
  Form,
  Input,
} from 'choerodon-ui';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Consumer } from '../certContext';

const { TextArea } = Input;
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

function CertTextarea(props) {
  const {
    intl: { formatMessage },
  } = props;

  /**
   * form 通过 Context API 传递
   * 为了将表单项注册在同一个 form 中
   * @param form
   * @returns {*}
   */
  const contents = form => (<Fragment>
    <FormItem
      className="c7n-select_480"
      {...formItemLayout}
      label={<FormattedMessage id="certificate.cert.content" />}
    >
      {form.getFieldDecorator('certValue', {
        rules: [
          {
            required: true,
            message: formatMessage({ id: 'required' }),
          },
        ],
      })(
        <TextArea
          autosize={{ minRows: 2 }}
          label={<FormattedMessage id="certificate.cert.content" />}
        />,
      )}
    </FormItem>
    <FormItem
      className="c7n-select_480"
      {...formItemLayout}
      label={<FormattedMessage id="certificate.key.content" />}
    >
      {form.getFieldDecorator('keyValue', {
        rules: [
          {
            required: true,
            message: formatMessage({ id: 'required' }),
          },
        ],
      })(
        <TextArea
          autosize={{ minRows: 2 }}
          label={<FormattedMessage id="certificate.key.content" />}
        />,
      )}
    </FormItem>
  </Fragment>);

  return <Consumer>
    {contents}
  </Consumer>;
}

export default injectIntl(CertTextarea);
