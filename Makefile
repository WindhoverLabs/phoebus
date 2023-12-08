build:
	 mvn -DskipTests   install -T6
dev-build:
	mvn -Dfmt.skip -DskipTests  install -T6

