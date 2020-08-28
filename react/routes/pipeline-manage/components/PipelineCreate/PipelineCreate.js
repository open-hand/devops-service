import { axios } from '@choerodon/boot';
import React, {
  useEffect, useState, Fragment, useRef,
} from 'react';
import {
  Form, TextField, Select, SelectBox, Modal, Button, DataSet,
} from 'choerodon-ui/pro';
import { message, Icon, Tooltip } from 'choerodon-ui';
import { observer } from 'mobx-react-lite';
import { usePipelineCreateStore } from './stores';
import Tips from '../../../../components/new-tips';
import StageEditBlock from '../PipelineFlow/components/stageEditBlock';
import './pipelineCreate.less';

const { Option } = Select;

const PipelineCreate = observer(() => {
  const {
    PipelineCreateFormDataSet,
    modal,
    editBlockStore,
    createUseStore,
    AppState: {
      currentMenuType: {
        id,
        projectId,
      },
    },
    refreshTree,
    dataSource,
    mainStore,
  } = usePipelineCreateStore();

  const [expandIf, setExpandIf] = useState(false);

  useEffect(() => {
    if (dataSource) {
      const {
        name, appServiceId, image, stageList, versionName,
      } = dataSource;
      PipelineCreateFormDataSet.loadData([{
        name,
        appServiceId,
        image,
        selectImage: '1',
        versionName,
        bbcl: !!versionName,
      }]);
      // editBlockStore.setStepData(stageList, true);
    }
    const init = async () => {
      const res = await createUseStore.axiosGetDefaultImage();
      createUseStore.setDefaultImage(res);
      if (!dataSource) {
        PipelineCreateFormDataSet.current.set('image', res);
      }
    };
    init();
  }, [PipelineCreateFormDataSet, createUseStore, dataSource]);

  const handleCreate = async () => {
    const result = await PipelineCreateFormDataSet.validate();
    if (result) {
      const origin = PipelineCreateFormDataSet.toData()[0];
      const data = {
        ...dataSource,
        ...origin,
        image: origin.selectImage === '1' ? origin.image : null,
        devopsCiStageVOS: editBlockStore.getStepData2.filter((s) => s.type === 'CI'),
        devopsCdStageVOS: editBlockStore.getStepData2.filter((s) => s.type === 'CD'),
      };
      if (!data.bbcl) {
        delete data.versionName;
      }
      if (data.devopsCiStageVOS.some((s) => s.jobList.length === 0)
        || data.devopsCdStageVOS.some((s) => s.jobList.length === 0)
      ) {
        message.error(`流水线中存在空阶段，无法${modal.props.title.includes('创建') ? '创建' : '保存'}`);
        return false;
      }
      if (dataSource) {
        try {
          await axios.put(`/devops/v1/projects/${projectId}/cicd_pipelines/${dataSource.id}`, data);
          editBlockStore.loadData(projectId, dataSource.id);
          refreshTree();
          return true;
        } catch (e) {
          return false;
        }
      }
      try {
        const res = await createUseStore.axiosCreatePipeline(data, id);
        res.id && mainStore.setSelectedMenu({ key: String(res.id) });
        refreshTree();
        return true;
      } catch (e) {
        return false;
      }
    }
    return false;
  };

  const handelCancel = () => {
    refreshTree();
  };

  modal.handleOk(handleCreate);

  modal.handleCancel(handelCancel);

  const handleChangeSelectImage = (data) => {
    if (data === createUseStore.getDefaultImage) {
      PipelineCreateFormDataSet.current.set('selectImage', '0');
    } else {
      PipelineCreateFormDataSet.current.set('selectImage', '1');
    }
  };

  const handleClickMore = async (e) => {
    e.stopPropagation();
    const pageSize = PipelineCreateFormDataSet.current.get('pageSize') + 20;
    const result = await axios.post(`/devops/v1/projects/${projectId}/app_service/page_app_services_without_ci?page=0&size=${pageSize}`);
    if (result.length % 20 === 0) {
      result.push({
        appServiceId: 'more',
        appServiceName: '加载更多',
      });
    }
    PipelineCreateFormDataSet.current.set('pageSize', pageSize);
    PipelineCreateFormDataSet.getField('appServiceId').props.lookup = result;
  };

  const renderer = ({ text }) => {
    const { appServiceName } = createUseStore.getCurrentAppService || {};
    return appServiceName || text;
  };

  const optionRenderer = ({ text }) => (text === '加载更多' ? (
    <a
      role="none"
      style={{ width: '100%', height: '100%', display: 'block' }}
      onClick={handleClickMore}
    >
      {text}
    </a>
  ) : text);

  function getAppServiceData() {
    return createUseStore.getCurrentAppService || {};
  }

  return (
    <div>
      <Form columns={3} dataSet={PipelineCreateFormDataSet}>
        <TextField
          name="name"
          disabled={dataSource}
        />
        {/* 应用服务只能选择目前没有关联流水线的应用服务 */}
        <Select
          disabled={dataSource}
          name="appServiceId"
          searchable
          searchMatcher="appServiceName"
          addonAfter={<Tips helpText="此处仅能看到您有开发权限的启用状态的应用服务，并要求该应用服务必须有master分支，且尚未有关联的流水线" />}
          optionRenderer={optionRenderer}
          renderer={renderer}
        />
        <TextField style={{ display: 'none' }} />
        <div
          role="none"
          className="advanced_text"
          style={{ cursor: 'pointer' }}
          onClick={() => setExpandIf(!expandIf)}
        >
          高级设置
          <Icon style={{ fontSize: 18 }} type={expandIf ? 'expand_less' : 'expand_more'} />
        </div>
        {
          expandIf ? [
            <Select
              combo
              newLine
              colSpan={2}
              name="image"
              onChange={handleChangeSelectImage}
              addonAfter={<Tips helpText="CI流程Runner镜像是该条流水线中所有CI任务默认的执行环境。您可直接使用此处给出的默认Runner镜像，或是输入自定义的CI流程Runner镜像" />}
            >
              <Option
                value={createUseStore.getDefaultImage}
              >
                {createUseStore.getDefaultImage}
              </Option>
            </Select>,
            <div newLine colSpan={2} style={{ position: 'relative', top: '15px' }}>
              <SelectBox
                name="bbcl"
              >
                <Option value={false}>平台默认</Option>
                <Option value>自定义</Option>
              </SelectBox>
              <Tooltip title="是否对harbor域名进行证书校验">
                <Icon
                  type="help"
                  className="c7ncd-select-tips-icon"
                  style={{
                    position: 'absolute',
                    top: '-18px',
                    left: '55px',
                  }}
                />
              </Tooltip>
            </div>,
            PipelineCreateFormDataSet.current.get('bbcl') && (
              <TextField
                newLine
                colSpan={2}
                addonAfter={<Tips helpText="CI流程Runner镜像是该条流水线中所有CI任务默认的执行环境。您可直接使用此处给出的默认Runner镜像，或是输入自定义的CI流程Runner镜像" />}
                name="versionName"
              />
            ),
          ] : ''
        }
      </Form>
      <StageEditBlock
        editBlockStore={editBlockStore}
        edit
        image={PipelineCreateFormDataSet.current.get('image')}
        appServiceId={PipelineCreateFormDataSet.current.get('appServiceId')}
        appServiceCode={
          getAppServiceData()?.appServiceCode || editBlockStore.getMainData?.appServiceCode
        }
        appServiceName={
          getAppServiceData()?.appServiceName || editBlockStore.getMainData?.appServiceName
        }
        appServiceType={getAppServiceData().type || editBlockStore.getMainData?.appServiceType}
        dataSource={dataSource}
      />
      <p className="pipeline_createInfo">
        <Icon style={{ color: 'red', verticalAlign: 'text-bottom' }} type="error" />
        此页面定义了CI阶段或其中的任务后，GitLab仓库中的.gitlab-ci.yml文件也会同步修改。
      </p>
    </div>
  );
});

export default PipelineCreate;
