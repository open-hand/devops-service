export default ({ handleDataSetChange }) => ({
  autoCreate: true,
  events: {
    update: handleDataSetChange,
  },
  fields: [
    { name: 'appServiceId', type: 'string' },
  ],
});
