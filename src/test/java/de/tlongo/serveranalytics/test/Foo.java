package de.tlongo.serveranalytics.test;

import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

/**
 * Created by Tomas Longo on 10/9/14.
 */
@Entity(name = "foo")
public class Foo {
    @Type(type="de.tlongo.serveranalytics.services.logfileservice.LocalDateTimeUserType")
    public LocalDateTime date;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    long id;

    public Foo() {

    }

    public Foo(LocalDateTime date) {
        this.date = date;
    }
}
