package de.tlongo.serveranalytics.test;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by tomas on 10/9/14.
 */
public interface FooRepo extends JpaRepository<Foo, Long> {
}
