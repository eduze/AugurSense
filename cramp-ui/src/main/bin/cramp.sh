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

echo "Starting Accumulator ..."

$JAVA_HOME/bin/java \
        	-cp ${LIB_DEPS} \
        	-Ddb.user=${DB_USER} \
        	-Ddb.password=${DB_PASSWORD} \
        	-Ddb.jdbc.url=${DB_URL} \
        	-Dorg.eduze.fyp.mode=${CRAMP_MODE} \
        	org.eduze.fyp.ui.CHASS "$@"

echo "FINISHED"
sleep 1
exit 0