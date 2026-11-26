package edu.campus.library.service.command;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

/** Invoker: runs commands and keeps them for undo (most recent first). */
public class CommandHistory {
    private final Deque<Command> done = new ArrayDeque<>();

    public void run(Command command) {
        command.execute();          // if it throws, nothing is recorded
        done.push(command);
    }

    /** Undoes the most recent command. Returns what was undone, if anything. */
    public Optional<String> undo() {
        if (done.isEmpty()) {
            return Optional.empty();
        }
        Command last = done.pop();
        last.undo();
        return Optional.of(last.description());
    }

    public List<String> descriptions() {
        return done.stream().map(Command::description).toList();
    }
}
