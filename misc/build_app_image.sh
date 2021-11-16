mvn -Djavafx.platform=linux clean verify

cd phoebus-product/target
mkdir product-4.6.6-SNAPSHOT
tar xzf product-4.6.6-SNAPSHOT.zip -C "product-4.6.6-SNAPSHOT"
cd product-4.6.6-SNAPSHOT
mkdir deploy
cp -r product-4.6.6-SNAPSHOT/ deploy/
cd deploy
jpackage --name phoebus --input product-4.6.6-SNAPSHOT --type app-image --main-jar product-4.6.6-SNAPSHOT.jar --java-options --java-options --runtime-image /usr/lib/jvm/java-11-openjdk-amd64/

