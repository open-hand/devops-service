import React, { useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import { Button } from 'choerodon-ui';
import { Choerodon } from '@choerodon/boot';
import { Modal, Form, TextField } from 'choerodon-ui/pro';
import { usePipelineFlowStore } from '../../stores';
import './index.less';
import { usePipelineStageEditStore } from '../stageEditBlock/stores';

const key1 = Modal.key();

const EditItem = ({ taskName, stepName, id }) => (
  <div className="c7n-piplineManage-edit-column-item">
    <div className="c7n-piplineManage-edit-column-item-header">
      【{stepName}】{taskName}
    </div>
    <div className="c7n-piplineManage-edit-column-item-btnGroup">
      <Button
        className="c7n-piplineManage-edit-column-item-btnGroup-btn"
        shape="circle"
        size="small"
        icon="mode_edit"
      />
      <Button
        className="c7n-piplineManage-edit-column-item-btnGroup-btn"
        shape="circle"
        size="small"
        icon="delete_forever"
      />
    </div>
  </div>
);

export default observer(({ stepTasks, stepName, id, columnIndex }) => {
  const {
    addStepDs,
  } = usePipelineFlowStore();

  const {
    stepStore: {
      addNewStep, removeStep, eidtStep,
    },
  } = usePipelineStageEditStore();

  useEffect(() => {
  }, []);

  async function createNewStage() {
    if (addStepDs.current && addStepDs.current.get('step')) {
      // console.log(addStepDs.current.get('step'));
      addNewStep(columnIndex, addStepDs.current.get('step'));
    } else {
      return false;
    }
    // try {
    //   if (await addStepDs.submit() !== false) {
    //     // refresh();
    //   } else {
    //     return false;
    //   }
    // } catch (e) {
    //   Choerodon.handleResponseError(e);
    //   return false;
    // }
    addStepDs.reset();
  }

  async function editStage() {
    if (addStepDs.current && addStepDs.current.get('step')) {
      eidtStep(id, addStepDs.current.get('step'));
    } else {
      return false;
    }
    addStepDs.reset();
  }

  const renderStepTasks = () => stepTasks && stepTasks.slice().map(item => <EditItem
    key={item.id}
    stepName={stepName}
    {...item}
  />);

  function openAddStageModal(optType) {
    const title = optType === 'create' ? '创建新阶段' : '修改阶段信息';
    if (optType === 'edit') {
      addStepDs.current.set('step', stepName);
    }
    const optsFun = optType === 'create' ? createNewStage : editStage;
    Modal.open({
      key: key1,
      title,
      drawer: true,
      style: {
        width: 380,
      },
      children: (
        <Form dataSet={addStepDs}>
          <TextField name="step" />
        </Form>
      ),
      onOk: optsFun,
      onCancel: () => addStepDs.reset(),
    });
  }

  function deleteStep() {
    removeStep(id);
  }

  return (
    <div className="c7n-piplineManage-edit-column">
      <div className="c7n-piplineManage-edit-column-header">
        <span>{stepName}</span>
        <div
          className="c7n-piplineManage-edit-column-header-btnGroup"
        >
          <Button
            funcType="raised"
            shape="circle"
            size="small"
            icon="mode_edit"
            onClick={openAddStageModal.bind(this, 'edit')}
            className="c7n-piplineManage-edit-column-header-btnGroup-btn"
          />
          <Button
            funcType="raised"
            shape="circle"
            size="small"
            onClick={deleteStep}
            icon="delete_forever"
            className="c7n-piplineManage-edit-column-header-btnGroup-btn"
          />
        </div>
      </div>
      <div className="c7n-piplineManage-edit-column-lists">
        {renderStepTasks()}
      </div>
      <Button
        funcType="flat"
        icon="add"
        type="primary"
        style={{ marginTop: '10px' }}
      >添加任务</Button>
      <Button
        funcType="raised"
        icon="add"
        shape="circle"
        size="small"
        className="c7n-piplineManage-edit-column-addBtn"
        onClick={openAddStageModal.bind(this, 'create')}
      />
    </div>
  );
});
