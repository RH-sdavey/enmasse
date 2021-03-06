/*
 * Copyright 2018, EnMasse authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.enmasse.systemtest.common.upgrade;


import io.enmasse.systemtest.*;
import io.enmasse.systemtest.bases.TestBase;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

import static io.enmasse.systemtest.TestTag.upgrade;

@Tag(upgrade)
class UpgradeTest extends TestBase {

    private static Logger log = CustomLogger.getLogger();

    @Test
    void testFunctionalityBeforeAndAfterUpgrade() throws Exception {
        AddressSpace brokered = new AddressSpace("brokered-addr-space", AddressSpaceType.BROKERED, AuthService.STANDARD);
        AddressSpace standard = new AddressSpace("standard-addr-space", AddressSpaceType.STANDARD, AuthService.STANDARD);
        List<Destination> standardAddresses = getAllStandardAddresses();
        List<Destination> brokeredAddresses = getAllBrokeredAddresses();

        List<Destination> brokeredQueues = getQueues(brokeredAddresses);
        List<Destination> standardQueues = getQueues(standardAddresses);

        UserCredentials cred = new UserCredentials("kornelius", "korny");
        int msgCount = 13;

        if (!environment.isUpgraded()) {
            log.info("Before upgrade phase");
            createAddressSpaceList(brokered, standard);

            createUser(brokered, cred);
            createUser(standard, cred);

            setAddresses(brokered, brokeredAddresses.toArray(new Destination[0]));
            setAddresses(standard, standardAddresses.toArray(new Destination[0]));

            assertCanConnect(brokered, cred, brokeredAddresses);
            assertCanConnect(standard, cred, standardAddresses);

            log.info("Send durable messages to brokered queue");
            for (Destination dest : brokeredQueues) {
                sendDurableMessages(brokered, dest, cred, msgCount);
            }
            log.info("Send durable messages to standard queues");
            for (Destination dest : standardQueues) {
                sendDurableMessages(standard, dest, cred, msgCount);
            }
            Thread.sleep(10_000);
            log.info("End of before upgrade phase");
        } else {
            log.info("After upgrade phase");

            brokered = getAddressSpace(brokered.getName());
            standard = getAddressSpace(standard.getName());

            waitForAddressSpaceReady(brokered);
            waitForAddressSpaceReady(standard);

            Thread.sleep(120_000);

            log.info("Receive durable messages from brokered queue");
            for (Destination dest : brokeredQueues) {
                receiveDurableMessages(brokered, dest, cred, msgCount);
            }
            log.info("Receive durable messages from standard queues");
            for (Destination dest : standardQueues) {
                receiveDurableMessages(standard, dest, cred, msgCount);
            }

            assertCanConnect(brokered, cred, brokeredAddresses);
            assertCanConnect(standard, cred, standardAddresses);

            log.info("End of after upgrade phase");

            log.info("Send durable messages to brokered queue");
            for (Destination dest : brokeredQueues) {
                sendDurableMessages(brokered, dest, cred, msgCount);
            }
            log.info("Send durable messages to standard queues");
            for (Destination dest : standardQueues) {
                sendDurableMessages(standard, dest, cred, msgCount);
            }
        }
    }

    private List<Destination> getQueues(List<Destination> addresses) {
        return addresses.stream().filter(dest -> dest.getType()
                .equals(AddressType.QUEUE.toString())).collect(Collectors.toList());
    }
}
