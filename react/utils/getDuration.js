import moment from 'moment';

export default function getDuration({ value, unit = 's' }) {
  const duration = moment.duration(value, unit);
  const seconds = duration.seconds();
  const minutes = duration.minutes();
  const hours = duration.hours();
  const days = duration.days();
  return `${days ? `${days}天` : ''}${hours ? `${hours}小时` : ''}${minutes ? `${minutes}分钟` : ''}${seconds ? `${seconds}秒` : ''}`;
}
