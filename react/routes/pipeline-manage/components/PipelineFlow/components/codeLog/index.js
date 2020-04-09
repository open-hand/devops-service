import React, { useEffect, useState } from 'react';
import ReactCodeMirror from 'react-codemirror';
import './index.less';
import { observer } from 'mobx-react-lite';
import { axios } from '@choerodon/boot';


const LOG_OPTIONS = {
  readOnly: true,
  lineNumbers: true,
  lineWrapping: true,
  autofocus: true,
  theme: 'base16-dark',
};

export default observer((props) => {
  const [value, setValue] = useState('');
  const { gitlabJobId, projectId, gitlabProjectId } = props;
  function loadData() {
    axios.get(`/devops/v1/projects/${projectId}/ci_jobs/gitlab_projects/${gitlabProjectId}/gitlab_jobs/${gitlabJobId}/trace`).then((res) => {
      if (res && !res.failed) {
        setValue(res);
      }
    });
  }

  useEffect(() => {
    loadData();
  }, []);

  return (<div className="c7n-pipelineManage-codeLog">
    {value && <ReactCodeMirror
      value={value}
      className="c7n-log-editor"
      options={LOG_OPTIONS}
    />}
  </div>);
});
