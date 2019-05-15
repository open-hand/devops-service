/**
 * @author ale0720@163.com
 * @date 2019-05-09 22:11
 */
import React from 'react';
import { injectIntl } from 'react-intl';
import { Input, Form } from 'choerodon-ui';
import _ from 'lodash';
import { Consumer } from './EditableContext';

const { TextArea } = Input;
const { Item: FormItem } = Form;


@injectIntl
export default class EditableCell extends React.Component {
  state = {
    editing: false,
    pasting: false,
    oldValue: '',
  };

  componentDidMount() {
    if (this.props.editable) {
      document.addEventListener('click', this.handleClickOutside, true);
    }
  }

  componentWillUnmount() {
    if (this.props.editable) {
      document.removeEventListener('click', this.handleClickOutside, true);
    }
  }

  /**
   * 切换输入编辑状态
   */
  toggleEdit = () => {
    const editing = !this.state.editing;
    this.setState({ editing }, () => {
      if (editing) {
        this.input.focus();
      }
    });
  };

  /**
   * 点击外部触发保存切换编辑状态
   * @param e
   */
  handleClickOutside = (e) => {
    const { editing } = this.state;
    if (editing && this.cell !== e.target && !this.cell.contains(e.target)) {
      this.handleSave();
    }
  };

  /**
   * from获取value 保存
   */
  handleSave = () => {
    const { record, save } = this.props;

    this.form.validateFields((error, values) => {
      if (error) return;

      this.toggleEdit();
      if (values.key === '' || values.key === null) {
        record.index = '';
      }
      save({ ...record, ...values });
    });
  };

  /**
   * input change触发
   * 判断是否粘贴，处理数据调用add函数
   * 添加key-value
   * @param e
   */
  onChange = (e) => {
    const { add } = this.props;
    const { oldValue, pasting } = this.state;

    if (pasting) {
      const value = oldValue !== '' ? (e.target.value.substring(oldValue.length) || e.target.value) : e.target.value;
      if (value.includes('=')) {
        const KValue = _.map(value.split('\n'), str => {
          if (str) {
            return _.map(str.split('='), s => s.trim());
          }
        });
        this.handleSave();
        add(_.filter(KValue, item => item));
      }
    }

    this.setState({ pasting: false });
  };

  /**
   * 判断粘贴事件
   * @param e
   */
  onKeyDown = (e) => {
    if (e.keyCode === 86 && (e.ctrlKey || e.metaKey)) {
      this.setState({ pasting: true, oldValue: e.target.value });
    }
  };

  /**
   * 校验key
   * @param rule
   * @param value
   * @param callback
   */
  checkKey = (rule, value, callback) => {
    const { intl } = this.props;
    const pattern = /[^0-9A-Za-z\.\-\_]/;
    if (pattern.test(value) && rule.field === 'key') {
      callback(intl.formatMessage({ id: 'configMap.keyRule' }));
    } else {
      callback();
    }
  };

  render() {
    const { editing } = this.state;
    const {
      title,
      editable,
      dataIndex,
      record,
      intl,
      ...restProps
    } = this.props;
    return (
      <td ref={node => (this.cell = node)} {...restProps}>
        {editable ? (
          <Consumer>
            {(form) => {
              this.form = form;
              return (
                editing ? (
                  <FormItem style={{ margin: 0 }}>
                    {form.getFieldDecorator(dataIndex, {
                      initialValue: record[dataIndex],
                      rules: [
                        {
                          validator: this.checkKey,
                        },
                      ],
                    })(
                      <TextArea
                        label={intl.formatMessage({ id: dataIndex })}
                        ref={node => (this.input = node)}
                        autosize
                        onKeyDown={this.onKeyDown}
                        onChange={this.onChange}
                      />,
                    )}
                  </FormItem>
                ) : (
                  <TextArea
                    autosize
                    label={intl.formatMessage({ id: dataIndex })}
                    className="editable-cell-value-wrap"
                    onClick={this.toggleEdit}
                    onFocus={this.toggleEdit}
                    value={
                      title === 'secret'
                      && dataIndex === 'value'
                      && restProps.children.filter(a => typeof (a) === 'string').length
                        ? '******'
                        : restProps.children.filter(a => typeof (a) === 'string')}
                  />
                )
              );
            }}
          </Consumer>
        ) : restProps.children}
      </td>
    );
  }
}
