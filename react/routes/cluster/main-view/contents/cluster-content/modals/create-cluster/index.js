import React, { Fragment, useState, useMemo } from 'react';
import { observer } from 'mobx-react-lite';
import { Input, Form } from 'choerodon-ui';
import { Modal } from 'choerodon-ui/pro';
import { Choerodon } from '@choerodon/boot';
import _ from 'lodash';
import { handlePromptError } from '../../../../../../../utils';
import ActivateCluster from '../activate-cluster';

import './index.less';

const { TextArea } = Input;
const FormItem = Form.Item;
const formDataModal = {
  name: '',
  code: '',
  description: '',
};

const ActivateClusterModalKey = Modal.key();

const CreateCluster = observer((props) => {
  const {
    isEdit,
    afterOk,
    mainStore,
    projectId,
    formatMessage,
    intlPrefix,
    modal,
    record,
    form,
  } = props;

  const { getFieldDecorator, validateFields } = form;

  const [formData, setFormData] = useState(record ? toData(record) : formDataModal);

  modal.handleOk(() => {
    validateFields(async (err, values) => {
      if (!err) {
        if (!isEdit) {
          const res = await mainStore.createCluster({ projectId, ...formData });
          if (handlePromptError(res, true)) {
            openActivate(res);
            afterOk();
            modal.close();
          }
        } else {
          // 更新集群
          const data = {
            projectId,
            ...formData,
            clusterId: formData.id,
          };
          try {
            const res = await mainStore.updateCluster(data);
            if (handlePromptError(res, false)) {
              afterOk();
              modal.close();
            }
          } catch (e) {
            Choerodon.handleResponseError(e);
          }
        }
      }
    });
    return false;
  });


  const handleNameChange = ({ target: { value } }) => {
    handleChange({ name: value });
  };
  const handleCodeChange = ({ target: { value } }) => {
    handleChange({ code: value });
  };
  const handleDescriptionChange = ({ target: { value } }) => {
    handleChange({ description: value });
  };
  const handleChange = (newState) => {
    setFormData((oldState) => ({
      ...oldState,
      ...newState,
    }));
  };

  const openActivate = (cmd) => {
    Modal.open({
      key: ActivateClusterModalKey,
      title: formatMessage({ id: `${intlPrefix}.activate.header` }),
      children: <ActivateCluster cmd={cmd} intlPrefix={intlPrefix} formatMessage={formatMessage} />,
      drawer: true,
      style: {
        width: 380,
      },
      okCancel: false,
      okText: formatMessage({ id: 'close' }),
    });
  };

  const checkName = useMemo(() => _.debounce((rule, value, callback) => {
    if (formData && value === formData.name) {
      callback();
    } else if (value) {
      mainStore.checkClusterName({
        projectId,
        clusterName: window.encodeURIComponent(value),
      })
        .then((res) => {
          if (res && res.failed) {
            callback(`名称${formatMessage({ id: `${intlPrefix}.check.exist` })}`);
          } else {
            callback();
          }
        })
        .catch((e) => {
          callback(`${formatMessage({ id: `${intlPrefix}.check.error` })}`);
        });
    } else {
      callback();
    }
  }, 1000),
  []);
  const checkCode = useMemo(() => _.debounce((rule, value, callback) => {
    const pa = /^[a-z]([-a-z0-9]*[a-z0-9])?$/;
    if (formData && value === formData.code) {
      callback();
    } else if (value && pa.test(value)) {
      mainStore.checkClusterCode({
        projectId,
        clusterCode: value,
      })
        .then((res) => {
          if (res && res.failed) {
            callback(`编码${formatMessage({ id: `${intlPrefix}.check.exist` })}`);
          } else {
            callback();
          }
        })
        .catch((e) => {
          callback(`${formatMessage({ id: `${intlPrefix}.check.error` })}`);
        });
    } else if (value && !pa.test(value)) {
      callback(`编码${formatMessage({ id: `${intlPrefix}.check.failed` })}`);
    } else {
      callback();
    }
  }, 1000),
  []);

  return (
    <Fragment>
      <Form className="c7ncd-cluster-create-form">
        <FormItem>
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
            initialValue: isEdit ? formData.name : '',
          })(
            <Input
              maxLength={30}
              label={formatMessage({ id: `${intlPrefix}.name` })}
              onChange={handleNameChange}
              autoFocus
            />,
          )} </FormItem>
        <FormItem>
          {getFieldDecorator('code', {
            rules: [
              {
                required: true,
                message: formatMessage({ id: 'required' }),
              },
              {
                validator: checkCode,
              },
            ],
            initialValue: isEdit ? formData.code : '',
          })(
            <Input
              disabled={isEdit}
              maxLength={30}
              label={formatMessage({ id: `${intlPrefix}.code` })}
              onChange={handleCodeChange}
            />,
          )} </FormItem>
        <FormItem>
          {getFieldDecorator('description', {
            initialValue: isEdit ? formData.description : '',
          })(
            <TextArea
              maxLength={200}
              label={formatMessage({ id: `${intlPrefix}.dec` })}
              onChange={handleDescriptionChange}
            />,
          )}
        </FormItem>
      </Form>
    </Fragment>);
});

function toData(obj) {
  return obj.toData ? obj.toData() : obj;
}

export default Form.create({})(CreateCluster);
