/**
 * @author ale0720@163.com
 * @date 2019-05-30 15:37
 */
import React, { Fragment } from 'react';
import {
  Form,
  Input,
} from 'choerodon-ui';
import { FormattedMessage } from 'react-intl';

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

export function CertTextarea(propsForm, formatMessage, initData = {}) {
  return <Fragment>
    <FormItem
      {...formItemLayout}
      label={<FormattedMessage id="certificate.cert.content" />}
    >
      {propsForm.getFieldDecorator('certValue', {
        rules: [
          {
            required: true,
            message: formatMessage({ id: 'required' }),
          },
        ],
        initialValue: initData.keyValue || '',
      })(
        <TextArea
          autosize={{ minRows: 2 }}
          label={<FormattedMessage id="certificate.cert.content" />}
        />,
      )}
    </FormItem>
    <FormItem
      {...formItemLayout}
      label={<FormattedMessage id="certificate.key.content" />}
    >
      {propsForm.getFieldDecorator('keyValue', {
        rules: [
          {
            required: true,
            message: formatMessage({ id: 'required' }),
          },
        ],
        initialValue: initData.certValue || '',
      })(
        <TextArea
          autosize={{ minRows: 2 }}
          label={<FormattedMessage id="certificate.key.content" />}
        />,
      )}
    </FormItem>
  </Fragment>;
}
