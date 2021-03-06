include ../Makefile.common

PACKAGE_DOCS_DIR=$(TOPDIR)/documentation/html
PACKAGE_ANSIBLE_DIR=$(TOPDIR)/ansible
BUILDDIR=build
INSTALLNAME=enmasse-$(TAG)
INSTALLDIR=$(BUILDDIR)/$(INSTALLNAME)
PACKAGE_INSTALL_DIR=$(INSTALLDIR)/install
MODULES=address-space-controller extra api-server api-service grafana none-authservice prometheus service-broker cluster-service-broker standard-authservice tenant alertmanager kube-state-metrics

prepare:
	mkdir -p $(PACKAGE_INSTALL_DIR)
	mkdir -p $(PACKAGE_INSTALL_DIR)/templates
	mkdir -p $(PACKAGE_INSTALL_DIR)/bundles
	mkdir -p $(PACKAGE_INSTALL_DIR)/components

replace_images: prepare
	mkdir -p $(BUILDDIR)/replaced
	cp -r $(MODULES) $(BUILDDIR)/replaced
	for i in `find $(BUILDDIR)/replaced -name "*.yaml"`; do \
		cat $$i | $(IMAGE_ENV) envsubst '$${ENMASSE_VERSION},$${IMAGE_PULL_POLICY},$${ADDRESS_SPACE_CONTROLLER_IMAGE},$${STANDARD_CONTROLLER_IMAGE},$${ROUTER_IMAGE},$${NONE_AUTHSERVICE_IMAGE},$${KEYCLOAK_IMAGE},$${KEYCLOAK_CONTROLLER_IMAGE},$${KEYCLOAK_PLUGIN_IMAGE},$${TOPIC_FORWARDER_IMAGE},$${ARTEMIS_IMAGE},$${ARTEMIS_PLUGIN_IMAGE},$${ROUTER_METRICS_IMAGE},$${SUBSERV_IMAGE},$${API_SERVER_IMAGE},$${SERVICE_BROKER_IMAGE},$${AGENT_IMAGE},$${MQTT_GATEWAY_IMAGE},$${MQTT_LWT_IMAGE},$${PROMETHEUS_IMAGE},$${ALERTMANAGER_IMAGE},$${GRAFANA_IMAGE},$${KUBE_STATE_METRICS_IMAGE},$${OAUTH_PROXY_IMAGE}' > $$i.tmp; \
		mv $$i.tmp $$i; \
	done

component_install: replace_images
	cp -r $(BUILDDIR)/replaced/* $(PACKAGE_INSTALL_DIR)/components/
	for i in `find $(PACKAGE_INSTALL_DIR)/components -name "*.yaml"`; do \
		cat $$i | NAMESPACE=$(DEFAULT_PROJECT) envsubst '$${NAMESPACE}' > $$i.tmp; \
		mv $$i.tmp $$i; \
	done

ansible_install: component_install
	cp -r $(PACKAGE_ANSIBLE_DIR) $(INSTALLDIR)/
	$(LN) -srf $(INSTALLDIR)/install/components $(INSTALLDIR)/ansible/playbooks/openshift/components

ENMASSE_WITH_STANDARD_AUTHSERVICE_TEMPLATE=$(PACKAGE_INSTALL_DIR)/templates/enmasse-with-standard-authservice.yaml
$(ENMASSE_WITH_STANDARD_AUTHSERVICE_TEMPLATE): replace_images
	echo 'apiVersion: v1' > $(ENMASSE_WITH_STANDARD_AUTHSERVICE_TEMPLATE)
	echo 'kind: Template' >> $(ENMASSE_WITH_STANDARD_AUTHSERVICE_TEMPLATE)
	echo 'metadata:' >> $(ENMASSE_WITH_STANDARD_AUTHSERVICE_TEMPLATE)
	echo '  name: enmasse-with-standard-authservice' >> $(ENMASSE_WITH_STANDARD_AUTHSERVICE_TEMPLATE)
	echo 'parameters:' >> $(ENMASSE_WITH_STANDARD_AUTHSERVICE_TEMPLATE)
	echo '- name: NAMESPACE' >> $(ENMASSE_WITH_STANDARD_AUTHSERVICE_TEMPLATE)
	echo '  description: Namespace where EnMasse should be deployed' >> $(ENMASSE_WITH_STANDARD_AUTHSERVICE_TEMPLATE)
	echo '  required: true' >> $(ENMASSE_WITH_STANDARD_AUTHSERVICE_TEMPLATE)
	echo 'objects:' >> $(ENMASSE_WITH_STANDARD_AUTHSERVICE_TEMPLATE)
	for mod in address-space-controller api-server api-service standard-authservice tenant; do \
		for i in `find $(BUILDDIR)/replaced/$${mod} -name "*.yaml" | sort`; do \
			echo "Mod $${mod} file $${i}"; \
			cat $$i | sed 's/^/  /' | sed '1s/^  /- /' >> $(ENMASSE_WITH_STANDARD_AUTHSERVICE_TEMPLATE); \
		done; \
	done

ENMASSE_WITH_NONE_AUTHSERVICE_TEMPLATE=$(PACKAGE_INSTALL_DIR)/templates/enmasse-with-none-authservice.yaml
$(ENMASSE_WITH_NONE_AUTHSERVICE_TEMPLATE): replace_images
	echo 'apiVersion: v1' > $(ENMASSE_WITH_NONE_AUTHSERVICE_TEMPLATE)
	echo 'kind: Template' >> $(ENMASSE_WITH_NONE_AUTHSERVICE_TEMPLATE)
	echo 'metadata:' >> $(ENMASSE_WITH_NONE_AUTHSERVICE_TEMPLATE)
	echo '  name: enmasse-with-none-authservice' >> $(ENMASSE_WITH_NONE_AUTHSERVICE_TEMPLATE)
	echo 'parameters:' >> $(ENMASSE_WITH_NONE_AUTHSERVICE_TEMPLATE)
	echo '- name: NAMESPACE' >> $(ENMASSE_WITH_NONE_AUTHSERVICE_TEMPLATE)
	echo '  description: Namespace where EnMasse should be deployed' >> $(ENMASSE_WITH_NONE_AUTHSERVICE_TEMPLATE)
	echo '  required: true' >> $(ENMASSE_WITH_NONE_AUTHSERVICE_TEMPLATE)
	echo 'objects:' >> $(ENMASSE_WITH_NONE_AUTHSERVICE_TEMPLATE)
	for mod in address-space-controller api-server api-service none-authservice tenant; do \
		for i in `find $(BUILDDIR)/replaced/$${mod} -name "*.yaml" | sort`; do \
			cat $$i | sed 's/^/  /' | sed '1s/^  /- /' >> $(ENMASSE_WITH_NONE_AUTHSERVICE_TEMPLATE); \
		done; \
	done

ENMASSE_WITH_STANDARD_AUTHSERVICE_MONITORING_TEMPLATE=$(PACKAGE_INSTALL_DIR)/templates/enmasse-with-standard-authservice-monitoring.yaml
$(ENMASSE_WITH_STANDARD_AUTHSERVICE_MONITORING_TEMPLATE): replace_images
	echo 'apiVersion: v1' > $(ENMASSE_WITH_STANDARD_AUTHSERVICE_MONITORING_TEMPLATE)
	echo 'kind: Template' >> $(ENMASSE_WITH_STANDARD_AUTHSERVICE_MONITORING_TEMPLATE)
	echo 'metadata:' >> $(ENMASSE_WITH_STANDARD_AUTHSERVICE_MONITORING_TEMPLATE)
	echo '  name: enmasse-with-standard-authservice' >> $(ENMASSE_WITH_STANDARD_AUTHSERVICE_MONITORING_TEMPLATE)
	echo 'parameters:' >> $(ENMASSE_WITH_STANDARD_AUTHSERVICE_MONITORING_TEMPLATE)
	echo '- name: NAMESPACE' >> $(ENMASSE_WITH_STANDARD_AUTHSERVICE_MONITORING_TEMPLATE)
	echo '  description: Namespace where EnMasse should be deployed' >> $(ENMASSE_WITH_STANDARD_AUTHSERVICE_MONITORING_TEMPLATE)
	echo '  required: true' >> $(ENMASSE_WITH_STANDARD_AUTHSERVICE_MONITORING_TEMPLATE)
	echo 'objects:' >> $(ENMASSE_WITH_STANDARD_AUTHSERVICE_MONITORING_TEMPLATE)
	for mod in address-space-controller api-server api-service standard-authservice tenant alertmanager grafana kube-state-metrics prometheus; do \
		for i in `find $(BUILDDIR)/replaced/$${mod} -name "*.yaml" | sort`; do \
			echo "Mod $${mod} file $${i}"; \
			cat $$i | sed 's/^/  /' | sed '1s/^  /- /' >> $(ENMASSE_WITH_STANDARD_AUTHSERVICE_MONITORING_TEMPLATE); \
		done; \
	done

ENMASSE_WITH_NONE_AUTHSERVICE_BUNDLE=$(PACKAGE_INSTALL_DIR)/bundles/enmasse-with-none-authservice
$(ENMASSE_WITH_NONE_AUTHSERVICE_BUNDLE): replace_images
	mkdir -p $(ENMASSE_WITH_NONE_AUTHSERVICE_BUNDLE)
	for i in address-space-controller extra api-server api-service none-authservice tenant; do \
		cp $(BUILDDIR)/replaced/$$i/*.yaml $(ENMASSE_WITH_NONE_AUTHSERVICE_BUNDLE)/; \
	done
	for i in `find $(ENMASSE_WITH_NONE_AUTHSERVICE_BUNDLE) -name "*.yaml"`; do \
		cat $$i | NAMESPACE=$(DEFAULT_PROJECT) envsubst '$${NAMESPACE}' > $$i.tmp; \
		mv $$i.tmp $$i; \
	done

ENMASSE_WITH_STANDARD_AUTHSERVICE_BUNDLE=$(PACKAGE_INSTALL_DIR)/bundles/enmasse-with-standard-authservice
$(ENMASSE_WITH_STANDARD_AUTHSERVICE_BUNDLE): replace_images
	mkdir -p $(ENMASSE_WITH_STANDARD_AUTHSERVICE_BUNDLE)
	for i in address-space-controller extra api-server api-service standard-authservice tenant; do \
		cp $(BUILDDIR)/replaced/$$i/*.yaml $(ENMASSE_WITH_STANDARD_AUTHSERVICE_BUNDLE)/; \
	done
	for i in `find $(ENMASSE_WITH_STANDARD_AUTHSERVICE_BUNDLE) -name "*.yaml"`; do \
		cat $$i | NAMESPACE=$(DEFAULT_PROJECT) envsubst '$${NAMESPACE}' > $$i.tmp; \
		mv $$i.tmp $$i; \
	done

ENMASSE_WITH_STANDARD_AUTHSERVICE_MONITORING_BUNDLE=$(PACKAGE_INSTALL_DIR)/bundles/enmasse-with-standard-authservice-monitoring
$(ENMASSE_WITH_STANDARD_AUTHSERVICE_MONITORING_BUNDLE): replace_images
	mkdir -p $(ENMASSE_WITH_STANDARD_AUTHSERVICE_MONITORING_BUNDLE)
	for i in address-space-controller extra api-server api-service standard-authservice tenant alertmanager grafana kube-state-metrics prometheus; do \
		cp $(BUILDDIR)/replaced/$$i/*.yaml $(ENMASSE_WITH_STANDARD_AUTHSERVICE_MONITORING_BUNDLE)/; \
	done
	for i in `find $(ENMASSE_WITH_STANDARD_AUTHSERVICE_MONITORING_BUNDLE) -name "*.yaml"`; do \
		cat $$i | NAMESPACE=$(DEFAULT_PROJECT) envsubst '$${NAMESPACE}' > $$i.tmp; \
		mv $$i.tmp $$i; \
	done

install: ansible_install component_install $(ENMASSE_WITH_STANDARD_AUTHSERVICE_BUNDLE) $(ENMASSE_WITH_NONE_AUTHSERVICE_BUNDLE) $(ENMASSE_WITH_STANDARD_AUTHSERVICE_MONITORING_BUNDLE) $(ENMASSE_WITH_STANDARD_AUTHSERVICE_TEMPLATE) $(ENMASSE_WITH_NONE_AUTHSERVICE_TEMPLATE) $(ENMASSE_WITH_STANDARD_AUTHSERVICE_MONITORING_TEMPLATE)
	cp -r $(PACKAGE_DOCS_DIR) $(INSTALLDIR)/docs

package: prepare install
	tar -czf build/$(INSTALLNAME).tgz -C build $(INSTALLNAME)

coverage:

.PHONY: prepare package clean $(ENMASSE_WITH_STANDARD_AUTHSERVICE_BUNDLE) $(ENMASSE_WITH_NONE_AUTHSERVICE_BUNDLE) $(ENMASSE_WITH_STANDARD_AUTHSERVICE_MONITORING_BUNDLE) $(ENMASSE_WITH_STANDARD_AUTHSERVICE_TEMPLATE) $(ENMASSE_WITH_NONE_AUTHSERVICE_TEMPLATE) $(ENMASSE_WITH_STANDARD_AUTHSERVICE_MONITORING_BUNDLE)
