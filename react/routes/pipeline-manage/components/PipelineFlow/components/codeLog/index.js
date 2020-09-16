import React, { useEffect, useState } from 'react';
import forEach from 'lodash/forEach';
import { Terminal } from 'xterm';
import { fit } from 'xterm/lib/addons/fit/fit';
import { observer } from 'mobx-react-lite';
import { axios, Choerodon } from '@choerodon/boot';

import 'xterm/dist/xterm.css';
import './index.less';

export default observer((props) => {
  const {
    gitlabJobId, projectId, gitlabProjectId, type, cdRecordId, stageRecordId, jobRecordId,
  } = props;
  const term = new Terminal({
    fontSize: 13,
    fontWeight: 400,
    fontFamily: 'monospace',
    disableStdin: true,
  });

  async function loadData() {
    try {
      if (type === 'cdHost') {
        const res = await axios.get(`/devops/v1/projects/${projectId}/pipeline_records/${cdRecordId}/stage_records/${stageRecordId}/job_records/log/${jobRecordId}`);
        if (res && !res.failed) {
          const newRes = res.split(/\n/);
          forEach(newRes, (item) => term.writeln(item));
        }
      } else {
        const res = await axios.get(`/devops/v1/projects/${projectId}/ci_jobs/gitlab_projects/${gitlabProjectId}/gitlab_jobs/${gitlabJobId}/trace`);
        if (res && !res.failed) {
          const newRes = res.split(/\n/);
          forEach(newRes, (item) => term.writeln(item));
        }
      }
    } catch (e) {
      Choerodon.handleResponseError(e);
    }
  }

  useEffect(() => {
    loadData();
    term.open(document.getElementById('jobLog'));
    fit(term);
  }, []);

  return (
    <div className="c7n-pipelineManage-codeLog" id="jobLog" />
  );
});
