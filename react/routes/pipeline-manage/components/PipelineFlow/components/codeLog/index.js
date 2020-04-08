import React from 'react';
import ReactCodeMirror from 'react-codemirror';
import './index.less';


const LOG_OPTIONS = {
  readOnly: true,
  lineNumbers: true,
  lineWrapping: true,
  autofocus: true,
  theme: 'base16-dark',
};

const codeLog = () => (
  <div className="c7n-pipelineManage-codeLog">
    <ReactCodeMirror
      // ref={(editor) => {
      //   this.editorLog = editor;
      // }}
      value="Loading..."
      className="c7n-log-editor"
      options={LOG_OPTIONS}
    />
  </div>
);
export default codeLog;
