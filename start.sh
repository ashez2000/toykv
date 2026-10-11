run_node() {
    local tag="$1"
    shift
    java "$@" 2>&1 | sed -u "s/^/[$tag] /"
}

run_node Node-8080 -cp target/toykv-1.0-SNAPSHOT.jar \
    io.github.ashez2000.toykv.App 8080 \
    http://localhost:8081 http://localhost:8082 &

run_node Node-8081 -cp target/toykv-1.0-SNAPSHOT.jar \
    io.github.ashez2000.toykv.App 8081 \
    http://localhost:8080 http://localhost:8082 &

run_node Node-8082 -cp target/toykv-1.0-SNAPSHOT.jar \
    io.github.ashez2000.toykv.App 8082 \
    http://localhost:8080 http://localhost:8081 &

wait
