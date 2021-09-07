set -e
WORK_DIR={{ WORK_DIR }}
APP_FILE_NAME={{ APP_FILE_NAME }}
APP_FILE={{ APP_FILE }}
cd ${WORK_DIR}
nohup {{ COMMAND }} > ${WORK_DIR}/app.log 2>&1 &
echo $!
