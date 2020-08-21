export default (({ projectId }) => ({
  autoQuery: false,
  selection: false,
  paging: false,
  transport: {
    read: ({ data }) => {
      const { clusterId } = data || {};
      return ({
        url: `/devops/v1/projects/${projectId}/clusters/${clusterId}/node_names`,
        method: 'get',
        transformResponse: (response) => {
          try {
            const res = JSON.parse(response);
            if (res && res.failed) {
              return data;
            } else {
              return res.map((item) => ({
                value: item,
              }));
            }
          } catch (e) {
            return response;
          }
        },
      });
    },
  },
}));
