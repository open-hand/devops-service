import React, { useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import { Button } from 'choerodon-ui';
import { Choerodon } from '@choerodon/boot';
import { Modal, Form, TextField } from 'choerodon-ui/pro';
import './index.less';
import { usePipelineStageEditStore } from '../stageEditBlock/stores';
import AddTask from '../../../PipelineCreate/components/AddTask';
import { usePipelineCreateStore } from '../../../PipelineCreate/stores';

const jobTask = {
  build: '构建',
  sonnar: '代码优化',
};

const EditItem = (props) => {
  const {
    taskName,
    index,
    sequence,
    edit,
    jobDetail,
    PipelineCreateFormDataSet,
    AppServiceOptionsDs,
    appServiceId,
  } = props;

  const { type, name } = jobDetail;

  const {
    editBlockStore, stepStore,
  } = usePipelineStageEditStore();

  const {
    editJob,
  } = editBlockStore || stepStore;

  function handleEditOk(data) {
    editJob(sequence, index, data, edit);
  }

  function openEditJobModal() {
    Modal.open({
      key: Modal.key(),
      title: `编辑${taskName}任务`,
      children: <AddTask
        jobDetail={jobDetail}
        appServiceId={!edit && appServiceId}
        handleOk={handleEditOk}
        PipelineCreateFormDataSet={edit && PipelineCreateFormDataSet}
        AppServiceOptionsDs={edit && AppServiceOptionsDs}
      />,
      style: {
        width: '740px',
      },
      drawer: true,
      okText: '添加',
    });
  }

  return (
    <div className="c7n-piplineManage-edit-column-item">
      <div className="c7n-piplineManage-edit-column-item-header">
        【{jobTask[type]}】{name}
      </div>
      <div className="c7n-piplineManage-edit-column-item-btnGroup">
        <Button
          className="c7n-piplineManage-edit-column-item-btnGroup-btn"
          shape="circle"
          size="small"
          icon="mode_edit"
          onClick={openEditJobModal}
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
};

export default observer((props) => {
  const { jobList, sequence, name, columnIndex, edit, appServiceId } = props;

  const {
    addStepDs,
    editBlockStore, stepStore,
  } = usePipelineStageEditStore();

  const {
    addNewStep,
    removeStep,
    eidtStep,
    newJob,
  } = editBlockStore || stepStore;


  let PipelineCreateFormDataSet;
  let AppServiceOptionsDs;
  try {
    PipelineCreateFormDataSet = usePipelineCreateStore().PipelineCreateFormDataSet;
    AppServiceOptionsDs = usePipelineCreateStore().AppServiceOptionsDs;
  } catch (e) {
    window.console.log(e);
  }

  useEffect(() => {
  }, []);

  async function createNewStage() {
    if (addStepDs.current && addStepDs.current.get('step')) {
      addNewStep(columnIndex, addStepDs.current.get('step'), edit);
    } else {
      return false;
    }
    addStepDs.reset();
  }

  async function editStage() {
    if (addStepDs.current && addStepDs.current.get('step')) {
      eidtStep(sequence, addStepDs.current.get('step'), edit);
    } else {
      return false;
    }
    addStepDs.reset();
  }

  const renderStepTasks = () => (
    jobList.length > 0 ? <div className="c7n-piplineManage-edit-column-lists">
      {
        jobList.slice().map((item, index) => <EditItem
          index={index}
          sequence={sequence}
          key={Math.random()}
          edit={edit}
          appServiceId={!edit && appServiceId}
          AppServiceOptionsDs={edit && AppServiceOptionsDs}
          PipelineCreateFormDataSet={edit && PipelineCreateFormDataSet}
          jobDetail={item}
        />)
      }
    </div> : null
  );

  function openAddStageModal(optType) {
    const title = optType === 'create' ? '创建新阶段' : '修改阶段信息';
    if (optType === 'edit') {
      addStepDs.current.set('step', name);
    }
    const optsFun = optType === 'create' ? createNewStage : editStage;
    Modal.open({
      key: Modal.key(),
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
    Modal.open({
      title: `删除${name}阶段`,
      children: '确认删除此阶段吗？',
      key: Modal.key(),
      onOk: () => removeStep(sequence, edit),
    });
  }

  function hanleStepCreateOk(data) {
    newJob(sequence, data, edit);
  }

  function openNewTaskModal() {
    Modal.open({
      key: Modal.key(),
      title: '添加任务',
      children: <AddTask
        PipelineCreateFormDataSet={edit && PipelineCreateFormDataSet}
        AppServiceOptionsDs={edit && AppServiceOptionsDs}
        handleOk={hanleStepCreateOk}
        appServiceId={!edit && appServiceId}
      />,
      style: {
        width: '740px',
      },
      drawer: true,
      okText: '添加',
    });
  }

  return (
    <div className="c7n-piplineManage-edit-column">
      <div className="c7n-piplineManage-edit-column-header">
        <span>{name}</span>
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
          {columnIndex !== 0 && <Button
            funcType="raised"
            shape="circle"
            size="small"
            onClick={deleteStep}
            icon="delete_forever"
            className="c7n-piplineManage-edit-column-header-btnGroup-btn c7n-piplineManage-edit-column-header-btnGroup-btn-delete"
          />}
        </div>
      </div>
      {renderStepTasks()}
      <Button
        funcType="flat"
        icon="add"
        type="primary"
        onClick={openNewTaskModal}
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
