import React, { Component, Fragment } from 'react';
import { observer } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Content, Header, Page, Permission, stores, Action, Choerodon } from '@choerodon/boot';
import { Button, Select, Modal, Form, Icon, Collapse, Avatar, Pagination, Tooltip } from 'choerodon-ui';
import ReactMarkdown from 'react-markdown';
import { Modal as ProModal } from 'choerodon-ui/pro';
import _ from 'lodash';
import Loading from '../../../../components/loading';
import TimePopover from '../../../../components/timePopover';
import UserInfo from '../../../../components/userInfo';
import DevPipelineStore from '../../stores/DevPipelineStore';
import handleMapStore from '../../main-view/store/handleMapStore';
import StatusIcon from '../../../../components/StatusIcon/StatusIcon';
import CustomConfirm from '../../../../components/custom-confirm';
import AppTagStore from './stores';
import '../../../main.less';
import './style/AppTag.less';
import AppTagCreate from './AppTagCreate';
import AppTagEdit from './AppTagEdit';
import ClickText from '../../../../components/click-text';
import EmptyPage from '../../../../components/empty-page';

const { AppState } = stores;
const { Panel } = Collapse;
const deleteKey = ProModal.key();

@observer
class AppTag extends Component {
  constructor(props) {
    super(props);
    const { formatMessage } = this.props.intl;
    this.customConfirm = new CustomConfirm({ formatMessage });
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
  }


  /**
   * 生成特殊的自定义tool-bar
   */
  getSelfToolBar= () => ( 
    <Permission
      service={[
        'devops-service.devops-git.createTag',
        'devops-service.devops-git.checkTag',
      ]}
    >
      <Button
        type="primary"
        funcType="flat"
        icon="playlist_add"
        onClick={() => this.displayCreateModal(true)}
        disabled={!DevPipelineStore.getSelectApp}
      >
        <FormattedMessage id="apptag.create" />
      </Button>
    </Permission>)


  /**
   * 通过下拉选择器选择应用时，获取应用id
   * @param id
   * @param option
   */
  handleSelect = (id, option) => {
    this.setState({ page: 1, pageSize: 10, appName: option.props.children });
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
  openRemove = (tag) => {
    const { intl: { formatMessage } } = this.props;
    this.setState({ tag });
    ProModal.open({
      key: deleteKey,
      title: formatMessage({ id: 'apptag.action.delete.title' }, { name: tag }),
      children: formatMessage({ id: 'apptag.delete.tooltip' }),
      onOk: this.deleteTag,
      okText: formatMessage({ id: 'delete' }),
      okProps: { color: 'red' },
      cancelProps: { color: 'dark' },
    });
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
    AppTagStore.setLoading(true);
    AppTagStore.deleteTag(projectId, tag).then((data) => {
      AppTagStore.setLoading(false);
      if (data && data.failed) {
        Choerodon.prompt(data.message);
      } else {
        this.loadTagData();
      }
    }).catch((error) => {
      AppTagStore.setLoading(false);
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
  displayEditModal = (flag, res, tag, ...rest) => {
    const e = rest.pop();
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
            <div className="c7n-tag-name" onClick={this.displayEditModal.bind(this, true, release, release.tagName)}>
              <span className="c7n-tag-name-text">{release.tagName}</span>
            </div>
            <div className="c7n-tag-action" onClick={stopPropagation}>
              <Action data={[
                {
                  service: [
                    'devops-service.devops-git.deleteTag',
                  ],
                  text: formatMessage({ id: 'delete' }),
                  action: () => this.openRemove(release.tagName),
                },
              ]}
              />
            </div>
          </div>
          <div className="c7n-tag-panel-detail">
            <Icon className="c7n-tag-icon-point" type="point" />
            <a href={url} rel="nofollow me noopener noreferrer" target="_blank">{shortId}</a>
            <span className="c7n-divide-point">&bull;</span>
            <span className="c7n-tag-msg">{commitMsg}</span>
            <span className="c7n-divide-point">&bull;</span>
            <span className="c7n-tag-panel-person">
              <UserInfo
                name={authorName || ''}
                avatar={commitUserImage}
              />
            </span>
            <span className="c7n-divide-point">&bull;</span>
            <div className="c7n-tag-time"><TimePopover content={committedDate} /></div>
          </div>
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
        className="c7n-tag-wrapper page-container"
        service={['devops-service.devops-git.pageTagsByOptions']}
      >
        {appData && appData.length && appId ? <div className="c7ncd-tag-content">
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
            </Fragment> : (
              <EmptyPage
                title={formatMessage({ id: 'code-management.tag.empty' })}
                describe={formatMessage({ id: 'code-management.tag.empty.des' })}
                btnText={formatMessage({ id: 'apptag.create' })}
                onClick={() => this.displayCreateModal(true, empty)}
                access
              />
            )}
          </Fragment>}
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
          /> : null}</div> : <Loading display={DevPipelineStore.getLoading} />}
      </Page>
    );
  }
}

export default Form.create({})(withRouter(injectIntl(AppTag)));


function stopPropagation(e) {
  e.stopPropagation();
}
