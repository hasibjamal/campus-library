package edu.campus.library.infra.persistence;

import edu.campus.library.domain.Repository;
import edu.campus.library.domain.catalog.CatalogRepository;
import edu.campus.library.domain.loan.Loan;
import edu.campus.library.domain.member.Member;
import edu.campus.library.domain.room.Room;
import edu.campus.library.domain.room.RoomReservation;

public class InMemoryRepositoryFactory implements RepositoryFactory {
    @Override
    public Repository<Member> members() {
        return new InMemoryRepository<>();
    }

    @Override
    public Repository<Loan> loans() {
        return new InMemoryRepository<>();
    }

    @Override
    public Repository<Room> rooms() {
        return new InMemoryRepository<>();
    }

    @Override
    public Repository<RoomReservation> roomReservations() {
        return new InMemoryRepository<>();
    }

    @Override
    public CatalogRepository catalog() {
        return new InMemoryCatalogRepository();
    }
}
