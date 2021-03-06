/*
 * Copyright 2018, EnMasse authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.enmasse.address.model;

import io.enmasse.admin.model.v1.AddressPlan;
import io.enmasse.config.AnnotationKeys;

import java.util.Optional;

public class AddressResolver {
    private final AddressSpaceType addressSpaceType;

    public AddressResolver(AddressSpaceType addressSpaceType) {
        this.addressSpaceType = addressSpaceType;
    }

    public AddressPlan getPlan(Address address) {
        return findPlan(address).orElseThrow(() -> new UnresolvedAddressException("Unknown address plan " + address.getSpec().getPlan() + " for address type " + address.getSpec().getType()));
    }

    public Optional<AddressPlan> findPlan(Address address) {
        return getType(address).findAddressPlan(address.getSpec().getPlan());
    }

    public AddressPlan getPlan(AddressType addressType, String plan) {
        return addressType.findAddressPlan(plan).orElseThrow(() -> new UnresolvedAddressException("Unknown address plan " + plan + " for address type " + addressType.getName()));
    }

    public AddressPlan getPlan(AddressType addressType, Address address) {
        return addressType.findAddressPlan(address.getAnnotation(AnnotationKeys.APPLIED_PLAN))
                .orElse(addressType.findAddressPlan(address.getSpec().getPlan()).orElseThrow(() -> new UnresolvedAddressException("Unknown address plan " + address.getSpec().getPlan() + " for address type " + address.getSpec().getType())));
    }

    public AddressType getType(Address address) {
        return addressSpaceType.findAddressType(address.getSpec().getType()).orElseThrow(() -> new UnresolvedAddressException("Unknown address type " + address.getSpec().getType()));
    }

    public void validate(Address address) {
        getPlan(getType(address), address);
    }
}
