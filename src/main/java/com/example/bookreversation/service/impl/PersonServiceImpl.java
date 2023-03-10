package com.example.bookreversation.service.impl;

import com.example.bookreversation.dto.requests.PersonRequest;
import com.example.bookreversation.entity.Person;
import com.example.bookreversation.events.CreatePersonEvents;
import com.example.bookreversation.repository.PersonRepository;
import com.example.bookreversation.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;


@Service
public class PersonServiceImpl implements PersonService {

    private final PersonRepository personRepository;

    private ApplicationEventPublisher eventPublisher;

    @Autowired
    public PersonServiceImpl(PersonRepository personRepository, ApplicationEventPublisher eventPublisher) {
        this.personRepository = personRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public List<Person> getAllBook() {
        List<Person> people = personRepository.findAll();
        return people;
    }

    @Override
    public Person getById(int id) {
        Person person = personRepository.getById(id);


        person.getBooks().forEach(
                book -> {
                    long Milli = Math.abs(book.getTakenAt().getTime() - new Date().getTime());
                    if(Milli > 864000000) {
                        book.setExpired(true);
                    }
                }
        );


        return person;
    }

    @Override
    public String addPerson(PersonRequest personRequest) {
        Person person = new Person();

        person.setFirstName(personRequest.getFirstName());
        person.setLastName(personRequest.getLastName());
        person.setDateBirthday(personRequest.getDateBirthday());

        personRepository.save(person);

        CreatePersonEvents createPersonEvents = new CreatePersonEvents(
                this,
                "Create User and Add in DB!!!"
        );
        eventPublisher.publishEvent(createPersonEvents);

        return "Person Created";
    }

    @Override
    public String updatePerson(PersonRequest personRequest, int id) {
        Person person2 = new Person();

        Person person = personRepository.findById(id)
                .map(person1 -> {
                    person1.setFirstName(personRequest.getFirstName());
                    person1.setLastName(personRequest.getLastName());
                    person1.setDateBirthday(personRequest.getDateBirthday());
                    return personRepository.save(person1);
                })
                .orElseGet(() -> {
                    person2.setId(personRequest.getId());
                    return personRepository.save(person2);
                });
        return "Updated person!";
    }

    @Override
    public String  deletePerson(int id) {
        personRepository.deleteById(id);

        return "Delete person!";
    }

}
