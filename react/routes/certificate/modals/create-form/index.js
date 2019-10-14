import React, { useMemo, useState, useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Choerodon } from '@choerodon/boot';
import {
  Form,
  Input,
  Button,
} from 'choerodon-ui';
import debounce from 'lodash/debounce';
import forEach from 'lodash/forEach';
import includes from 'lodash/includes';
import CertConfig from '../../../../components/certConfig';
import Tips from '../../../../components/new-tips';
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
          const res = await store.checkCertName(projectId, encodeURIComponent(value));
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
    let result = true;
    const formData = new FormData();
    validateFieldsAndScroll((err, data) => {
      if (!err) {
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

        if (certId) {
          const { skipCheckProjectPermission, objectVersionNumber, id } = store.getCert || {};
          formData.append('skipCheckProjectPermission', skipCheckProjectPermission);
          formData.append('objectVersionNumber', objectVersionNumber);
          formData.append('id', id);
          formData.append('type', 'update');
        } else {
          formData.append('skipCheckProjectPermission', true);
          formData.append('type', 'create');
        }
      }
      result = !err;
    });
    if (!result) {
      return false;
    }

    try {
      const res = await store.createCert(projectId, formData);
      if (handlePromptError(res, false)) {
        refresh();
      } else {
        return false;
      }
    } catch (error) {
      Choerodon.handleResponseError(error);
      return false;
    }
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
              autoFocus
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
            <div>
              <Tips
                helpText={formatMessage({ id: `${intlPrefix}.add.tips` })}
                title={formatMessage({ id: `${intlPrefix}.add` })}
                showHelp={!uploadMode}
              />
            </div>
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
