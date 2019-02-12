/*
 * Copyright 2017-2018, EnMasse authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.enmasse.systemtest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import io.enmasse.systemtest.cmdclients.KubeCMDClient;
import io.enmasse.systemtest.executor.ExecutionResultData;
import io.fabric8.kubernetes.api.model.Pod;

public class GlobalLogCollector {
    private final static Logger log = CustomLogger.getLogger();
    private final Map<String, LogCollector> collectorMap = new HashMap<>();
    private final Kubernetes kubernetes;
    private final File logDir;
    private final String namespace;

    public GlobalLogCollector(Kubernetes kubernetes, File logDir) {
        this.kubernetes = kubernetes;
        this.logDir = logDir;
        this.namespace = kubernetes.getNamespace();
    }


    public synchronized void startCollecting(AddressSpace addressSpace) {
        log.info("Start collecting logs for address space {}", addressSpace);
        collectorMap.put(addressSpace.getInfraUuid(), new LogCollector(kubernetes, new File(logDir, addressSpace.getInfraUuid()), addressSpace.getInfraUuid()));
    }

    public synchronized void stopCollecting(String namespace) throws Exception {
        log.info("Stop collecting logs for pods in namespace {}", namespace);
        LogCollector collector = collectorMap.get(namespace);
        if (collector != null) {
            collector.close();
        }
        collectorMap.remove(namespace);
    }

    public void collectConfigMaps() {
        collectConfigMaps("global");
    }

    public void collectConfigMaps(String operation) {
        log.info("Collecting configmaps for namespace {}", namespace);
        kubernetes.getAllConfigMaps(namespace).getItems().forEach(configMap -> {
            try {
                Path confMapFile = resolveLogFile(configMap.getMetadata().getName() + "." + operation + ".configmap");
                log.info("config map '{}' will be archived with path: '{}'", configMap.getMetadata().getName(), confMapFile);
                if (!Files.exists(confMapFile)) {
                    try (BufferedWriter bf = Files.newBufferedWriter(confMapFile)) {
                        bf.write(configMap.toString());
                    }
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    /**
     * Collect logs from terminated pods in namespace
     */
    public void collectLogsTerminatedPods() {
        log.info("Store logs from all terminated pods in namespace '{}'", namespace);
        kubernetes.getLogsOfTerminatedPods(namespace).forEach((podName, podLogTerminated) -> {
            try {
                Path podLog = resolveLogFile(namespace + "." + podName + ".terminated.log");
                log.info("log of terminated '{}' pod will be archived with path: '{}'", podName, podLog);
                try (BufferedWriter bf = Files.newBufferedWriter(podLog)) {
                    bf.write(podLogTerminated);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    public void collectEvents() throws IOException {
        long timestamp = System.currentTimeMillis();
        log.info("Collecting events in {}", namespace);
        ExecutionResultData result = KubeCMDClient.getEvents(namespace);
        Path eventLog = resolveLogFile(namespace + ".events." + timestamp);
        try (BufferedWriter writer = Files.newBufferedWriter(eventLog)) {
            writer.write(result.getStdOut());
        } catch (Exception e) {
            log.error("Error collecting events for {}", namespace, e);
        }
    }

    public void collectRouterState(String operation) {
        log.info("Collecting router state in namespace {}", namespace);
        long timestamp = System.currentTimeMillis();
        kubernetes.listPods(Collections.singletonMap("capability", "router")).forEach(pod -> {
            collectRouterInfo(pod, "." + operation + ".autolinks." + timestamp, "qdmanage", "QUERY", "--type=autoLink");
            collectRouterInfo(pod, "." + operation + ".links." + timestamp, "qdmanage", "QUERY", "--type=link");
            collectRouterInfo(pod, "." + operation + ".connections." + timestamp, "qdmanage", "QUERY", "--type=connection");
            collectRouterInfo(pod, "." + operation + ".qdstat_a." + timestamp, "qdstat", "-a");
            collectRouterInfo(pod, "." + operation + ".qdstat_l." + timestamp, "qdstat", "-l");
            collectRouterInfo(pod, "." + operation + ".qdstat_n." + timestamp, "qdstat", "-n");
            collectRouterInfo(pod, "." + operation + ".qdstat_c." + timestamp, "qdstat", "-c");
        });
    }

    private void collectRouterInfo(Pod pod, String filesuffix, String command, String ... args) {
        List<String> allArgs = new ArrayList<>();
        allArgs.add(command);
        allArgs.add("--sasl-mechanisms=EXTERNAL");
        allArgs.add("--ssl-certificate=/etc/enmasse-certs/tls.crt");
        allArgs.add("--ssl-key=/etc/enmasse-certs/tls.key");
        allArgs.add("--ssl-trustfile=/etc/enmasse-certs/ca.crt");
        allArgs.add("--ssl-disable-peer-name-verify");
        allArgs.add("-b");
        allArgs.add("127.0.0.1:55671");
        allArgs.addAll(Arrays.asList(args));

        String output = kubernetes.runOnPod(pod, "router", allArgs.toArray(new String[0]));
        try {
            Path routerAutoLinks = resolveLogFile(pod.getMetadata().getName() + filesuffix);
            log.info("router info '{}' pod will be archived with path: '{}'", pod.getMetadata().getName(), routerAutoLinks);
            if (!Files.exists(routerAutoLinks)) {
                try (BufferedWriter bf = Files.newBufferedWriter(routerAutoLinks)) {
                    bf.write(output);
                }
            }
        } catch (IOException e) {
            log.warn("Error collecting router state", e);
        }
    }

    public void collectApiServerInfo() {
        log.info("Collecting info from api server");
        kubernetes.listPods(Collections.singletonMap("component", "api-server")).forEach(pod -> { collectJmap(pod); collectHeapDumps(pod); });
    }

    private void collectHeapDumps(Pod pod) {
        try {
            Path path = Paths.get(logDir.getPath(), namespace);
            File heapDumpDir = new File(
                    Files.createDirectories(path).toFile(),
                    pod.getMetadata().getName() + "-heapdumps");
            KubeCMDClient.copyPodContent(pod.getMetadata().getName(), "/heapdumps", heapDumpDir.getAbsolutePath());
        } catch (Exception e) {
            log.warn("Error collecting heap dumps from {}: {}", pod.getMetadata().getName(), e.getMessage());
        }
    }

    private void collectJmap(Pod pod) {
        kubernetes.runOnPod(pod, "api-server", "jmap", "-dump:live,format=b,file=/tmp/dump.bin", "1");
        try {
            Path jmapLog = resolveLogFile(pod.getMetadata().getName() + ".dump." + Instant.now().toString().replace(":", "_") + ".bin");
            KubeCMDClient.copyPodContent(pod.getMetadata().getName(), "/tmp/dump.bin", jmapLog.toAbsolutePath().toString());
        } catch (Exception e) {
            log.warn("Error collecting jmap state", e);
        }
    }

    /**
     * Create a new path inside the log directory, and ensure that the parent directory exists.
     * @param other the path segment, relative to the log directory.
     * @return The full path.
     * @throws IOException In case of any IO error
     */
    private Path resolveLogFile(final String other) throws IOException {
        return Files
                .createDirectories(Paths.get(logDir.getPath(), namespace))
                .resolve(other);
    }

}
