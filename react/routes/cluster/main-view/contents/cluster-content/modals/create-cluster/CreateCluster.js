import React from 'react';
import { observer } from 'mobx-react-lite';
import { TextField, TextArea, Form, Modal } from 'choerodon-ui/pro';
import { useFormStore } from './stores';
import ActivateCluster from '../activate-cluster';

const modalStyle = {
  width: 380,
};
function CreateClusterForm() {
  const ActivateClusterModalKey = Modal.key();
  const {
    modal,
    formDs,
    mainStore,
    afterOk,
    formatMessage,
    intlPrefix,
    isEdit,
  } = useFormStore();
  const openActivate = (cmd) => {
    Modal.open({
      key: ActivateClusterModalKey,
      title: formatMessage({ id: `${intlPrefix}.activate.header` }),
      children: <ActivateCluster cmd={cmd} intlPrefix={intlPrefix} formatMessage={formatMessage} />,
      drawer: true,
      style: modalStyle,
      okCancel: false,
      okText: formatMessage({ id: 'close' }),
    });
  };
  if (isEdit) {
    formDs.query();
  }

  async function handleSubmit() {
    try {
      if ((await formDs.submit()) !== false) {
        if (!isEdit) {
          const dataObj = JSON.parse(JSON.stringify(mainStore));
          openActivate(dataObj.responseData);
        }
        afterOk();
        return true;
      } else {
        return false;
      }
    } catch (e) {
      return false;
    }
  }

  modal.handleOk(handleSubmit);
  return <Form dataSet={formDs}>
    <TextField name="name" />
    <TextField name="code" disabled={isEdit} />
    <TextArea name="description" resize="vertical" />
  </Form>;
}

export default observer(CreateClusterForm);
