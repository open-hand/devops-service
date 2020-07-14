import React, { useEffect, Fragment } from 'react';
import { observer } from 'mobx-react-lite';
import { Button } from 'choerodon-ui';
import { Modal, Form, TextField, Select, SelectBox } from 'choerodon-ui/pro';
import { usePipelineStageEditStore } from '../stageEditBlock/stores';
import AddTask from '../../../PipelineCreate/components/AddTask';
import AddCDTask from '../../../PipelineCreate/components/AddCDTask';
import AddStage from './AddStage';
import { usePipelineCreateStore } from '../../../PipelineCreate/stores';
import ViewVariable from '../../../view-variables';
import StageType from '../stage-type';

import './index.less';

const jobTask = {
  build: '构建',
  sonar: '代码检查',
  custom: '自定义',
  chart: '发布Chart',
  cdDeploy: '部署',
  cdHost: '主机部署',
  cdAudit: '人工卡点',
};
const modalStyle = {
  width: 380,
};

const EditItem = (props) => {
  const {
    index,
    sequence,
    edit,
    jobDetail,
    PipelineCreateFormDataSet,
    AppServiceOptionsDs,
    appServiceId,
    appServiceName,
    image,
    openVariableModal,
    stageType,
  } = props;

  const { name, type } = jobDetail;

  const {
    editBlockStore, stepStore,
  } = usePipelineStageEditStore();

  const {
    editJob, removeStepTask,
  } = editBlockStore || stepStore;

  function handleEditOk(data) {
    editJob(sequence, index, data, edit);
  }

  function openEditJobModal() {
    Modal.open({
      key: Modal.key(),
      title: (
        <Fragment>
          <span className="c7n-piplineManage-edit-title-text">{`编辑${name}任务`}</span>
          {
            stageType === 'CI' && (
              <Button
                type="primary"
                icon="find_in_page-o"
                className="c7n-piplineManage-edit-title-btn"
                onClick={openVariableModal}
              >
                查看流水线变量
              </Button>
            )
          }
        </Fragment>
      ),
      children: stageType === 'CI' ? <AddTask
        jobDetail={jobDetail}
        appServiceId={!edit && appServiceName}
        appServiceName={!edit && appServiceName}
        handleOk={handleEditOk}
        PipelineCreateFormDataSet={edit && PipelineCreateFormDataSet}
        AppServiceOptionsDs={edit && AppServiceOptionsDs}
        image={image}
      /> : (<AddCDTask
        jobDetail={jobDetail}
        appServiceId={!edit && appServiceName}
        appServiceName={!edit && appServiceName}
        PipelineCreateFormDataSet={edit && PipelineCreateFormDataSet}
        handleOk={handleEditOk}
      />),
      style: {
        width: '740px',
      },
      drawer: true,
      okText: '添加',
    });
  }

  function openDeleteJobModal() {
    Modal.open({
      key: Modal.key(),
      title: `删除${name}任务`,
      children: '确认删除此任务吗？',
      okText: '确认',
      onOk: () => removeStepTask(sequence, index, edit),
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
          onClick={openDeleteJobModal}
        />
      </div>
    </div>
  );
};

export default observer((props) => {
  const { jobList, sequence, name, columnIndex, edit, appServiceId, appServiceName, image, type, isLast, parallel, isFirst, triggerType: stageTriggerType } = props;
  const {
    addStepDs,
    editBlockStore, stepStore,
  } = usePipelineStageEditStore();

  const {
    addNewStep,
    removeStep,
    eidtStep,
    newJob,
    getStepData,
    getStepData2,
  } = editBlockStore || stepStore;

  const stageLength = edit ? getStepData2.length : getStepData.length;

  let PipelineCreateFormDataSet;
  let AppServiceOptionsDs;
  try {
    PipelineCreateFormDataSet = usePipelineCreateStore().PipelineCreateFormDataSet;
    AppServiceOptionsDs = usePipelineCreateStore().AppServiceOptionsDs;
  } catch (e) {
    window.console.log(e);
  }

  async function createNewStage() {
    const res = await addStepDs.validate();
    if (res) {
      const a = addStepDs.toData()[0];
      addNewStep(columnIndex, addStepDs.toData()[0], edit);
      addStepDs.reset();
      return true;
    }
    return false;
  }

  async function editStage() {
    if (addStepDs.current && addStepDs.current.validate()) {
      eidtStep(sequence, addStepDs.current.get('step'), addStepDs.current.get('type'), addStepDs.current.get('triggerType'), addStepDs.current.get('cdAuditUserIds'), true);
    } else {
      return false;
    }
    addStepDs.reset();
  }

  const renderStepTasks = () => (
    jobList && jobList.length > 0 ? <div className="c7n-piplineManage-edit-column-lists">
      {
        jobList.slice().map((item, index) => <EditItem
          index={index}
          sequence={sequence}
          key={Math.random()}
          edit={edit}
          appServiceId={appServiceId}
          appServiceName={appServiceName}
          AppServiceOptionsDs={edit && AppServiceOptionsDs}
          PipelineCreateFormDataSet={edit && PipelineCreateFormDataSet}
          jobDetail={item}
          image={image}
          openVariableModal={openVariableModal}
          stageType={type || 'CI'}
        />)
      }
    </div> : null
  );
  const openAddStageModal = ({ optType, curType }) => {
    const title = optType === 'create' ? '添加阶段' : '修改阶段信息';
    const okText = optType === 'create' ? '添加' : '修改';
    if (optType === 'edit') {
      const { triggerType, cdAuditUserIds } = editBlockStore.getStepData2.filter(x => x.sequence === sequence)[0];
      addStepDs.current.set('step', name);
      addStepDs.current.set('type', curType || 'CI');
      addStepDs.current.set('triggerType', triggerType);
      addStepDs.current.set('cdAuditUserIds', cdAuditUserIds && [...cdAuditUserIds]);
    }
    const optsFun = optType === 'create' ? createNewStage : editStage;
    Modal.open({
      key: Modal.key(),
      title,
      drawer: true,
      style: {
        width: 380,
      },
      okText,
      children: <AddStage curType={curType} optType={optType} addStepDs={addStepDs} />,
      onOk: optsFun,
      onCancel: () => addStepDs.reset(),
    });
  };

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

  function openVariableModal() {
    Modal.open({
      key: Modal.key(),
      style: modalStyle,
      drawer: true,
      title: '查看变量配置',
      children: <ViewVariable
        appServiceId={appServiceId}
      />,
      okCancel: false,
      okText: '关闭',
    });
  }

  function openNewTaskModal() {
    Modal.open({
      key: Modal.key(),
      title: (
        <Fragment>
          <span className="c7n-piplineManage-edit-title-text">添加任务</span>
          {
            type === 'CI' && (
              <Button
                type="primary"
                icon="find_in_page-o"
                className="c7n-piplineManage-edit-title-btn"
                onClick={openVariableModal}
              >
                查看流水线变量
              </Button>
            )
          }
        </Fragment>
      ),
      children: type === 'CI' ? (
        <AddTask
          PipelineCreateFormDataSet={edit && PipelineCreateFormDataSet}
          AppServiceOptionsDs={edit && AppServiceOptionsDs}
          handleOk={hanleStepCreateOk}
          appServiceId={!edit && appServiceName}
          appServiceName={!edit && appServiceName}
          image={image}
        />
      ) : (<AddCDTask
        appServiceId={!edit && appServiceName}
        appServiceName={!edit && appServiceName}
        PipelineCreateFormDataSet={edit && PipelineCreateFormDataSet}
        handleOk={hanleStepCreateOk}
      />),
      style: {
        width: '740px',
      },
      drawer: true,
      okText: '添加',
    });
  }

  const getType = () => type === 'CI';

  const realType = type?.toUpperCase();

  return (
    <div
      className="c7n-piplineManage-edit-column"
      style={{
        background: getType() ? 'rgba(245, 246, 250, 1)' : 'rgba(245,248,250,1)',
      }}
    >
      <div className="c7n-piplineManage-edit-column-header">
        <span>{name}</span>
        {/* <StageType type={type} parallel={parallel} /> */}
        <span className={`c7n-piplineManage-stage-type c7n-piplineManage-stage-type-${realType}`}>
          {realType}
        </span>
        <div
          className="c7n-piplineManage-edit-column-header-btnGroup"
        >
          <Button
            funcType="raised"
            shape="circle"
            size="small"
            icon="mode_edit"
            onClick={
              () => openAddStageModal({ optType: 'edit', curType: type })
            }
            className="c7n-piplineManage-edit-column-header-btnGroup-btn"
          />
          {stageLength > 1 && <Button
            funcType="raised"
            shape="circle"
            size="small"
            onClick={deleteStep}
            icon="delete_forever"
            className="c7n-piplineManage-edit-column-header-btnGroup-btn c7n-piplineManage-edit-column-header-btnGroup-btn-delete"
          />}
        </div>
      </div>
      <div className="c7n-piplineManage-edit-column-stageType">
        <span>任务列表</span>
        <span
          className={`c7n-piplineManage-stage-type-task c7n-piplineManage-stage-type-task-${parallel ? 'parallel' : 'serial'}`}
        >{parallel ? '任务并行' : '任务串行'}</span>
      </div>
      {renderStepTasks()}
      <Button
        funcType="flat"
        icon="add"
        type="primary"
        onClick={openNewTaskModal}
        style={{ marginTop: '10px' }}
        disabled={PipelineCreateFormDataSet && !PipelineCreateFormDataSet.current.get('appServiceId')}
      >添加任务</Button>
      <Button
        funcType="raised"
        icon="add"
        shape="circle"
        size="small"
        className="c7n-piplineManage-edit-column-addBtn"
        onClick={() => openAddStageModal({ optType: 'create', curType: type })}
      />
      <div
        className={`c7n-piplineManage-edit-column-arrow c7n-piplineManage-edit-column-arrow-${stageTriggerType}`}
        style={{
          display: isFirst ? 'none' : 'block',
        }}
      >
        <span />
      </div>
    </div>
  );
});
