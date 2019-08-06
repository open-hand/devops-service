#!/bin/bash
set -e

find /dist -name '*.js' | xargs sed -i "s localhost:http $PRO_HTTP g"
find /dist -name '*.js' | xargs sed -i "s localhost:8080 $PRO_API_HOST g"
find /dist -name '*.js' | xargs sed -i "s localhost:clientId $PRO_CLIENT_ID g"
find /dist -name '*.js' | xargs sed -i "s localhost:local $PRO_LOCAL g"
find /dist -name '*.js' | xargs sed -i "s localhost:headertitlename $PRO_HEADER_TITLE_NAME g"
find /dist -name '*.js' | xargs sed -i "s localhost:cookieServer $PRO_COOKIE_SERVER g"
find /dist -name '*.html' | xargs sed -i "s localhost:titlename $PRO_TITLE_NAME g"
find /dist -name '*.js' | xargs sed -i "s localhost:fileserver $PRO_FILE_SERVER g"
find /dist -name '*.js' | xargs sed -i "s localhost:wsserver $PRO_WEBSOCKET_SERVER g"
find /dist -name '*.js' | xargs sed -i "s localhost:apimgateway $PRO_APIM_GATEWAY g"

exec "$@"