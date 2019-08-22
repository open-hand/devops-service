import React, { Component, Fragment } from 'react';
import { observer } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Content, Header, Page, Permission, stores } from '@choerodon/master';
import { Button, Select, Modal, Form, Icon, Collapse, Avatar, Pagination, Tooltip } from 'choerodon-ui';
import ReactMarkdown from 'react-markdown';
import _ from 'lodash';
import Loading from '../../../../components/loading';
import TimePopover from '../../../../components/timePopover';
import DevPipelineStore from '../../stores/DevPipelineStore';
import handleMapStore from '../../main-view/store/handleMapStore';
import AppTagStore from './stores';
import '../../../main.less';
import './style/AppTag.scss';
import AppTagCreate from './AppTagCreate';
import AppTagEdit from './AppTagEdit';

const { AppState } = stores;
const { Panel } = Collapse;

@observer
class AppTag extends Component {
  constructor(props) {
    super(props);
    handleMapStore.setCodeManagerAppTag({
      refresh: this.handleRefresh,
      select: this.handleSelect,
      getSelfToolBar: this.getSelfToolBar,
    });
    this.state = {
      page: 1,
      pageSize: 10,
      visible: false,
      deleteLoading: false,
      tag: null,
      editTag: null,
      editRelease: null,
      creationDisplay: false,
      editDisplay: false,
      appName: null,
    };
  }

  componentDidMount() {
    AppTagStore.setLoading(null);
    AppTagStore.setTagData([]);
    this.loadInitData();
    this.handleRefresh();
  }


  /**
   * 生成特殊的自定义tool-bar
   */
  getSelfToolBar= () => {
    const appData = DevPipelineStore.getAppData;
    const { type, id: projectId, organizationId: orgId } = AppState.currentMenuType;
    return appData && appData.length ? (
      <Permission
        service={[
          'devops-service.devops-git.createTag',
        ]}
        type={type}
        projectId={projectId}
        organizationId={orgId}
      >
        <Button
          type="primary"
          funcType="flat"
          icon="playlist_add"
          onClick={() => this.displayCreateModal(true)}
        >
          <FormattedMessage id="apptag.create" />
        </Button>
      </Permission>
    ) : null;
  }


  /**
   * 通过下拉选择器选择应用时，获取应用id
   * @param id
   * @param option
   */
  handleSelect = (id, option) => {
    this.setState({ page: 1, pageSize: 10, appName: option.props.children });
    DevPipelineStore.setSelectApp(id);
    DevPipelineStore.setRecentApp(id);
    this.loadTagData();
  };

  /**
   * 页面内刷新，选择器变回默认选项
   */
  handleRefresh = () => {
    const { page, pageSize } = this.state;
    this.loadTagData(page, pageSize);
  };

  /**
   * 加载应用信息
   */
  loadInitData = () => {
    this.setState({ appName: null });
  };

  /**
   * 加载刷新tag列表信息
   * @param page
   * @param pageSize
   */
  loadTagData = (page = 1, pageSize = 10) => {
    const { projectId } = AppState.currentMenuType;
    AppTagStore.queryTagData(projectId, page, pageSize);
  };

  /**
   * 分页器
   * @param current
   * @param size
   */
  handlePaginChange = (current, size) => {
    this.setState({ page: current, pageSize: size });
    this.loadTagData(current, size);
  };

  /**
   * 打开删除确认框
   * @param tag
   */
  openRemove = (e, tag) => {
    e.stopPropagation();
    this.setState({ visible: true, tag });
  };

  /**
   * 关闭删除确认框
   */
  closeRemove = () => this.setState({ visible: false });

  /**
   * 删除tag
   */
  deleteTag = () => {
    const { projectId } = AppState.currentMenuType;
    const { tag } = this.state;
    this.setState({ deleteLoading: true });
    AppTagStore.deleteTag(projectId, tag).then((data) => {
      if (data && data.failed) {
        Choerodon.prompt(data.message);
      } else {
        this.loadTagData();
      }
      this.setState({ deleteLoading: false, visible: false });
    }).catch((error) => {
      this.setState({ deleteLoading: false });
      Choerodon.handleResponseError(error);
    });
  };

  /**
   * 控制创建窗口显隐
   * @param flag
   */
  displayCreateModal = (flag) => this.setState({ creationDisplay: flag });

  /**
   * 控制编辑窗口
   * @param flag 显隐
   * @param tag tag名称
   * @param res release内容
   * @param e
   */
  displayEditModal = (flag, tag, res, e) => {
    let editTag = null;
    let editRelease = null;
    if (tag) {
      e.stopPropagation();
      editTag = tag;
      editRelease = res;
    }
    this.setState({ editDisplay: flag, editTag, editRelease });
  };

  render() {
    const { intl: { formatMessage }, history: { location: { state } } } = this.props;
    const { type, id: projectId, organizationId: orgId, name } = AppState.currentMenuType;
    const { visible, deleteLoading, creationDisplay, appName, editDisplay, editTag, editRelease, tag } = this.state;
    const appData = DevPipelineStore.getAppData;
    const appId = DevPipelineStore.getSelectApp;
    const titleName = _.find(appData, ['id', appId]) ? _.find(appData, ['id', appId]).name : name;
    const tagData = AppTagStore.getTagData;
    const loading = AppTagStore.getLoading;
    const currentAppName = appName || DevPipelineStore.getDefaultAppName;
    const { current, total, pageSize } = AppTagStore.pageInfo;
    const tagList = [];
    const backPath = state && state.backPath;
    _.forEach(tagData, (item) => {
      const {
        commit: {
          authorName,
          committedDate,
          message: commitMsg,
          shortId,
          url,
        },
        commitUserImage,
        release,
      } = item;
      const header = (<div className="c7n-tag-panel">
        <div className="c7n-tag-panel-info">
          <div className="c7n-tag-panel-name">
            <Icon type="local_offer" />
            <span>{release.tagName}</span>
          </div>
          <div className="c7n-tag-panel-detail">
            <Icon className="c7n-tag-icon-point" type="point" />
            <a href={url} rel="nofollow me noopener noreferrer" target="_blank">{shortId}</a>
            <span className="c7n-divide-point">&bull;</span>
            <span className="c7n-tag-msg">{commitMsg}</span>
            <span className="c7n-divide-point">&bull;</span>
            <span className="c7n-tag-panel-person">
              {commitUserImage
                ? <Avatar className="c7n-tag-commit-img" src={commitUserImage} />
                : <span className="c7n-tag-commit c7n-tag-commit-avatar">{authorName.toString().substr(0, 1)}</span>}
              <span className="c7n-tag-commit">{authorName}</span>
            </span>
            <span className="c7n-divide-point">&bull;</span>
            <div className="c7n-tag-time"><TimePopover content={committedDate} /></div>
          </div>
        </div>
        <div className="c7n-tag-panel-opera">
          <Permission
            service={[
              'devops-service.devops-git.updateTagRelease',
            ]}
            type={type}
            projectId={projectId}
            organizationId={orgId}
          >
            <Tooltip
              placement="bottom"
              title={<FormattedMessage id="edit" />}
            >
              <Button
                shape="circle"
                size="small"
                icon="mode_edit"
                onClick={(e) => this.displayEditModal(true, release.tagName, release, e)}
              />
            </Tooltip>
          </Permission>
          <Permission
            type={type}
            projectId={projectId}
            organizationId={orgId}
            service={[
              'devops-service.devops-git.deleteTag',
            ]}
          >
            <Tooltip
              placement="bottom"
              title={<FormattedMessage id="delete" />}
            >
              <Button
                shape="circle"
                size="small"
                icon="delete_forever"
                onClick={(e) => this.openRemove(e, release.tagName)}
              />
            </Tooltip>
          </Permission>
        </div>
      </div>);
      tagList.push(<Panel
        header={header}
        key={release.tagName}
      >
        <div className="c7n-tag-release">{release ? <div className="c7n-md-parse c7n-md-preview">
          <ReactMarkdown
            source={release.description !== 'empty' ? release.description : formatMessage({ id: 'apptag.release.empty' })}
            skipHtml={false}
            escapeHtml={false}
          />
        </div> : formatMessage({ id: 'apptag.release.empty' })}</div>
      </Panel>);
    });
    const empty = appData && appData.length ? 'tag' : 'app';
    return (
      <Page
        className="c7n-tag-wrapper"
        service={[
          'devops-service.application.listByActive',
          'devops-service.devops-git.getTagByPage',
          'devops-service.devops-git.listByAppId',
          'devops-service.devops-git.updateTagRelease',
          'devops-service.devops-git.createTag',
          'devops-service.devops-git.checkTag',
          'devops-service.devops-git.deleteTag',
        ]}
      >
        {appData && appData.length && appId ? <Fragment>
          {/* <Header
          title={<FormattedMessage id="apptag.head" />}
          backPath={backPath}
        >
          <Select
            filter
            className="c7n-header-select"
            dropdownClassName="c7n-header-select_drop"
            placeholder={formatMessage({ id: 'ist.noApp' })}
            value={appData && appData.length ? DevPipelineStore.getSelectApp : undefined}
            disabled={appData.length === 0}
            filterOption={(input, option) => option.props.children.props.children.props.children
              .toLowerCase().indexOf(input.toLowerCase()) >= 0}
            onChange={(value, option) => this.handleSelect(value, option)}
          >
            <OptGroup label={formatMessage({ id: 'recent' })} key="recent">
              {
                _.map(DevPipelineStore.getRecentApp, app => (
                  <Option
                    key={`recent-${app.id}`}
                    value={app.id}
                    disabled={!app.permission}
                  >
                    <Tooltip title={app.code}><span className="c7n-ib-width_100">{app.name}</span></Tooltip>
                  </Option>))
              }
            </OptGroup>
            <OptGroup label={formatMessage({ id: 'deploy.app' })} key="app">
              {
                _.map(appData, (app, index) => (
                  <Option
                    value={app.id}
                    key={index}
                    disabled={!app.permission}
                  >
                    <Tooltip title={app.code}><span className="c7n-ib-width_100">{app.name}</span></Tooltip>
                  </Option>))
              }
            </OptGroup>
          </Select>
          {appData && appData.length ? (
            <Permission
              service={[
                'devops-service.devops-git.createTag',
              ]}
              type={type}
              projectId={projectId}
              organizationId={orgId}
            >
              <Button
                type="primary"
                funcType="flat"
                icon="playlist_add"
                onClick={() => this.displayCreateModal(true)}
              >
                <FormattedMessage id="apptag.create" />
              </Button>
            </Permission>
          ) : null}
          <Button
            type="primary"
            funcType="flat"
            icon="refresh"
            onClick={this.handleRefresh}
          >
            <FormattedMessage id="refresh" />
          </Button>
        </Header> */}
          {/* <Content code={appData.length ? 'apptag.app' : 'apptag'} values={{ name: titleName }}> */}
          <Content className="c7n-tag-content">
            {/* <div className="c7n-tag-table"><FormattedMessage id="apptag.table" /></div> */}
            {loading || _.isNull(loading) ? <Loading display /> : <Fragment>
              {tagList.length ? <Fragment>
                <Collapse bordered={false}>{tagList}</Collapse>
                <div className="c7n-tag-pagin">
                  <Pagination
                    total={total}
                    current={current}
                    pageSize={pageSize}
                    onChange={this.handlePaginChange}
                    onShowSizeChange={this.handlePaginChange}
                  />
                </div>
              </Fragment> : (<div className="c7n-tag-empty">
                <div>
                  <Icon type="info" className="c7n-tag-empty-icon" />
                  <span className="c7n-tag-empty-text">{formatMessage({ id: `apptag.${empty}.empty` })}</span>
                </div>
                {empty === 'tag' ? (
                  <Button
                    type="primary"
                    funcType="raised"
                    onClick={() => this.displayCreateModal(true, empty)}
                  >
                    <FormattedMessage id="apptag.create" />
                  </Button>
                ) : null}
              </div>)}
            </Fragment>}
          </Content>
          <Modal
            confirmLoading={deleteLoading}
            visible={visible}
            title={`${formatMessage({ id: 'apptag.action.delete' })}“${tag}”`}
            closable={false}
            footer={[
              <Button key="back" onClick={this.closeRemove} disabled={deleteLoading}>{<FormattedMessage id="cancel" />}</Button>,
              <Button key="submit" type="danger" onClick={this.deleteTag} loading={deleteLoading}>
                {formatMessage({ id: 'delete' })}
              </Button>,
            ]}
          ><div className="c7n-padding-top_8">{formatMessage({ id: 'apptag.delete.tooltip' })}</div></Modal>
          {creationDisplay ? <AppTagCreate
            app={titleName}
            store={AppTagStore}
            show={creationDisplay}
            close={this.displayCreateModal}
          /> : null}
          {editDisplay ? <AppTagEdit
            app={currentAppName}
            store={AppTagStore}
            tag={editTag}
            release={editRelease}
            show={editDisplay}
            close={this.displayEditModal}
          /> : null}</Fragment> : null}
      </Page>
    );
  }
}

export default Form.create({})(withRouter(injectIntl(AppTag)));
