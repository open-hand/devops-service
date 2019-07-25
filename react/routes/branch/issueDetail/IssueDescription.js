/* eslint-disable react/no-danger */
import React, { Component } from 'react';
import { injectIntl, FormattedMessage } from 'react-intl';
import Lightbox from 'react-image-lightbox';
import './IssueDescription.scss';

const QuillDeltaToHtmlConverter = require('quill-delta-to-html');

class IssueDescription extends Component {
  constructor(props, context) {
    super(props, context);
    this.state = {
      isOpen: {},
      src: '',
      open: false,
    };
  }

  componentDidMount() {
    window.addEventListener('click', (e) => {
      if (e.target.nodeName === 'IMG' && this.props.data && this.props.data.search(e.target.src) > -1) {
        e.stopPropagation();
        this.open(e.target.src);
      }
    });
  }

  onOpenLightboxChange = (index) => {
    const { isOpen } = this.state;
    isOpen[index] = !isOpen[index];
    this.setState({
      isOpen,
    });
  };

  getSubject = (data) => {
    const replyContents = [];
    const { intl: { formattedMessage }} = this.props;
    if (data) {
      JSON.parse(data).forEach((item, index) => {
        if (item.insert && item.insert.image) {
          replyContents.push(
            <span>
              <img
                role="none"
                src={item.insert.image}
                style={{ display: 'block', maxWidth: '600px' }}
                alt={formattedMessage({id: 'branch.issue.img' })}
              />
              {this.state.isOpen[`${index}`] ? (
                <Lightbox
                  mainSrc={item.insert.image}
                  onCloseRequest={() => this.onOpenLightboxChange(`${index}`)}
                  imageTitle="images"
                />
              ) : (
                ''
              )}
            </span>,
          );
        } else {
          const delta = [];
          delta.push(item);
          const converter = new QuillDeltaToHtmlConverter(delta, {});
          // 去掉p标签
          let text = converter.convert();
          if (converter.convert().substring(0, 3) === '<p>') {
            text = converter.convert().substring(3, converter.convert().length - 4);
          }
          replyContents.push(<span dangerouslySetInnerHTML={{ __html: `${this.escape(text)}` }} />);
        }
      });
    }
    return replyContents;
  };

  open = (src) => {
    this.setState({
      open: true,
      src,
    });
  };

  escape = str => str.replace(/<\/script/g, '<\\/script').replace(/<!--/g, '<\\!--');

  render() {
    return (
      <div className="c7n-read-delta" style={{ width: '100%' }}>
        <div dangerouslySetInnerHTML={{ __html: `${this.escape(this.props.data)}` }} />
        {
          this.state.open ? (
            <Lightbox
              mainSrc={this.state.src}
              onCloseRequest={() => this.setState({ open: false })}
              imageTitle="images"
            />
          ) : null
        }
      </div>
    );
  }
}

export default injectIntl(IssueDescription);
