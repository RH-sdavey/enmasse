FROM enmasseproject/java-base:8-11

RUN yum -y install openssl && yum -y clean all

ARG version
ARG maven_version
ARG commit
ENV VERSION=${version} COMMIT=${commit} MAVEN_VERSION=${maven_version}
ENV KEYCLOAK_PLUGIN_DIR /keycloak-plugin

ADD ./build/keycloak-plugin-${MAVEN_VERSION}.tar.gz ${KEYCLOAK_PLUGIN_DIR}/

ENTRYPOINT [ "sh", "-c", "${KEYCLOAK_PLUGIN_DIR}/bin/init-keycloak.sh" ]
