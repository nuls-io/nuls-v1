
jar -cvf ./contract-module/base/contract-vm/target/contract.jar -C ./contract-module/base/contract-vm/target/test-classes ./contracts/
jar -cvf ./contract-module/base/contract-vm/target/test-classes/contract.jar -C ./contract-module/base/contract-vm/target/test-classes ./testcontract/
jar -cvf /tmp/classes.jar -C /tmp ./








javac -encoding utf-8 -sourcepath ./nvm/src/main/java -d ./nvm/target/classes ./nvm/src/main/java/nuls/contract/examples/token/*.java

jar -cvf ./nvm/target/contract.jar -C ./nvm/target/classes ./nuls/contract/examples/token/

create


javac -encoding utf-8 -sourcepath ./nuls-contract-sdk/src/main/java -d ./nvm/target/classes ./nuls-contract-sdk/src/main/java/nuls/contract/sdk/examples/java/token/*.java

jar -cvf ./nvm/target/contract.jar -C ./nvm/target/classes ./nuls/contract/sdk/examples/java/token/



