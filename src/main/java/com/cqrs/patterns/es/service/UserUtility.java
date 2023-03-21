package com.cqrs.patterns.es.service;

import java.util.List;
import java.util.UUID;

import com.cqrs.patterns.domain.Address;
import com.cqrs.patterns.domain.Contact;
import com.cqrs.patterns.domain.User;
import com.cqrs.patterns.es.events.Event;
import com.cqrs.patterns.es.events.UserAddressAddedEvent;
import com.cqrs.patterns.es.events.UserAddressRemovedEvent;
import com.cqrs.patterns.es.events.UserContactAddedEvent;
import com.cqrs.patterns.es.events.UserContactRemovedEvent;
import com.cqrs.patterns.es.events.UserCreatedEvent;
import com.cqrs.patterns.es.repository.EventStore;

public class UserUtility {

    public static User recreateUserState(EventStore store, String userId) {
        User user = null;

        List<Event> events = store.getEvents(userId);
        for (Event event : events) {
            if (event instanceof UserCreatedEvent) {
                UserCreatedEvent e = (UserCreatedEvent) event;
                user = new User(UUID.randomUUID()
                    .toString(), e.getFirstName(), e.getLastName());
            }
            if (event instanceof UserAddressAddedEvent) {
                UserAddressAddedEvent e = (UserAddressAddedEvent) event;
                Address address = new Address(e.getCity(), e.getState(), e.getPostCode());
                if (user != null)
                    user.getAddresses()
                        .add(address);
            }
            if (event instanceof UserAddressRemovedEvent) {
                UserAddressRemovedEvent e = (UserAddressRemovedEvent) event;
                Address address = new Address(e.getCity(), e.getState(), e.getPostCode());
                if (user != null)
                    user.getAddresses()
                        .remove(address);
            }
            if (event instanceof UserContactAddedEvent) {
                UserContactAddedEvent e = (UserContactAddedEvent) event;
                Contact contact = new Contact(e.getContactType(), e.getContactDetails());
                if (user != null)
                    user.getContacts()
                        .add(contact);
            }
            if (event instanceof UserContactRemovedEvent) {
                UserContactRemovedEvent e = (UserContactRemovedEvent) event;
                Contact contact = new Contact(e.getContactType(), e.getContactDetails());
                if (user != null)
                    user.getContacts()
                        .remove(contact);
            }
        }

        return user;
    }

}
