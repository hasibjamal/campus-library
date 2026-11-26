package edu.campus.library.infra.persistence;

import edu.campus.library.domain.Repository;
import edu.campus.library.domain.catalog.CatalogRepository;
import edu.campus.library.domain.loan.Loan;
import edu.campus.library.domain.member.Member;
import edu.campus.library.domain.room.Room;
import edu.campus.library.domain.room.RoomReservation;

/**
 * Abstract Factory (L3). Creates a whole family of repositories that must
 * match (all in memory, or all on file). App picks one factory; mixing
 * families by mistake is impossible.
 */
public interface RepositoryFactory {
    Repository<Member> members();

    Repository<Loan> loans();

    Repository<Room> rooms();

    Repository<RoomReservation> roomReservations();

    CatalogRepository catalog();
}
