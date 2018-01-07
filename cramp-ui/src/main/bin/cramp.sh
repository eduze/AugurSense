CRAMP_HOME=.

for file in `find ${CRAMP_HOME}/lib/*.jar`; do
     LIB_DEPS="${file}:${LIB_DEPS}"
done

LIB_DEPS="${CRAMP_HOME}/etc/log4j2.xml:${LIB_DEPS}"

echo -n "Starting Accumulator ..."
$JAVA_HOME/bin/java \
        	-cp ${LIB_DEPS} \
        	org.eduze.fyp.ui.CHASS "$@"
echo "OK"
sleep 1
exit 0