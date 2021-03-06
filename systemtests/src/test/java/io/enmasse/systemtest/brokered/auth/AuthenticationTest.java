/*
 * Copyright 2017-2018, EnMasse authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.enmasse.systemtest.brokered.auth;

import io.enmasse.systemtest.*;
import io.enmasse.systemtest.ability.ITestBaseBrokered;
import io.enmasse.systemtest.bases.auth.AuthenticationTestBase;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import static io.enmasse.systemtest.TestTag.nonPR;
import static io.enmasse.systemtest.TestTag.noneAuth;

class AuthenticationTest extends AuthenticationTestBase implements ITestBaseBrokered {
    private static Logger log = CustomLogger.getLogger();

    /**
     * related github issue: #523
     */
    @Test
    void testStandardAuthenticationServiceRestartBrokered() throws Exception {
        log.info("testStandardAuthenticationServiceRestartBrokered");
        AddressSpace addressSpace = new AddressSpace("keycloak-restart-brokered", AddressSpaceType.BROKERED, AuthService.STANDARD);
        createAddressSpace(addressSpace);

        UserCredentials credentials = new UserCredentials("pavel", "novak");
        createUser(addressSpace, credentials);

        assertCanConnect(addressSpace, credentials, amqpAddressList);

        scaleKeycloak(0);
        scaleKeycloak(1);
        Thread.sleep(160000);

        assertCanConnect(addressSpace, credentials, amqpAddressList);
    }

    @Test
    @Tag(nonPR)
    void testStandardAuthenticationServiceBrokered() throws Exception {
        testStandardAuthenticationServiceGeneral(AddressSpaceType.BROKERED);
    }

    @Test
    @Tag(noneAuth)
    void testNoneAuthenticationServiceBrokered() throws Exception {
        testNoneAuthenticationServiceGeneral(AddressSpaceType.BROKERED, anonymousUser, anonymousPswd);
    }
}
