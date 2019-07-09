/* eslint-disable no-underscore-dangle, react/no-unused-state */
/**
 * @author ale0720@163.com
 * @date 2019-06-06 14:09
 */
import React, { Component, Fragment } from 'react';
import { Permission } from '@choerodon/boot';
import { observer, inject } from 'mobx-react';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Select, Button, Icon } from 'choerodon-ui';
import _ from 'lodash';
import classnames from 'classnames';
import ButtonGroup from '../components/buttonGroup';
import AppList from '../appList';
import AppName from '../../../../components/appName';
import { handleCheckerProptError } from '../../../../utils';

import './AppWithVersions.scss';

const INIT_STATE = {
  app: null,
  appId: undefined,
  versions: [],
  versionId: undefined,
  versionOptions: [],
  versionPageNum: 2,
  versionSearchParam: '',
  versionLoading: false,
  displayAppList: false,
  isLocalProject: true,
};

const { Option } = Select;

@injectIntl
@inject('AppState')
@observer
export default class AppWithVersions extends Component {
  state = {
    versions: [],
    versionOptions: [],
    versionPageNum: 2,
    versionSearchParam: '',
    versionLoading: false,
    displayAppList: false,
    isLocalProject: true,
  };

  componentDidMount() {
    const {
      store: {
        getSelectedApp,
        getSelectedVersion,
      },
    } = this.props;

    let appId;
    let isLocalProject = true;

    const versionId = (getSelectedVersion || {}).id;

    if (getSelectedApp) {
      // NOTE: 项目下的应用 publishLevel 一定为 null
      isLocalProject = !getSelectedApp.publishLevel;
      appId = getSelectedApp.publishLevel ? getSelectedApp.appId : getSelectedApp.id;
      this.handleLoadVersion(appId, isLocalProject, '', versionId);
    }

    this.setState({
      appId,
      isLocalProject,
      app: getSelectedApp,
      versionDto: getSelectedVersion,
      versionId,
    });
  }

  /**
   * 选择应用的侧边栏
   */
  openAppList = () => {
    this.setState({ displayAppList: true });
  };

  closeAppList = () => {
    this.setState({ displayAppList: false });
  };

  /**
   * 确认选择APP
   * @param app 选择的数据
   * @param isLocalProject 标明是项目应用还是应用市场应用
   */
  handleSelectApp = (app, isLocalProject) => {
    const { store } = this.props;
    if (app) {
      const appId = isLocalProject ? app.id : app.appId;
      const initState = _.cloneDeep(INIT_STATE);

      this.setState({
        ...initState,
        app,
        appId,
        isLocalProject,
      });

      this.handleLoadVersion(appId, !isLocalProject);
      store.initAllData();
    } else {
      this.setState({ displayAppList: false });
    }
  };

  /**
   * 搜索版本
   */
  handleVersionSearch = _.debounce((value) => {
    const { appId, isLocalProject } = this.state;

    if (appId) {
      const isPublic = !isLocalProject;
      this.setState({ versionSearchParam: value, versionPageNum: 2 });
      this.handleLoadVersion(appId, isPublic, value, '');
    }
  }, 500);

  /**
   * 加载版本
   * @param appId
   * @param isPublic
   * @param param
   * @param init
   */
  handleLoadVersion = async (appId, isPublic, param = '', init) => {
    const {
      AppState: {
        currentMenuType: {
          id: projectId,
        },
      },
      store,
    } = this.props;

    this.setState({ versionLoading: true });

    const initId = init || '';
    const data = await store.loadVersions({ projectId, appId, isPublic, page: 1, param, initId })
      .catch(() => {
        this.setState({ versionLoading: false });
      });

    if (handleCheckerProptError(data)) {
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
    }
  };

  /**
   * 选择版本
   * @param value
   */
  handleSelectVersion = (value) => {
    const { versions } = this.state;
    const versionDto = _.find(versions, ['id', value]);

    this.setState({
      versionId: value,
      versionDto,
      value: null,
      markers: [],
      isChangedYaml: false,
    });
  };

  /**
   * 点击加载更多
   * @param e
   */
  handleLoadMoreVersion = async (e) => {
    e.stopPropagation();

    const {
      AppState: {
        currentMenuType: {
          id: projectId,
        },
      },
      store,
    } = this.props;

    const {
      versionId,
      versionOptions,
      versions,
      appId,
      isLocalProject,
      versionPageNum,
      versionSearchParam,
    } = this.state;

    this.setState({ versionLoading: true });

    const data = await store.loadVersions({
      projectId,
      appId,
      isPublish: !isLocalProject,
      page: versionPageNum,
      param: versionSearchParam,
    })
      .catch(() => {
        this.setState({ versionLoading: false });
      });

    if (handleCheckerProptError(data)) {
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

  handleNext = () => {
    const { onChange, store } = this.props;
    const { app, versionDto } = this.state;

    const _app = app;
    if (!_app.publishLevel) {
      _app.appId = _app.id;
    }

    store.setSelectedApp(_app);
    store.setSelectedVersion(versionDto);
    onChange(1);
  };

  handleCancel = () => {
    const { onCancel } = this.props;
    this.initState();
    onCancel();
  };

  initState = () => {
    const state = _.cloneDeep(INIT_STATE);
    this.setState(state);
  };

  render() {
    const {
      intl: { formatMessage },
    } = this.props;
    const {
      app,
      isLocalProject,
      versionId,
      appId,
      versionOptions,
      versionLoading,
      displayAppList,
    } = this.state;

    const appExist = !_.isEmpty(app) && !!app;

    const openBtnClass = classnames({
      'c7ncd-detail-btn': true,
      'c7ncd-detail-btn-right': appExist,
    });

    return (
      <Fragment>
        <p className="c7ncd-step-describe">
          {formatMessage({ id: 'deploy.step.one.description' })}
        </p>
        <div className="c7ncd-step-item">
          <div className="c7ncd-step-item-header">
            <Icon className="c7ncd-step-item-icon" type="widgets" />
            <span className="c7ncd-step-item-title">
              {formatMessage({ id: 'deploy.step.one.app' })}
            </span>
          </div>
          <div className="c7ncd-step-item-indent">
            <div className="c7ncd-step-item-app">
              {appExist && (
                <AppName
                  width="366px"
                  name={`${app.name}(${app.code})`}
                  showIcon
                  self={isLocalProject}
                />
              )}
            </div>
            <Permission
              service={[
                'devops-service.application.pageByOptions',
                'devops-service.application-market.listAllApp',
              ]}
            >
              <Button
                className={openBtnClass}
                onClick={this.openAppList}
              >
                <FormattedMessage id="deploy.app.add" />
                <Icon type="open_in_new" />
              </Button>
            </Permission>
          </div>
        </div>
        <div className="c7ncd-step-item">
          <div className="c7ncd-step-item-header">
            <Icon className="c7ncd-step-item-icon" type="version" />
            <span className="c7ncd-step-item-title">
              {formatMessage({ id: 'deploy.step.one.version.title' })}
            </span>
          </div>
          <div className="c7ncd-step-item-indent is-required">
            <Select
              filter
              disabled={!appExist}
              className="c7ncd-step-input"
              label={<FormattedMessage id="deploy.step.one.version" />}
              optionFilterProp="children"
              style={{ width: 482 }}
              onSelect={this.handleSelectVersion}
              onSearch={this.handleVersionSearch}
              value={versionOptions.length ? versionId : undefined}
              loading={versionLoading}
              notFoundContent={formatMessage({ id: 'network.form.version.notFount' })}
              filterOption={false}
            >
              {versionOptions}
            </Select>
          </div>
        </div>
        <ButtonGroup
          disabled={!(appId && versionId)}
          onNext={this.handleNext}
          onCancel={this.handleCancel}
        />
        {displayAppList && (
          <AppList
            isMarket={!isLocalProject}
            app={app}
            show={displayAppList}
            handleCancel={this.closeAppList}
            handleOk={this.handleSelectApp}
          />
        )}
      </Fragment>
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
