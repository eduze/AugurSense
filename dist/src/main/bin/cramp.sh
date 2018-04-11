if [[ -z "${DB_URL}" ]]; then
    DB_URL="jdbc:mysql://localhost:3306/analytics?createDatabaseIfNotExist=true"
fi

if [[ -z "${DB_USER}" ]]; then
    DB_USER="root"
fi

if [[ -z "${DB_PASSWORD}" ]]; then
    DB_PASSWORD="root"
fi

if [[ -z "${CRAMP_MODE}" ]]; then
    CRAMP_MODE="PASSIVE"
fi

echo "DB_URL: ${DB_URL}"
echo "DB_USER: ${DB_USER}"
echo "MODE: ${CRAMP_MODE}"

CRAMP_HOME=.

for file in `find ${CRAMP_HOME}/lib/*.jar`; do
     LIB_DEPS="${file}:${LIB_DEPS}"
done

LIB_DEPS="${CRAMP_HOME}/etc/log4j2.xml:${LIB_DEPS}"

case "$1" in
  start)
        echo "Starting CRAMP ..."

        $JAVA_HOME/bin/java \
                    -cp ${LIB_DEPS} \
                    -Ddb.user=${DB_USER} \
                    -Ddb.password=${DB_PASSWORD} \
                    -Ddb.jdbc.url=${DB_URL} \
                    -Dorg.augur.sense.mode=${CRAMP_MODE} \
                    -Dlog4j.configurationFile=${CRAMP_HOME}/etc/log4j2.xml \
                    org.augur.sense.ui.CHASS "$@" &

        echo $! > ${CRAMP_HOME}/run/cramp.pid
        echo "STARTED"
        sleep 2
        ;;
  stop)
        echo -n "Shutting down CRAMP"
        kill `cat ${CRAMP_HOME}/run/cramp.pid`
        rm -f ${CRAMP_HOME}/run/cramp.pid
        echo "OK"
        ;;
  restart)
        $0 stop
        $0 start
        ;;
  status)
        if [ -e ${CRAMP_HOME}/run/cramp.pid ] ; then
           pid=`cat ${CRAMP_HOME}/run/cramp.pid`
           echo "CRAMP is running with pid: $pid"
        else
           echo "CRAMP is not running"
        fi
        ;;
  *)
        echo "Usage: $0 {start|stop|restart|status}"
        exit 1
esac

sleep 1
exit 0