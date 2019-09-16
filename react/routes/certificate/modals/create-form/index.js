import React, { useMemo, useState, useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import { injectIntl, FormattedMessage } from 'react-intl';
import {
  Form,
  Input,
  Button,
} from 'choerodon-ui';
import debounce from 'lodash/debounce';
import forEach from 'lodash/forEach';
import includes from 'lodash/includes';
import CertConfig from '../../../../components/certConfig';
import Tips from '../../../../components/Tips/Tips';
import { handlePromptError } from '../../../../utils';

import './index.less';

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

const CreateForm = ({ certId, intl: { formatMessage }, form, store, projectId, modal, refresh, intlPrefix, prefixCls }) => {
  const [uploadMode, setUploadMode] = useState(false);
  const { getFieldDecorator, validateFieldsAndScroll } = form;

  const checkName = useMemo(() => (
    debounce(async (rule, value, callback) => {
      const { name } = store.getCert || {};
      if (value && value !== name) {
        try {
          const res = await store.checkCertName(projectId, value);
          if (res && res.failed) {
            callback(formatMessage({ id: 'checkNameExist' }));
          } else {
            callback();
          }
        } catch (e) {
          callback(formatMessage({ id: 'checkNameFailed' }));
          Choerodon.handleResponseError(e);
        }
      } else {
        callback();
      }
    }, 1000)
  ), []);

  useEffect(() => {
    certId && store.loadCertById(projectId, certId);
  }, []);


  /**
   * 域名格式检查
   * @param rule
   * @param value
   * @param callback
   */
  function checkDomain(rule, value, callback) {
    const p = /^([a-z0-9]([-a-z0-9]*[a-z0-9])?(\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)+)$/;
    if (p.test(value)) {
      callback();
    } else {
      callback(formatMessage({ id: `${intlPrefix}.domain.failed` }));
    }
  }

  modal.handleOk(async () => {
    validateFieldsAndScroll(async (err, data) => {
      if (!err) {
        const formData = new FormData();
        const excludeProps = ['domainArr', 'cert', 'key'];

        if (uploadMode) {
          const { key, cert } = data;

          formData.append('key', key.file);
          formData.append('cert', cert.file);
        }

        forEach(data, (value, k) => {
          if (!includes(excludeProps, k)) {
            formData.append(k, value);
          }
        });

        let method;
        if (certId) {
          const { skipCheckProjectPermission, objectVersionNumber, id } = store.getCert || {};
          formData.append('skipCheckProjectPermission', skipCheckProjectPermission);
          formData.append('objectVersionNumber', objectVersionNumber);
          formData.append('id', id);
          method = store.updateCert;
        } else {
          formData.append('skipCheckProjectPermission', true);
          method = store.createCert;
        }

        try {
          const result = await method(projectId, formData);
          if (handlePromptError(result, false)) {
            refresh();
            modal.close();
          }
        } catch (error) {
          Choerodon.handleResponseError(error);
        }
      }
    });
    return false;
  });

  function changeUploadMode() {
    setUploadMode((pre) => !pre);
  }

  return (
    <div className={`${prefixCls}-create-wrap`}>
      <Form layout="vertical">
        <FormItem {...formItemLayout}>
          {getFieldDecorator('name', {
            rules: [
              {
                required: true,
                message: formatMessage({ id: 'required' }),
              },
              {
                validator: checkName,
              },
            ],
            initialValue: store.getCert ? store.getCert.name : '',
          })(
            <Input
              maxLength={40}
              type="text"
              label={<FormattedMessage id={`${intlPrefix}.name`} />}
            />,
          )}
        </FormItem>
        <div className={`${prefixCls}-create-wrap-section`}>
          <div className={`${prefixCls}-create-wrap-section-title`}>
            <FormattedMessage id={`${intlPrefix}.upload`} />
          </div>
          <FormItem
            {...formItemLayout}
          >
            {getFieldDecorator('domain', {
              rules: [
                {
                  required: true,
                  message: formatMessage({ id: 'required' }),
                },
                {
                  validator: checkDomain,
                },
              ],
              initialValue: store.getCert ? store.getCert.domain : '',
            })(
              <Input
                type="text"
                maxLength={50}
                label={<FormattedMessage id={`${intlPrefix}.domain`} />}
              />,
            )}
          </FormItem>
          <div className={`${prefixCls}-create-wrap-add-title`}>
            <Tips
              type="title"
              data={`${intlPrefix}.add`}
              help={!uploadMode}
            />
            <Button
              type="primary"
              funcType="flat"
              onClick={changeUploadMode}
            >
              <FormattedMessage id={`${intlPrefix}.upload.mode`} />
            </Button>
          </div>
          {CertConfig(uploadMode, form, formatMessage, store.getCert || {})}
        </div>
      </Form>
    </div>
  );
};

export default Form.create()(injectIntl(observer(CreateForm)));
