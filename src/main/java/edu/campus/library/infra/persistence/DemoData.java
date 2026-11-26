package edu.campus.library.infra.persistence;

import edu.campus.library.domain.Repository;
import edu.campus.library.domain.catalog.Book;
import edu.campus.library.domain.catalog.CatalogRepository;
import edu.campus.library.domain.catalog.Section;
import edu.campus.library.domain.member.Member;
import edu.campus.library.domain.member.MemberType;
import edu.campus.library.domain.room.Room;

/** Fills empty repositories with a small demo data set. Does nothing if data exists. */
public final class DemoData {
    private DemoData() {
    }

    public static void seedIfEmpty(Repository<Member> members, Repository<Room> rooms, CatalogRepository catalog) {
        if (members.findAll().isEmpty()) {
            members.save(new Member("M-1", "Aigerim Sadykova", "aigerim@campus.edu", MemberType.STUDENT));
            members.save(new Member("M-2", "Daniyar Omarov", "daniyar@campus.edu", MemberType.STUDENT));
            members.save(new Member("M-3", "Dr. Elena Petrova", "petrova@campus.edu", MemberType.STAFF));
        }
        if (rooms.findAll().isEmpty()) {
            rooms.save(new Room("RM-1", "Study Room A", 4, false));
            rooms.save(new Room("RM-2", "Group Room B", 8, false));
            rooms.save(new Room("RM-3", "Faculty Seminar Room", 20, true));
        }
        if (catalog.loadRoot().books().findAny().isEmpty()) {
            Section root = new Section("Catalogue");
            Section science = new Section("Science");
            Section computing = new Section("Computing");
            computing.add(new Book("B-1", "Design Patterns", "Gamma, Helm, Johnson, Vlissides", 1994));
            computing.add(new Book("B-2", "Clean Architecture", "Robert C. Martin", 2017));
            computing.add(new Book("B-3", "Head First Design Patterns", "Freeman, Robson", 2020));
            Section maths = new Section("Mathematics");
            maths.add(new Book("B-4", "Linear Algebra Done Right", "Sheldon Axler", 2015));
            science.add(computing).add(maths);
            Section humanities = new Section("Humanities");
            humanities.add(new Book("B-5", "Sapiens", "Yuval Noah Harari", 2011));
            humanities.add(new Book("B-6", "The Art of Thinking Clearly", "Rolf Dobelli", 2013));
            root.add(science).add(humanities);
            catalog.saveRoot(root);
        }
    }
}
