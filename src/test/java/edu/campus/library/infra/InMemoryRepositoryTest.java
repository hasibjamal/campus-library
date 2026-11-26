package edu.campus.library.infra;

import edu.campus.library.infra.persistence.InMemoryRepositoryFactory;
import edu.campus.library.infra.persistence.RepositoryFactory;

class InMemoryRepositoryTest extends RepositoryContractTest {
    @Override
    protected RepositoryFactory factory() {
        return new InMemoryRepositoryFactory();
    }
}
