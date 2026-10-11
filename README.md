# ToyKV

```bash
java -cp target/toykv-1.0-SNAPSHOT.jar io.github.ashez2000.toykv.App 8080 http://localhost:8081 http://localhost:8082
java -cp target/toykv-1.0-SNAPSHOT.jar io.github.ashez2000.toykv.App 8081 http://localhost:8080 http://localhost:8082
java -cp target/toykv-1.0-SNAPSHOT.jar io.github.ashez2000.toykv.App 8082 http://localhost:8080 http://localhost:8081

pkill -f 'io.github.ashez2000.toykv.App'
```
