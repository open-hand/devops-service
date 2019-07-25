import React, { Component, Fragment } from 'react/index';
import { observer, inject } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { Modal, Select, Icon, Button } from 'choerodon-ui';
import { Content } from '@choerodon/boot';
import { injectIntl, FormattedMessage } from 'react-intl';
import _ from 'lodash';
import YamlEditor from '../../components/yamlEditor';
import InterceptMask from '../../components/interceptMask/InterceptMask';
import LoadingBar from '../../components/loadingBar';
import { handlePromptError } from '../../utils';

import './instances-home/index.scss';
import '../main.scss';

const { Sidebar } = Modal;
const Option = Select.Option;

@withRouter
@injectIntl
@inject('AppState')
@observer
export default class UpgradeIst extends Component {
  constructor(props) {
    super(props);
    this.state = {
      versionId: undefined,
      values: null,
      loading: false,
      submitting: false,
      hasEditorError: false,
      idArr: {},
      versionLoading: false,
      versionOptions: [],
      versions: [],
      versionPageNum: 2,
    };
  }

  componentDidMount = async () => {
    const {
      idArr,
    } = this.props;
    this.handleLoadVersion(idArr.appId, '', '', true);
  };

  /**
   * 加载版本
   * @param appId
   * @param param 搜索内容
   * @param init 版本id 需要在列表中的版本
   * @param shouldLoadValue 是否需要加载value
   */
  handleLoadVersion = async (appId, param = '', init, shouldLoadValue = false) => {
    const {
      AppState: {
        currentMenuType: {
          id: projectId,
        },
      },
      store,
      idArr,
    } = this.props;

    this.setState({ versionLoading: true });

    const initId = init || '';
    const data = await store.loadUpVersion({ projectId, appId, page: 1, param, initId })
      .catch(() => {
        this.setState({ versionLoading: false });
      });

    if (handlePromptError(data)) {
      const { hasNextPage, list } = data;
      const versionOptions = renderVersionOptions(list);

      if (hasNextPage) {
        // 在选项最后置入一个加载更多按钮
        const loadMoreBtn = renderLoadMoreBtn(this.handleLoadMoreVersion);
        versionOptions.push(loadMoreBtn);
      }

      this.setState({
        versionOptions,
        versions: list,
        versionLoading: false,
      });

      if (shouldLoadValue) {
        const newIdArr = {
          ...idArr,
          appVersionId: list[0].id,
        };
        this.setState({ idArr: newIdArr });
        this.handleVersionChange(list[0].id);
      }
    }
  };

  /**
   * 搜索版本
   */
  handleVersionSearch = _.debounce((value) => {
    const {
      idArr,
    } = this.state;
    this.setState({ versionSearchParam: value, versionPageNum: 2 });
    this.handleLoadVersion(idArr.appId, value, '');
  }, 500);

  /**
   * 点击加载更多
   * @param e
   */
  handleLoadMoreVersion = async (e) => {
    e.stopPropagation();

    const {
      AppState: {
        currentMenuType: {
          projectId,
        },
      },
      store,
    } = this.props;

    const {
      versionId,
      versionOptions,
      versions,
      idArr: {
        appId,
      },
      versionPageNum,
      versionSearchParam,
    } = this.state;

    this.setState({ versionLoading: true });

    const data = await store.loadUpVersion({
      projectId,
      appId,
      page: versionPageNum,
      param: versionSearchParam,
    })
      .catch(() => {
        this.setState({ versionLoading: false });
      });

    if (handlePromptError(data)) {
      const { hasNextPage, list } = data;

      const moreVersion = _.filter(list, ({ id }) => id !== versionId);
      const options = renderVersionOptions(moreVersion);

      /**
       * 触发此事件说明初次渲染的选项versionOptions中的最后一个肯定是 “展开更多” 按钮
       * 可以先将该按钮使用 initial 方法从原来去除
       * 如果当前页页码仍然小于总页数，再讲该按钮放回到新的选项的最后
       */
      const newVersionOpt = _.concat(
        _.initial(versionOptions),
        options,
        hasNextPage ? _.last(versionOptions) : [],
      );
      const newVersions = _.concat(versions, moreVersion);

      this.setState({
        versionOptions: newVersionOpt,
        versions: newVersions,
        versionLoading: false,
        versionPageNum: versionPageNum + 1,
      });
    }
  };

  handleNextStepEnable = flag => this.setState({ hasEditorError: flag });

  handleChangeValue = values => this.setState({ values });

  onClose = () => this.props.onClose(false);

  /**
   * 修改配置升级实例
   */
  handleOk = async () => {
    const {
      store,
      appInstanceId,
      onClose,
      AppState: {
        currentMenuType: { projectId },
      },
    } = this.props;

    const { values, versionId, idArr, versions } = this.state;
    const verId = versionId || versions[0].id;
    const { id, yaml } = store.getValue || {};

    const data = {
      ...idArr,
      values: values || yaml || '',
      valueId: id,
      appInstanceId,
      appVersionId: verId,
      type: 'update',
    };

    this.setState({ submitting: true });
    const res = await store.reDeploy(projectId, data)
      .catch((e) => {
        this.setState({ submitting: false });
        onClose(true);
        Choerodon.handleResponseError(e);
      });

    if (res && res.failed) {
      Choerodon.prompt(res.message);
    }

    this.setState({ submitting: false });
    onClose(true);
  };

  /**
   * 切换实例版本，加载该版本下的配置内容
   * @param id
   */
  handleVersionChange = (id) => {
    const {
      store,
      appInstanceId,
      AppState: {
        currentMenuType: { projectId },
      },
    } = this.props;
    this.setState({ versionId: id, values: null, loading: true });
    store.setValue(null);
    store.loadValue(projectId, appInstanceId, id).then(() => {
      this.setState({ loading: false });
    });
  };

  render() {
    const {
      intl: { formatMessage },
      store: {
        getValue,
      },
      name,
      visible,
    } = this.props;
    const {
      values,
      submitting,
      loading,
      versionId,
      hasEditorError,
      versionOptions,
      versions,
      versionLoading,
    } = this.state;

    const initValue = getValue ? getValue.yaml : '';

    return (
      <Sidebar
        title={formatMessage({ id: 'ist.upgrade' })}
        visible={visible}
        confirmLoading={submitting}
        footer={
          [<Button
            key="submit"
            type="primary"
            funcType="raised"
            onClick={this.handleOk}
            loading={submitting}
            disabled={hasEditorError}
          >
            {formatMessage({ id: 'ist.upgrade' })}
          </Button>,
            <Button
              funcType="raised"
              key="back"
              onClick={this.onClose}
              disabled={submitting}
            >
              {formatMessage({ id: 'cancel' })}
            </Button>]
        }
      >
        <Content
          code="ist.upgrade"
          values={{ name }}
          className="sidebar-content"
        >
          <Select
            filter
            className="c7n-app-select_512"
            label={formatMessage({ id: 'deploy.step.one.version.title' })}
            notFoundContent={formatMessage({ id: 'ist.noUpVer' })}
            loading={versionLoading}
            filterOption={false}
            onSearch={this.handleVersionSearch}
            onChange={this.handleVersionChange}
            value={versionId || (versions.length ? versions[0].id : undefined)}
          >
            {versionOptions}
          </Select>

          {versions.length === 0 ? (
            <div>
              <Icon type="error" className="c7n-noVer-waring" />
              {formatMessage({ id: 'ist.noUpVer' })}
            </div>
          ) : null}

          <div className="c7n-config-section">
            {getValue ? <YamlEditor
              readOnly={false}
              value={values || initValue}
              originValue={initValue}
              handleEnableNext={this.handleNextStepEnable}
              onValueChange={this.handleChangeValue}
            /> : null}
          </div>
          <LoadingBar display={loading} />
        </Content>

        <InterceptMask visible={submitting} />
      </Sidebar>
    );
  }
}

/**
 * 生成版本选项
 * @param versions
 */
function renderVersionOptions(versions) {
  return _.map(versions, ({ id, version }) => <Option key={id} value={id}>{version}</Option>);
}

function renderLoadMoreBtn(handler) {
  return <Option
    disabled
    className="c7ncd-more-btn-wrap"
    key="btn_load_more"
  >
    <Button
      type="default"
      className="c7ncd-more-btn"
      onClick={handler}
    >
      <FormattedMessage id="loadMore" />
    </Button>
  </Option>;
}
