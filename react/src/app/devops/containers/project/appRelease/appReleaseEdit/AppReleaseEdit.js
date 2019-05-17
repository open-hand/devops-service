import React, { Component } from 'react';
import { observer, inject } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { Button, Form, Input } from 'choerodon-ui';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Content, Header, Page, Permission, stores } from '@choerodon/boot';
import '../../../main.scss';
import './AppReleaseEdit.scss';

const FormItem = Form.Item;
const formItemLayout = {
  labelCol: {
    xs: { span: 24 },
    sm: { span: 100 },
  },
  wrapperCol: {
    xs: { span: 24 },
    sm: { span: 26 },
  },
};
const { TextArea } = Input;
const { AppState } = stores;

@observer
class AppReleaseEdit extends Component {
  constructor(props) {
    const menu = AppState.currentMenuType;
    super(props);
    this.state = {
      id: props.match.params.id || '',
      projectId: menu.id,
    };
  }

  componentDidMount() {
    const { EditReleaseStore } = this.props;
    const { projectId, id } = this.state;
    EditReleaseStore.loadDataById(projectId, id);
  }

  /**
   * 处理图片回显
   * @param img
   * @param callback
   */
  getBase64 =(img, callback) => {
    const reader = new FileReader();
    reader.addEventListener('load', () => callback(reader.result));
    reader.readAsDataURL(img);
  };

  /**
   * 提交数据
   * @param e
   */
  handleSubmit =(e) => {
    e.preventDefault();
    const { EditReleaseStore } = this.props;
    const { projectId, id, img } = this.state;
    this.props.form.validateFieldsAndScroll((err, data) => {
      if (!err) {
        const postData = data;
        postData.imgUrl = img;
        postData.publishLevel = EditReleaseStore.getSingleData.publishLevel;
        postData.id = id;
        postData.appId = EditReleaseStore.getSingleData.appId;
        EditReleaseStore.updateData(projectId, id, postData)
          .then((datass) => {
            this.setState({ submitting: false });
            if (datass) {
              this.handleBack();
            }
          }).catch((errs) => {
            this.setState({ submitting: false });
            Choerodon.prompt(errs.response.data.message);
          });
      }
    });
  };

  /**
   * 图标的上传button显示
   */
  showBth =() => {
    this.setState({ showBtn: true });
  };

  /**
   * 图标的上传button隐藏
   */
  hideBth =() => {
    this.setState({ showBtn: false });
  };

  /**
   * 触发上传按钮
   */
  triggerFileBtn =() => {
    this.setState({ showBtn: true });
    const ele = document.getElementById('file');
    ele.click();
  };

  /**
   * 选择文件
   * @param e
   */
  selectFile =(e) => {
    const { EditReleaseStore } = this.props;
    const formdata = new FormData();
    const img = e.target.files[0];
    formdata.append('file', e.target.files[0]);
    EditReleaseStore.uploadFile('devops-service', img.name.split('.')[0], formdata)
      .then((data) => {
        if (data) {
          this.setState({ img: data });
          this.getBase64(formdata.get('file'), (imgUrl) => {
            const ele = document.getElementById('img');
            ele.style.backgroundImage = `url(${imgUrl})`;
          });
        }
      });
    this.setState({ showBtn: false });
  };

  /**
   * 返回上一级
   */
  handleBack =() => {
    const menu = AppState.currentMenuType;
    const { EditReleaseStore } = this.props;
    EditReleaseStore.setSelectData([]);
    EditReleaseStore.setSingleData(null);
    this.props.history.push(`/devops/app-release/2?type=${menu.type}&id=${menu.id}&name=${menu.name}&organizationId=${menu.organizationId}`);
  };

  render() {
    const { EditReleaseStore } = this.props;
    const { getFieldDecorator } = this.props.form;
    const menu = AppState.currentMenuType;
    const SingleData = EditReleaseStore.getSingleData;
    const contentDom = (<div>
      <Form layout="vertical" onSubmit={this.handleSubmit}>
        <FormItem
          className="c7n-sidebar-form"
          {...formItemLayout}
        >
          <div className="c7n-appRelease-img">
            <div
              style={{ backgroundImage: SingleData && SingleData.imgUrl !== null ? `url(${Choerodon.fileServer(SingleData.imgUrl)})` : '' }}
              className="c7n-appRelease-img-hover"
              id="img"
              onMouseLeave={this.hideBth}
              onMouseEnter={this.showBth}
              onClick={this.triggerFileBtn}
              role="none"
            >
              {this.state.showBtn && <div className="c7n-appRelease-img-child">
                <i className="icon icon-photo_camera" />
              </div>
              }
              <Input id="file" type="file" onChange={this.selectFile} style={{ display: 'none' }} />
            </div>
            <span className="c7n-appRelease-img-title">{this.props.intl.formatMessage({ id: 'release.add.step.four.app.icon' })}</span>
          </div>
        </FormItem>
        <FormItem
          className="c7n-sidebar-form"
          {...formItemLayout}
        >
          {getFieldDecorator('contributor', {
            rules: [{
              required: true,
              whitespace: true,
              message: this.props.intl.formatMessage({ id: 'required' }),
            }],
            initialValue: SingleData ? SingleData.contributor : menu.name,
          })(
            <Input
              maxLength={30}
              label={<FormattedMessage id="appstore.contributor" />}
              size="default"
            />,
          )}
        </FormItem>
        <FormItem
          className="c7n-sidebar-form"
          {...formItemLayout}
        >
          {getFieldDecorator('category', {
            rules: [{
              required: true,
              whitespace: true,
              message: this.props.intl.formatMessage({ id: 'required' }),
            }],
            initialValue: SingleData ? SingleData.category : '',
          })(
            <Input
              maxLength={10}
              label={<FormattedMessage id="appstore.category" />}
              size="default"
            />,
          )}
        </FormItem>
        <FormItem
          className="c7n-sidebar-form"
          {...formItemLayout}
        >
          {getFieldDecorator('description', {
            rules: [{
              required: true,
              whitespace: true,
              message: this.props.intl.formatMessage({ id: 'required' }),
            }, {
            }],
            initialValue: SingleData ? SingleData.description : '',
          })(
            <TextArea
              maxLength={100}
              label={<FormattedMessage id="appstore.description.label" />}
              autosize={{ minRows: 2, maxRows: 6 }}
            />,
          )}
        </FormItem>
        <div className="c7n-appRelease-hr" />
        <FormItem
          className="c7n-sidebar-form"
          {...formItemLayout}
        >
          <Permission service={['devops-service.application-market.update']}>
            <Button
              onClick={this.handleSubmit}
              type="primary"
              funcType="raised"
              className="sidebar-btn"
              style={{ marginRight: 12 }}
              loading={this.state.submitting}
            >
              {<FormattedMessage id="save" />}
            </Button>
          </Permission>
          <Button
            funcType="raised"
            disabled={this.state.submitting}
            onClick={this.handleBack}
          >
            {<FormattedMessage id="cancel" />}
          </Button>
        </FormItem>
      </Form>
    </div>);
    return (
      <Page
        service={[
          'devops-service.application-market.queryAppInProject',
          'devops-service.application-market.update',
        ]}
        className="c7n-region"
      >
        <Header title={<FormattedMessage id="release.edit.header.title" />} backPath={`/devops/app-release/2?type=${menu.type}&id=${menu.id}&name=${menu.name}&organizationId=${menu.organizationId}`} />
        <Content className="c7n-appRelease-wrapper" code="release.edit" vales={{ name: AppState.currentMenuType.name }}>
          {contentDom}
        </Content>
      </Page>
    );
  }
}

export default Form.create({})(withRouter(injectIntl(AppReleaseEdit)));
