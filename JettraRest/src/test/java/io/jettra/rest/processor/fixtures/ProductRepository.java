package io.jettra.rest.processor.fixtures;

import io.jettra.rest.annotations.DenyAll;
import io.jettra.rest.annotations.PermitAll;
import io.jettra.rest.annotations.Repository;
import io.jettra.rest.annotations.RestRepository;
import io.jettra.rest.annotations.accreditation.RolesAllowed;

import java.util.List;

@Repository
@RestRepository
public interface ProductRepository {

    Persona findById(String id);

    @PermitAll
    List<Persona> findAll();

    @RolesAllowed({"ADMIN", "EDITOR"})
    Persona save(Persona persona);

    @RolesAllowed({"ADMIN"})
    Persona update(String id, Persona persona);

    @RolesAllowed({"ADMIN"})
    boolean deleteById(String id);

    List<Persona> searchByName(String name);

    @DenyAll
    void purgeInternal();
}
