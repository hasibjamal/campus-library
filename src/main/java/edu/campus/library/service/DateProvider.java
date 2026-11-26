package edu.campus.library.service;

import java.time.LocalDate;

/** Source of "today" for the services. */
public interface DateProvider {
    LocalDate today();
}
