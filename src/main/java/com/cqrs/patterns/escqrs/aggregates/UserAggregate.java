package com.cqrs.patterns.escqrs.aggregates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.cqrs.patterns.cqrs.commands.CreateUserCommand;
import com.cqrs.patterns.cqrs.commands.UpdateUserCommand;
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
import com.cqrs.patterns.es.service.UserUtility;

public class UserAggregate {

    private final EventStore writeRepository;

    public UserAggregate(EventStore repository) {
        this.writeRepository = repository;
    }

    public List<Event> handleCreateUserCommand(CreateUserCommand command) {
        UserCreatedEvent event = new UserCreatedEvent(command.getUserId(), command.getFirstName(), command.getLastName());
        writeRepository.addEvent(command.getUserId(), event);
        return List.of(event);
    }

    public List<Event> handleUpdateUserCommand(UpdateUserCommand command) {
        User user = UserUtility.recreateUserState(writeRepository, command.getUserId());
        List<Event> events = new ArrayList<>();

        List<Contact> contactsToRemove = user.getContacts()
                .stream()
                .filter(c -> !command.getContacts()
                        .contains(c)).toList();
        for (Contact contact : contactsToRemove) {
            UserContactRemovedEvent contactRemovedEvent = new UserContactRemovedEvent(contact.getType(), contact.getDetail());
            events.add(contactRemovedEvent);
            writeRepository.addEvent(command.getUserId(), contactRemovedEvent);
        }

        List<Contact> contactsToAdd = command.getContacts()
                .stream()
                .filter(c -> !user.getContacts()
                        .contains(c)).toList();
        for (Contact contact : contactsToAdd) {
            UserContactAddedEvent contactAddedEvent = new UserContactAddedEvent(contact.getType(), contact.getDetail());
            events.add(contactAddedEvent);
            writeRepository.addEvent(command.getUserId(), contactAddedEvent);
        }

        List<Address> addressesToRemove = user.getAddresses()
                .stream()
                .filter(a -> !command.getAddresses()
                        .contains(a)).toList();
        for (Address address : addressesToRemove) {
            UserAddressRemovedEvent addressRemovedEvent = new UserAddressRemovedEvent(address.getCity(), address.getState(), address.getPostcode());
            events.add(addressRemovedEvent);
            writeRepository.addEvent(command.getUserId(), addressRemovedEvent);
        }

        List<Address> addressesToAdd = command.getAddresses()
                .stream()
                .filter(a -> !user.getAddresses()
                        .contains(a)).toList();
        for (Address address : addressesToAdd) {
            UserAddressAddedEvent addressAddedEvent = new UserAddressAddedEvent(address.getCity(), address.getState(), address.getPostcode());
            events.add(addressAddedEvent);
            writeRepository.addEvent(command.getUserId(), addressAddedEvent);
        }

        return events;
    }

}
